package raubach.frickl.next.scanner;

import raubach.frickl.next.Frickl;
import raubach.frickl.next.codegen.enums.ImagesDataType;
import raubach.frickl.next.codegen.tables.records.ImagesRecord;
import raubach.frickl.next.util.ThumbnailUtils;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Sebastian Raubach
 */
public class ImageScaler extends ImageRecordRunnable<File>
{
	private final ThumbnailUtils.Size size;
	private       File                target = null;

	public ImageScaler(ImagesRecord image, ThumbnailUtils.Size size)
	{
		super(image);
		this.size = size;
	}

	@Override
	public File call()
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

			target = ThumbnailUtils.getOrCreateThumbnail(type, image.getId(), image.getDataType(), file, size);
		}
		catch (Exception e)
		{
			// Silently fail
			Logger.getLogger("").severe(e.getMessage());
			e.printStackTrace();
		}

		return target;
	}
}
