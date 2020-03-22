package raubach.fricklweb.server.scanner;

import org.jooq.DSLContext;
import org.jooq.InsertSetMoreStep;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.restlet.data.MediaType;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.computed.DataScanResult;
import raubach.fricklweb.server.computed.Status;
import raubach.fricklweb.server.database.tables.Albums;
import raubach.fricklweb.server.database.tables.records.AlbumsRecord;
import raubach.fricklweb.server.database.tables.records.ImagesRecord;
import raubach.fricklweb.server.util.ThumbnailUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static raubach.fricklweb.server.database.tables.Albums.ALBUMS;
import static raubach.fricklweb.server.database.tables.Images.IMAGES;

/**
 * Image scanner class that recursively walks through the base directory and imports all images that haven't been there before.
 * Also reads and imports their EXIF data and existing tags.
 */
public class ImageScanner
{
	public static DataScanResult SCANRESULT = new DataScanResult();

	private ThreadPoolExecutor executor;

	private File basePath;
	private File folder;
	private Map<String, Integer> albumPathToId = new HashMap<>();
	private Map<String, Integer> imagePathToId = new HashMap<>();

	public ImageScanner(File basePath, File folder)
	{
		this.basePath = basePath;
		this.folder = folder;

		int cores = Runtime.getRuntime().availableProcessors();

		// If there are more than 2, leave one for handling of REST requests, otherwise use them all.
		if (cores > 2)
			cores--;

		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cores);
	}

	private String relativize(String input)
	{
		return basePath.toURI().relativize(new File(input).toURI()).getPath();
	}

	private String unrelativize(String input)
	{
		return new File(basePath, input).getAbsolutePath();
	}

	public void run()
			throws IOException
	{
		if (folder != null && folder.exists() && folder.isDirectory())
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				SCANRESULT.setStatus(Status.IMPORTING);
				// Get all existing albums and remember their path to id mapping
				context.selectFrom(ALBUMS)
						.stream()
						.forEach(a -> albumPathToId.put(unrelativize(a.getPath()), a.getId()));

				// Get all existing images and remember their path to id mapping
				context.selectFrom(IMAGES)
						.stream()
						.forEach(i -> imagePathToId.put(unrelativize(i.getPath()), i.getId()));

				Files.walkFileTree(folder.toPath(), new FileVisitor<Path>()
				{
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					{
						processDirectory(context, dir, attrs);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					{
						try
						{
							processFile(context, file, attrs);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}

						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc)
					{
						exc.printStackTrace();
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					{
						setAlbumBanner(context, dir);
						return FileVisitResult.CONTINUE;
					}
				});

				try
				{
					while (!executor.awaitTermination(10, TimeUnit.SECONDS))
					{
						SCANRESULT.setQueueSize(executor.getQueue().size());
						// Wait here
						Logger.getLogger("").log(Level.INFO, "Queue count: " + executor.getQueue().size());
						Logger.getLogger("").log(Level.INFO, "Queue active: " + executor.getActiveCount());

						if (executor.getQueue().size() < 1 && executor.getActiveCount() < 1)
							executor.shutdownNow();
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				SCANRESULT.reset();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void setAlbumBanner(DSLContext context, Path dir)
	{
		String path = dir.toFile().getAbsolutePath();
		Integer id = albumPathToId.get(path);

		if (id != null)
		{
			// For albums without banner image, select the first image within the album and use that as the initial banner image
			context.update(ALBUMS)
					.set(ALBUMS.BANNER_IMAGE_ID, context.select(IMAGES.ID).from(IMAGES).where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)).limit(1))
					.where(ALBUMS.BANNER_IMAGE_ID.isNull())
					.and(ALBUMS.ID.eq(id))
					.execute();

			Albums parent = ALBUMS.as("parent");
			Albums child = ALBUMS.as("child");
			// For albums that only contain other albums and no images, use the image of the first album within the album and use that as the initial banner image
			context.update(parent.innerJoin(child)
					.on(parent.ID.eq(child.PARENT_ALBUM_ID)))
					.set(parent.BANNER_IMAGE_ID, child.BANNER_IMAGE_ID)
					.where(parent.BANNER_IMAGE_ID.isNull())
					.and(parent.ID.eq(id))
					.execute();
		}
	}

	private void processDirectory(DSLContext context, Path file, BasicFileAttributes attrs)
	{
		try
		{
			if (Files.isSameFile(file, basePath.toPath()))
				return;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}

		String path = file.toFile().getAbsolutePath();
		Integer albumId = albumPathToId.get(path);
		String parentPath = file.getParent().toFile().getAbsolutePath();
		Integer parentAlbumId = albumPathToId.get(parentPath);

		if (albumId == null)
		{
			String relativePath = relativize(path);
			InsertSetMoreStep<AlbumsRecord> insertStep = context.insertInto(ALBUMS)
					.set(ALBUMS.PATH, relativePath)
					.set(ALBUMS.NAME, file.toFile().getName());

			if (parentAlbumId != null)
				insertStep.set(ALBUMS.PARENT_ALBUM_ID, parentAlbumId);


			insertStep.onDuplicateKeyIgnore()
					.returning()
					.fetchOptional()
					.ifPresent(albumsRecord -> albumPathToId.put(path, albumsRecord.getId()));
		}
	}

	private void processFile(DSLContext context, Path file, BasicFileAttributes attrs)
			throws IOException
	{
		String path = file.toFile().getAbsolutePath();
		String parentPath = file.getParent().toFile().getAbsolutePath();
		Integer imageId = imagePathToId.get(path);
		Integer albumId = albumPathToId.get(parentPath);

		if (path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".jpeg"))
		{
			if (albumId == null)
			{
				throw new IOException("Album with path not found: " + parentPath);
			}
			else
			{
				if (imageId == null)
				{
					// If the image doesn't exist, import it
					String relativePath = basePath.toURI().relativize(new File(path).toURI()).getPath();

					BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

					Optional<ImagesRecord> newImage = context.insertInto(IMAGES, IMAGES.ALBUM_ID, IMAGES.PATH, IMAGES.NAME, IMAGES.CREATED_ON)
							.values(albumId, relativePath, file.toFile().getName(), attr != null ? new Timestamp(attr.creationTime().toMillis()) : null)
							.onDuplicateKeyIgnore()
							.returning()
							.fetchOptional();

					if (newImage.isPresent())
					{
						ImagesRecord imagesRecord = newImage.get();
						imagePathToId.put(path, imagesRecord.getId());

						executor.submit(new ImageScaler(imagesRecord, ThumbnailUtils.Size.SMALL));
						executor.submit(new ImageScaler(imagesRecord, ThumbnailUtils.Size.MEDIUM));
						executor.submit(new ImageExifReader(imagesRecord));

						SCANRESULT.incrementTotalImages();
					}
				}
				else
				{
					// If it exists, get it and then check if exif is missing.
					ImagesRecord imagesRecord = context.select()
							.from(IMAGES)
							.where(IMAGES.ID.eq(imageId))
							.fetchSingleInto(ImagesRecord.class);

					if (imagesRecord != null)
					{
						if (imagesRecord.getExif() == null)
						{
							executor.submit(new ImageExifReader(imagesRecord));
						}

						MediaType type;

						if (file.toFile().getName().toLowerCase().endsWith(".jpg"))
							type = MediaType.IMAGE_JPEG;
						else if (file.toFile().getName().toLowerCase().endsWith(".png"))
							type = MediaType.IMAGE_PNG;
						else
							type = MediaType.IMAGE_ALL;

						if (!ThumbnailUtils.thumbnailExists(type, imagesRecord.getId(), file.toFile(), ThumbnailUtils.Size.SMALL))
						{
							executor.submit(new ImageScaler(imagesRecord, ThumbnailUtils.Size.SMALL));
						}

						if (!ThumbnailUtils.thumbnailExists(type, imagesRecord.getId(), file.toFile(), ThumbnailUtils.Size.MEDIUM))
						{
							executor.submit(new ImageScaler(imagesRecord, ThumbnailUtils.Size.MEDIUM));
						}

						SCANRESULT.incrementTotalImages();
					}
				}
			}
		}
	}
}