package raubach.frickl.next.util;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import raubach.frickl.next.codegen.enums.ImagesDataType;
import raubach.frickl.next.codegen.tables.records.ImagesRecord;
import raubach.frickl.next.scanner.ImageScaler;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * @author Sebastian Raubach
 */
public class ThumbnailUtils
{
	private static ThreadPoolExecutor executor;

	static
	{
		int cores = Runtime.getRuntime().availableProcessors();

		if (cores > 2)
			cores--;

		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cores);

		Logger.getLogger("").info("STARTED THUMBNAIL EXECUTOR WITH " + cores + " CORES.");
	}

	public static boolean thumbnailExists(String type, ImagesRecord image, File file, Size size)
	{
		String version = PropertyWatcher.get(ServerProperty.API_VERSION);
		File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-thumbnails" + "-" + version);
		folder.mkdirs();

		String extension = Objects.equals(type, "image/png") ? ".png" : ".jpg";

		File target = new File(folder, image.getId() + size.getSuffix() + extension);

		// Delete the thumbnail if it's older than the source image
		if (target.lastModified() < file.lastModified())
			target.delete();

		// If it exists, fine, just return it
		return target.exists();
	}

	public static File getOrCreateThreaded(String type, ImagesRecord image, File file, Size size)
		throws IOException
	{
		if (size == null)
			size = Size.ORIGINAL;

		File result;
		String version = PropertyWatcher.get(ServerProperty.API_VERSION);
		File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-thumbnails" + "-" + version);
		folder.mkdirs();

		String extension = Objects.equals(type, "image/png") ? ".png" : ".jpg";

		File target = new File(folder, image.getId() + size.getSuffix() + extension);

		// Delete the thumbnail if it's older than the source image
		if (target.exists() && target.lastModified() < file.lastModified())
			target.delete();

		// If it exists, fine, just return it
		if (target.exists())
		{
			result = target;
		}
		// If not, create a new thumbnail
		else
		{
			Future<File> future = executor.submit(new ImageScaler(image, size));
			try
			{
				result = future.get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				Logger.getLogger("").severe(e.getMessage());
				e.printStackTrace();
				result = null;
			}
		}

		return result;
	}

	public static File getOrCreateThumbnail(String type, Integer imageId, ImagesDataType dataType, File file, Size size)
		throws IOException
	{
		File result = null;
		String version = PropertyWatcher.get(ServerProperty.API_VERSION);
		File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-thumbnails" + "-" + version);
		folder.mkdirs();

		String extension = Objects.equals(type, "image/png") ? ".png" : ".jpg";

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
			if (dataType == ImagesDataType.video)
			{
				file = createVideoImage(file);
			}

			if (file != null)
			{
				createThumb(file, target, size);
				result = target;
			}
		}

		return result;
	}

	private static void createThumb(File source, File target, Size size)
	{
		int result = -1;
		try
		{
			String[] commands = {"convert", "-auto-orient", "-geometry", "x" + size.height, source.getAbsolutePath(), target.getAbsolutePath()};
			Process p = new ProcessBuilder(commands).start();
			String output = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
			result = p.waitFor();
		}
		catch (IOException | InterruptedException e)
		{
			// Fail silently
		}
		finally
		{
			if (result != 0)
			{
				try
				{
					Thumbnails.of(source)
							  .height(size.height)
							  .keepAspectRatio(true)
							  .outputQuality(0.8)
							  .toFile(target);
				}
				catch (IOException ex)
				{
					// Fail silently
				}
			}
		}
	}

	private static File createVideoImage(File video)
	{
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

			commands = new String[]{"ffmpeg", "-y", "-ss", Integer.toString(timePoint), "-i", video.getAbsolutePath(), "-vframes", "1", "-q:v", "2", videoImage.getAbsolutePath()};

			p = new ProcessBuilder(commands).start();
			p.waitFor();

			return videoImage;
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}

		return video;
	}

	public static void shutdownExecutor()
	{
		if (executor != null && !executor.isShutdown())
		{
			try
			{
				executor.shutdown();
			}
			catch (SecurityException e)
			{
				// Ignore
			}
		}
	}

	public enum Size
	{
		TINY("-tiny", 50),
		SMALL("-small", 400),
		MEDIUM("-medium", 1080),
		ORIGINAL("", -1);

		private final String suffix;
		private final int    height;

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
