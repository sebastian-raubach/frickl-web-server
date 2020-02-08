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
	public static boolean thumbnailExists(ServletContext servlet, MediaType type, Integer imageId, File file, Size size)
	{
		String version = servlet.getInitParameter("version");
		File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-thumbnails" + "-" + version);
		folder.mkdirs();

		String extension = type == MediaType.IMAGE_PNG ? ".png" : ".jpg";

		File target = new File(folder, imageId + size.getSuffix() + extension);

		// Delete the thumbnail if it's older than the source image
		if (target.lastModified() < file.lastModified())
			target.delete();

		// If it exists, fine, just return it
		return target.exists();
	}

	public static File getOrCreateThumbnail(ServletContext servlet, MediaType type, Integer imageId, File file, Size size)
		throws IOException
	{
		File result;
		String version = servlet.getInitParameter("version");
		File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-thumbnails" + "-" + version);
		folder.mkdirs();

		String extension = type == MediaType.IMAGE_PNG ? ".png" : ".jpg";

		File target = new File(folder, imageId + size.getSuffix() + extension);

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
					  .height(size.height)
					  .keepAspectRatio(true)
					  .toFile(target);

			result = target;
		}

		return result;
	}

	public enum Size {
		SMALL("-small", 400),
		MEDIUM("-medium", 1080),
		ORIGINAL("", -1);

		private String suffix;
		private int height;

		Size(String suffix, int height)
		{
			this.suffix = suffix;
			this.height = height;
		}

		public String getSuffix()
		{
			return suffix;
		}

		public int getHeight()
		{
			return height;
		}
	}
}
