package raubach.fricklweb.server.scanner;

import org.jooq.*;
import org.restlet.data.MediaType;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.computed.*;
import raubach.fricklweb.server.database.enums.ImagesDataType;
import raubach.fricklweb.server.database.tables.Albums;
import raubach.fricklweb.server.database.tables.records.*;
import raubach.fricklweb.server.util.ThumbnailUtils;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import static raubach.fricklweb.server.database.tables.Albums.*;
import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * Image scanner class that recursively walks through the base directory and imports all images that haven't been there before.
 * Also reads and imports their EXIF data and existing tags.
 */
public class ImageScanner
{
	public static DataScanResult SCANRESULT = new DataScanResult();

	private ThreadPoolExecutor executor;

	private File                      basePath;
	private File                      folder;
	private Map<String, AlbumsRecord> albumPathToId = new HashMap<>();
	private Map<String, ImagesRecord> imagePathToId = new HashMap<>();

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
				 DSLContext context = Database.getContext(conn))
			{
				SCANRESULT.setStatus(Status.SCANNING);
				// Get all existing albums and remember their path to id mapping
				context.selectFrom(ALBUMS)
					   .stream()
					   .forEach(a -> albumPathToId.put(unrelativize(a.getPath()), a));

				// Get all existing images and remember their path to id mapping
				context.selectFrom(IMAGES)
					   .stream()
					   .forEach(i -> {
						   imagePathToId.put(unrelativize(i.getPath()), i);

						   // For videos, add their representative images as well
						   if (i.getDataType() == ImagesDataType.video)
						   {
							   String path = unrelativize(i.getPath());

							   path = path.substring(0, path.lastIndexOf(".")) + ".jpg";

							   imagePathToId.put(path, i);
						   }
					   });

				Files.walkFileTree(folder.toPath(), new FileVisitor<Path>()
				{
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
					{
						// Process the album
						processDirectory(context, dir, attrs);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					{
						try
						{
							// Process the image
							processFile(context, file);
						}
						catch (IOException e)
						{
							Logger.getLogger("").severe(e.getMessage());
							e.printStackTrace();
						}

						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException e)
					{
						// Something went wrong, print exception
						e.printStackTrace();
						Logger.getLogger("").severe(e.getMessage());
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					{
						// Set the album cover now that all images (and sub-albums) have been processed
						setAlbumBanner(context, dir);
						Logger.getLogger("").info("Finished scanning directory: " + dir.toFile().getAbsolutePath());
						return FileVisitResult.CONTINUE;
					}
				});

				try
				{
					SCANRESULT.setStatus(Status.IMPORTING);

					while (!executor.awaitTermination(10, TimeUnit.SECONDS))
					{
						SCANRESULT.setQueueSize(executor.getQueue().size());
						// Wait here
						Logger.getLogger("").log(Level.INFO, "Queue count: " + executor.getQueue().size());
						Logger.getLogger("").log(Level.INFO, "Queue active: " + executor.getActiveCount());

						if (executor.getQueue().size() < 1 && executor.getActiveCount() < 1)
						{
							executor.shutdownNow();
						}
						else
						{
							Runnable runnable = executor.getQueue().peek();

							if (runnable instanceof ImageRecordRunnable)
								Logger.getLogger("").log(Level.INFO, "TOP RUNNABLE: " + ((ImageRecordRunnable) runnable).getId());
						}
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
		AlbumsRecord album = albumPathToId.get(path);

		if (album != null)
		{
			if (album.getBannerImageId() == null)
			{
				// For albums without banner image, select the first image within the album and use that as the initial banner image
				Integer imageId = context.select(IMAGES.ID).from(IMAGES).where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)).limit(1).fetchAnyInto(Integer.class);
				if (imageId != null)
				{
					album.setBannerImageId(imageId);
					album.store(ALBUMS.BANNER_IMAGE_ID);
				}
				else
				{
					// For albums that only contain other albums and no images, use the image of the first album within the album and use that as the initial banner image
					Albums parent = ALBUMS.as("parent");
					Albums child = ALBUMS.as("child");
					imageId = context.select(child.BANNER_IMAGE_ID).from(child.leftJoin(parent).on(parent.ID.eq(child.PARENT_ALBUM_ID))).limit(1).fetchAnyInto(Integer.class);

					if (imageId != null)
					{
						album.setBannerImageId(imageId);
						album.store(ALBUMS.BANNER_IMAGE_ID);
					}
				}
			}
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
		AlbumsRecord albumId = albumPathToId.get(path);

		if (albumId == null)
		{
			String parentPath = file.getParent().toFile().getAbsolutePath();
			AlbumsRecord parentAlbumId = albumPathToId.get(parentPath);

			String relativePath = relativize(path);
			InsertSetMoreStep<AlbumsRecord> insertStep = context.insertInto(ALBUMS)
																.set(ALBUMS.PATH, relativePath)
																.set(ALBUMS.NAME, file.toFile().getName());

			if (parentAlbumId != null)
				insertStep.set(ALBUMS.PARENT_ALBUM_ID, parentAlbumId.getId());


			insertStep.onDuplicateKeyIgnore()
					  .returning()
					  .fetchOptional()
					  .ifPresent(albumsRecord -> albumPathToId.put(path, albumsRecord));
		}
	}

	private void processFile(DSLContext context, Path file)
		throws IOException
	{
		String path = file.toFile().getAbsolutePath();
		String mimeType = URLConnection.guessContentTypeFromName(path);
		boolean isVideo = mimeType != null && mimeType.startsWith("video");

		if (path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".jpeg") || isVideo)
		{
			String parentPath = file.getParent().toFile().getAbsolutePath();
			AlbumsRecord album = albumPathToId.get(parentPath);

			if (album == null)
			{
				throw new IOException("Album with path not found: " + parentPath);
			}
			else
			{
				ImagesRecord image = imagePathToId.get(path);

				if (image == null)
				{
					// If the image doesn't exist, import it
					String relativePath = basePath.toURI().relativize(new File(path).toURI()).getPath();

					BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

					Optional<ImagesRecord> newImage = context.insertInto(IMAGES, IMAGES.ALBUM_ID, IMAGES.PATH, IMAGES.NAME, IMAGES.DATA_TYPE, IMAGES.CREATED_ON)
															 .values(album.getId(), relativePath, file.toFile().getName(), isVideo ? ImagesDataType.video : ImagesDataType.image, attr != null ? new Timestamp(attr.creationTime().toMillis()) : null)
															 .onDuplicateKeyIgnore()
															 .returning()
															 .fetchOptional();

					if (newImage.isPresent())
					{
						ImagesRecord imagesRecord = newImage.get();
						imagePathToId.put(path, imagesRecord);

						if (isVideo)
						{
							// If it's a video, add the newly created thumbnail as well, because we want to ignore those
							String imagePath = relativePath.substring(0, relativePath.lastIndexOf(".")) + ".jpg";

							imagePathToId.put(imagePath, imagesRecord);
						}

						executor.submit(new ImageScaler(imagesRecord, ThumbnailUtils.Size.SMALL));
						executor.submit(new ImageScaler(imagesRecord, ThumbnailUtils.Size.MEDIUM));

						if (!isVideo)
							executor.submit(new ImageExifReader(imagesRecord));

						SCANRESULT.incrementTotalImages();

						if (imagesRecord.getCreatedOn().getTime() > album.getCreatedOn().getTime())
						{
							album.setCreatedOn(imagesRecord.getCreatedOn());
							album.store(ALBUMS.CREATED_ON);
						}
					}
				}
				else
				{
					MediaType type;

					if (file.toFile().getName().toLowerCase().endsWith(".jpg") || image.getDataType() == ImagesDataType.video)
						type = MediaType.IMAGE_JPEG;
					else if (file.toFile().getName().toLowerCase().endsWith(".png"))
						type = MediaType.IMAGE_PNG;
					else
						type = MediaType.IMAGE_ALL;

					if (image.getExif() == null && image.getDataType() == ImagesDataType.image)
						executor.submit(new ImageExifReader(image));

					if (!ThumbnailUtils.thumbnailExists(type, image, file.toFile(), ThumbnailUtils.Size.SMALL))
						executor.submit(new ImageScaler(image, ThumbnailUtils.Size.SMALL));

					if (!ThumbnailUtils.thumbnailExists(type, image, file.toFile(), ThumbnailUtils.Size.MEDIUM))
						executor.submit(new ImageScaler(image, ThumbnailUtils.Size.MEDIUM));

					SCANRESULT.incrementTotalImages();

					if (image.getCreatedOn().getTime() > album.getCreatedOn().getTime())
					{
						album.setCreatedOn(image.getCreatedOn());
						album.store(ALBUMS.CREATED_ON);
					}
				}
			}
		}
	}
}