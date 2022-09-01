package raubach.fricklweb.server.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Sebastian Raubach
 */
public class TagUtils
{
	public static synchronized void deleteTagFromFileOrFolder(File file, List<String> tags)
		throws IOException
	{
		int result = -1;
		try
		{
			List<String> commands = new ArrayList<>();
			commands.add("exiftool");
			commands.add("-overwrite_original");
			tags.forEach(t -> commands.add("-keywords-=" + t));
			commands.add(file.getAbsolutePath());

			Process p = new ProcessBuilder(commands.toArray(String[]::new)).start();
			IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
			result = p.waitFor();
		}
		catch (InterruptedException e)
		{
			throw new IOException(e);
		}
		finally
		{
			if (result != 0)
			{
				throw new IOException("Adding image tag failed");
			}
		}
	}

	public static synchronized void addTagToFileOrFolder(File file, List<String> tags)
		throws IOException
	{
		int result = -1;
		try
		{
			List<String> commands = new ArrayList<>();
			commands.add("exiftool");
			commands.add("-overwrite_original");
			tags.forEach(t -> {
				// Remove first to prevent duplication
				commands.add("-keywords-=" + t);
				commands.add("-keywords+=" + t);
			});
			commands.add(file.getAbsolutePath());

			Process p = new ProcessBuilder(commands.toArray(String[]::new)).start();
			IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
			result = p.waitFor();
		}
		catch (InterruptedException e)
		{
			throw new IOException(e);
		}
		finally
		{
			if (result != 0)
			{
				throw new IOException("Adding image tag failed: " + file.getAbsolutePath());
			}
		}
	}
}
