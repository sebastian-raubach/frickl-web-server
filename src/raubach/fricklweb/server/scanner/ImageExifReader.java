package raubach.fricklweb.server.scanner;

import com.drew.imaging.*;
import com.drew.lang.*;
import com.drew.metadata.*;
import com.drew.metadata.exif.*;

import org.jooq.*;
import org.jooq.impl.*;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.computed.*;
import raubach.fricklweb.server.database.tables.records.*;

import static raubach.fricklweb.server.database.tables.ImageTags.*;
import static raubach.fricklweb.server.database.tables.Images.*;
import static raubach.fricklweb.server.database.tables.Tags.*;

/**
 * @author Sebastian Raubach
 */
public class ImageExifReader implements Runnable
{
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

	private ImagesRecord image;

	public ImageExifReader(ImagesRecord image)
	{
		this.image = image;
	}

	private static synchronized void processKeywords(DSLContext context, ImagesRecord image, List<String> keywords)
	{
		Map<String, Integer> existingKeywords = new HashMap<>();
		context.selectFrom(TAGS)
			   .where(TAGS.NAME.in(keywords))
			   .stream()
			   .forEach(k -> existingKeywords.put(k.getName(), k.getId()));

		for (String keyword : keywords)
		{
			Integer tagId = existingKeywords.get(keyword);

			if (tagId == null)
			{
				Optional<TagsRecord> tag = context.insertInto(TAGS, TAGS.NAME)
												  .values(keyword)
												  .returning()
												  .fetchOptional();

				if (tag.isPresent())
					tagId = tag.get().getId();
			}

			if (tagId != null)
			{
				context.insertInto(IMAGE_TAGS, IMAGE_TAGS.IMAGE_ID, IMAGE_TAGS.TAG_ID)
					   .values(image.getId(), tagId)
					   .onDuplicateKeyIgnore()
					   .execute();
			}
			else
			{
				throw new RuntimeException("Tag: " + keyword + " couldn't be created.");
			}
		}
	}

	@Override
	public void run()
	{
		ExifResult exif;
		Timestamp date;

		try
		{
			File file = new File(Frickl.BASE_PATH, image.getPath());
			exif = getExif(file);

			if (exif.exif.getDateTime() != null)
				date = new Timestamp(exif.exif.getDateTime().getTime());
			else if (exif.exif.getDateTimeOriginal() != null)
				date = new Timestamp(exif.exif.getDateTimeOriginal().getTime());
			else if (exif.exif.getDateTimeDigitized() != null)
				date = new Timestamp(exif.exif.getDateTimeDigitized().getTime());
			else
				date = null;
		}
		catch (IOException | ImageProcessingException e)
		{
			e.printStackTrace();
			exif = null;
			date = null;
		}

		try (Connection conn = Database.getConnection();
			 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
		{
			context.update(IMAGES)
				   .set(IMAGES.EXIF, exif.exif)
				   .set(IMAGES.CREATED_ON, date)
				   .where(IMAGES.ID.eq(image.getId()))
				   .execute();

			if (exif.keyword.size() > 0)
				processKeywords(context, image, exif.keyword);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private ExifResult getExif(File image)
		throws ImageProcessingException, IOException
	{
		Metadata metadata = ImageMetadataReader.readMetadata(image);

		Exif exif = new Exif();
		List<String> keywords = new ArrayList<>();

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

			if (dir.getName().contains("PrintIM"))
				continue;

			Collection<com.drew.metadata.Tag> tags = dir.getTags();
			for (Tag tag : tags)
			{
				try
				{
					switch (tag.getTagType())
					{
						case 0x0100:
						case 0xbc80:
							exif.setImageWidth(tag.getDescription());
							break;
						case 0x0101:
						case 0xbc81:
							exif.setImageHeight(tag.getDescription());
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
							exif.setIsoSpeedRatings(tag.getDescription());
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
							exif.setExifImageWidth(tag.getDescription());
							break;
						case 0xa003:
							exif.setExifImageHeight(tag.getDescription());
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
						case 0x0219:
							String k = tag.getDescription();
							if (k != null)
								keywords.addAll(Arrays.asList(k.split(";")));
							break;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		return new ExifResult(exif, keywords);
	}

	private static class ExifResult
	{
		private Exif         exif;
		private List<String> keyword;

		public ExifResult(Exif exif, List<String> keyword)
		{
			this.exif = exif;
			this.keyword = keyword;
		}
	}
}