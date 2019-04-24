package raubach.fricklweb.server.scanner;

import org.jooq.*;
import org.jooq.impl.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import javax.servlet.*;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.computed.*;
import raubach.fricklweb.server.database.tables.records.*;

import static raubach.fricklweb.server.database.tables.Albums.*;
import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * Image scanner class that recursively walks through the base directory and imports all images that haven't been there before.
 * Also reads and imports their EXIF data and existing tags.
 * // TODO: Add a queue and push images on it when found by file walker. Then work through the queue in separate threats making use of multiple cores.
 */
public class ImageScanner
{
	public static Status STATUS = Status.UNKNOWN;

	private ThreadPoolExecutor executor;

	private ServletContext       context;
	private File                 basePath;
	private File                 folder;
	private Map<String, Integer> albumPathToId = new HashMap<>();
	private Map<String, Integer> imagePathToId = new HashMap<>();

	public ImageScanner(ServletContext context, File basePath, File folder)
	{
		this.context = context;
		this.basePath = basePath;
		this.folder = folder;

		int cores = Runtime.getRuntime().availableProcessors();
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
				STATUS = Status.IMPORTING;
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
						throws IOException
					{
						processDirectory(context, dir, attrs);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						throws IOException
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
						throws IOException
					{
						exc.printStackTrace();
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc)
						throws IOException
					{
						setAlbumBanner(context, dir);
						return FileVisitResult.CONTINUE;
					}
				});

				try
				{
					while (!executor.awaitTermination(10, TimeUnit.SECONDS))
					{
						// Wait here
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				STATUS = Status.IDLE;
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


			Optional<AlbumsRecord> newAlbum = insertStep.onDuplicateKeyIgnore()
														.returning()
														.fetchOptional();

			if (newAlbum.isPresent())
				albumPathToId.put(path, newAlbum.get().getId());
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

					Optional<ImagesRecord> newImage = context.insertInto(IMAGES, IMAGES.ALBUM_ID, IMAGES.PATH, IMAGES.NAME)
															 .values(albumId, relativePath, file.toFile().getName())
															 .onDuplicateKeyIgnore()
															 .returning()
															 .fetchOptional();

					if (newImage.isPresent())
					{
						ImagesRecord imagesRecord = newImage.get();
						imagePathToId.put(path, imagesRecord.getId());

						executor.submit(new ImageScaler(this.context, imagesRecord));
						executor.submit(new ImageExifReader(imagesRecord));
					}
				}
				else
				{
					// If it exists, get it and then check if exif is missing.
					ImagesRecord imagesRecord = context.select()
													   .from(IMAGES)
													   .where(IMAGES.ID.eq(imageId))
													   .fetchSingleInto(ImagesRecord.class);

					if (imagesRecord != null && imagesRecord.getExif() == null)
					{
						executor.submit(new ImageScaler(this.context, imagesRecord));
						executor.submit(new ImageExifReader(imagesRecord));
					}
				}
			}
		}
	}
}