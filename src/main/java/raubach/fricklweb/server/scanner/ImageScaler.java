package raubach.fricklweb.server.scanner;

import org.restlet.data.MediaType;
import raubach.fricklweb.server.Frickl;
import raubach.fricklweb.server.database.enums.ImagesDataType;
import raubach.fricklweb.server.database.tables.records.ImagesRecord;
import raubach.fricklweb.server.util.ThumbnailUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Sebastian Raubach
 */
public class ImageScaler extends ImageRecordRunnable
{
	private ImagesRecord image;
	private ThumbnailUtils.Size size;

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
			MediaType type;

			if (file.getName().toLowerCase().endsWith(".jpg") || image.getDataType() == ImagesDataType.video)
				type = MediaType.IMAGE_JPEG;
			else if (file.getName().toLowerCase().endsWith(".png"))
				type = MediaType.IMAGE_PNG;
			else
				type = MediaType.IMAGE_ALL;

			ThumbnailUtils.getOrCreateThumbnail(type, image.getId(), image.getDataType(), file, size);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
