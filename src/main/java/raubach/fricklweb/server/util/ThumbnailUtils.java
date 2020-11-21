package raubach.fricklweb.server.util;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import org.restlet.data.MediaType;
import raubach.fricklweb.server.Frickl;
import raubach.fricklweb.server.database.enums.ImagesDataType;
import raubach.fricklweb.server.database.tables.records.ImagesRecord;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Sebastian Raubach
 */
public class ThumbnailUtils
{
	public static boolean thumbnailExists(MediaType type, ImagesRecord image, File file, Size size)
	{
		String version = PropertyWatcher.get(ServerProperty.API_VERSION);
		File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-thumbnails" + "-" + version);
		folder.mkdirs();

		String extension = type == MediaType.IMAGE_PNG ? ".png" : ".jpg";

		File target = new File(folder, image.getId() + size.getSuffix() + extension);

		// Delete the thumbnail if it's older than the source image
		if (target.lastModified() < file.lastModified())
			target.delete();

		// If it exists, fine, just return it
		return target.exists();
	}

	public static File getOrCreateThumbnail(MediaType type, Integer imageId, ImagesDataType dataType, File file, Size size)
			throws IOException
	{
		File result;
		String version = PropertyWatcher.get(ServerProperty.API_VERSION);
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
			if (dataType == ImagesDataType.video) {
				file = createVideoImage(file);
			}

			Thumbnails.of(file)
					.height(size.height)
					.keepAspectRatio(true)
					.toFile(target);

			result = target;
		}

		return result;
	}

	private static File createVideoImage(File video) {
		try
		{
			File videoImage = new File(video.getParentFile(), video.getName().substring(0, video.getName().lastIndexOf(".")) + ".jpg");

			String[] commands = {"ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", video.getAbsolutePath()};
			Process p = new ProcessBuilder(commands).start();
			String output = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
			p.waitFor();

			int timePoint = 0;
			try
			{
				timePoint = (int) (Double.parseDouble(output) / 2);
			}
			catch (NullPointerException | NumberFormatException e)
			{
				e.printStackTrace();
			}

			commands = new String[]{"ffmpeg", "-ss", Integer.toString(timePoint), "-i", video.getAbsolutePath(), "-vframes", "1", "-q:v", "2", videoImage.getAbsolutePath()};

			p = new ProcessBuilder(commands).start();
			p.waitFor();

			return videoImage;
		}
		catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return video;
	}

	public enum Size
	{
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
