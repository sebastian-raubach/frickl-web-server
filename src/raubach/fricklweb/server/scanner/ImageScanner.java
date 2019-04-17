package raubach.fricklweb.server.scanner;

import com.drew.imaging.*;
import com.drew.lang.*;
import com.drew.metadata.*;
import com.drew.metadata.exif.*;

import org.jooq.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.computed.*;
import raubach.fricklweb.server.database.tables.records.*;

import static raubach.fricklweb.server.database.tables.Albums.*;
import static raubach.fricklweb.server.database.tables.Images.*;

public class ImageScanner
{
	private SimpleDateFormat sdf = new SimpleDateFormat("YYYY:MM:DD (HH:MM:SS)");

	private File                 basePath;
	private File                 folder;
	private Map<String, Integer> albumPathToId = new HashMap<>();
	private Map<String, Integer> imagePathToId = new HashMap<>();

	public ImageScanner(File basePath, File folder)
	{
		this.basePath = basePath;
		this.folder = folder;
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
			try (DSLContext context = Database.context())
			{
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
						if (attrs.isDirectory())
							processDirectory(context, dir, attrs);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						throws IOException
					{
						try
						{
							if (attrs.isDirectory())
								processDirectory(context, file, attrs);
							else if (attrs.isRegularFile())
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
						return FileVisitResult.CONTINUE;
					}
				});

				// TODO: Set banner_image_id for each new album to the first image in the album.
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void processDirectory(DSLContext context, Path file, BasicFileAttributes attrs)
	{
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

		if (albumId == null)
		{
			throw new IOException("Album with path not found: " + parentPath);
		}
		else
		{
			if (imageId == null)
			{
				Exif exif;

				try
				{
					exif = getExif(file.toFile());
				}
				catch (ImageProcessingException e)
				{
					e.printStackTrace();
					exif = null;
				}

				String relativePath = basePath.toURI().relativize(new File(path).toURI()).getPath();

				Optional<ImagesRecord> newImage = context.insertInto(IMAGES, IMAGES.ALBUM_ID, IMAGES.PATH, IMAGES.EXIF)
														 .values(albumId, relativePath, exif)
														 .onDuplicateKeyIgnore()
														 .returning()
														 .fetchOptional();

				if (newImage.isPresent())
					imagePathToId.put(path, newImage.get().getId());
			}
		}
	}

	private Exif getExif(File image)
		throws ImageProcessingException, IOException
	{
		Metadata metadata = ImageMetadataReader.readMetadata(image);

		Exif exif = new Exif();

		// See whether it has GPS data
		Collection<GpsDirectory> gpsDirectories = metadata.getDirectoriesOfType(GpsDirectory.class);
		for (GpsDirectory gpsDirectory : gpsDirectories)
		{
			// Try to read out the location, making sure it's non-zero
			GeoLocation geoLocation = gpsDirectory.getGeoLocation();
			if (geoLocation != null && !geoLocation.isZero())
			{
				// Add to our collection for use below
				exif.setGpsLatitude(geoLocation.getLatitude())
					.setGpsLongitude(geoLocation.getLongitude())
					.setGpsTimestamp(gpsDirectory.getGpsDate());
				// TODO: How to get the altitude?
				break;
			}
		}

		Iterable<Directory> directories = metadata.getDirectories();
		Iterator<Directory> iterator = directories.iterator();
		while (iterator.hasNext())
		{
			Directory dir = iterator.next();
			Collection<Tag> tags = dir.getTags();
			for (Tag tag : tags)
			{
				try
				{
					switch (tag.getTagType())
					{
						case 0x0100:
						case 0xbc80:
							exif.setImageWidth(Integer.parseInt(tag.getDescription()));
							break;
						case 0x0101:
						case 0xbc81:
							exif.setImageHeight(Integer.parseInt(tag.getDescription()));
							break;
						case 0x0103:
							exif.setCompression(tag.getDescription());
							break;
						case 0x0106:
							exif.setPhotometricInterpretation(tag.getDescription());
							break;
						case 0x010f:
							exif.setCameraMake(tag.getDescription());
							break;
						case 0x0110:
							exif.setCameraModel(tag.getDescription());
							break;
						case 0x0112:
							exif.setOriantation(tag.getDescription());
							break;
						case 0x0115:
							exif.setSamplesPerPixel(tag.getDescription());
							break;
						case 0x011a:
							exif.setxResolution(tag.getDescription());
							break;
						case 0x011b:
							exif.setyResolution(tag.getDescription());
							break;
						case 0x829a:
							exif.setExposureTime(tag.getDescription());
							break;
						case 0x829d:
							exif.setfNumber(tag.getDescription());
							break;
						case 0x8827:
							exif.setIsoSpeedRatings(Integer.parseInt(tag.getDescription()));
							break;
						case 0x9000:
							exif.setExifVersion(tag.getDescription());
							break;
						case 0x9003:
							exif.setDateTimeOriginal(sdf.parse(tag.getDescription()));
							break;
						case 0x0132:
							exif.setDateTime(sdf.parse(tag.getDescription()));
							break;
						case 0x9004:
							exif.setDateTimeDigitized(sdf.parse(tag.getDescription()));
							break;
						case 0x9201:
							exif.setShutterSpeedValue(tag.getDescription());
							break;
						case 0x9202:
							exif.setApertureValue(tag.getDescription());
							break;
						case 0x9207:
							exif.setMeteringMode(tag.getDescription());
							break;
						case 0x9209:
							exif.setFlash(tag.getDescription());
							break;
						case 0x920a:
							exif.setFocalLength(tag.getDescription());
							break;
						case 0x9217:
						case 0xa217:
							exif.setSensingMethod(tag.getDescription());
							break;
						case 0xa001:
							exif.setColorSpace(tag.getDescription());
							break;
						case 0xa002:
							exif.setExifImageWidth(Integer.parseInt(tag.getDescription()));
							break;
						case 0xa003:
							exif.setExifImageHeight(Integer.parseInt(tag.getDescription()));
							break;
						case 0xa402:
							exif.setExposureMode(tag.getDescription());
							break;
						case 0xa403:
							exif.setWhiteBalanceMode(tag.getDescription());
							break;
						case 0xa404:
							exif.setDigitalZoomRatio(tag.getDescription());
							break;
						case 0xa406:
							exif.setSceneCaptureType(tag.getDescription());
							break;
						case 0xa407:
							exif.setGainControl(tag.getDescription());
							break;
						case 0xa408:
						case 0xfe54:
							exif.setContrast(tag.getDescription());
							break;
						case 0xa409:
						case 0xfe55:
							exif.setSaturation(tag.getDescription());
							break;
						case 0xa40a:
						case 0xfe56:
							exif.setSharpness(tag.getDescription());
							break;
						case 0xa433:
							exif.setLensMake(tag.getDescription());
							break;
						case 0xa434:
							exif.setLensModel(tag.getDescription());
							break;
						case 0xfe4e:
							exif.setWhiteBalance(tag.getDescription());
							break;
						case 0xfe51:
							exif.setExposure(tag.getDescription());
							break;
						case 0x9204:
							exif.setExposureBiasValue(tag.getDescription());
							break;
						case 0x8822:
							exif.setExposureProgram(tag.getDescription());
							break;
						case 0xa300:
							exif.setFileSource(tag.getDescription());
							break;
						case 0xa301:
							exif.setSceneType(tag.getDescription());
							break;
						case 0x9286:
							exif.setUserComment(tag.getDescription());
							break;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				System.out.println(tag.getTagName() + "  " + tag.getDescription() + " " + tag.getTagTypeHex());
			}
		}

		return exif;
	}
}