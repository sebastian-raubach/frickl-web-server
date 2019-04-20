package raubach.fricklweb.server.util;

import net.coobird.thumbnailator.*;

import org.restlet.data.*;

import java.io.*;

import javax.servlet.*;

/**
 * @author Sebastian Raubach
 */
public class ThumbnailUtils
{
	public static File getOrCreateThumbnail(ServletContext servlet, MediaType type, Integer imageId, File file)
		throws IOException
	{
		File result = file;
		String version = servlet.getInitParameter("version");
		File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-thumbnails" + "-" + version);
		folder.mkdirs();

		String extension = type == MediaType.IMAGE_PNG ? ".png" : ".jpg";

		File target = new File(folder, imageId + "-small" + extension);

		// Delete the thumbnail if it's older than the source image
		if (target.lastModified() < file.lastModified())
			target.delete();

		// If it exists, fine, just return it
		if (target.exists())
		{
			result = target;
		}
		// If not, create a new thumbnail
		else
		{
			Thumbnails.of(file)
					  .height(400)
					  .keepAspectRatio(true)
					  .toFile(target);

			result = target;
		}

		return result;
	}
}
