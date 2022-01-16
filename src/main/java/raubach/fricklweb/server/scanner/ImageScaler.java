package raubach.fricklweb.server.scanner;

import raubach.fricklweb.server.Frickl;
import raubach.fricklweb.server.database.enums.ImagesDataType;
import raubach.fricklweb.server.database.tables.records.ImagesRecord;
import raubach.fricklweb.server.util.ThumbnailUtils;

import java.io.File;

/**
 * @author Sebastian Raubach
 */
public class ImageScaler extends ImageRecordRunnable
{
	private       ImagesRecord        image;
	private final ThumbnailUtils.Size size;

	public ImageScaler(ImagesRecord image, ThumbnailUtils.Size size)
	{
		super(image);
		this.size = size;
	}

	@Override
	public void run()
	{
		File file = new File(Frickl.BASE_PATH, image.getPath());

		// Check if the image exists
		try
		{
			String type;

			if (file.getName().toLowerCase().endsWith(".jpg") || image.getDataType() == ImagesDataType.video)
				type = "image/jpeg";
			else if (file.getName().toLowerCase().endsWith(".png"))
				type = "image/png";
			else
				type = "image/*";

			ThumbnailUtils.getOrCreateThumbnail(type, image.getId(), image.getDataType(), file, size);
		}
		catch (Exception e)
		{
			// Silently fail
//			e.printStackTrace();
		}
	}
}
