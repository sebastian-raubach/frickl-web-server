package raubach.fricklweb.server.util;

import org.apache.commons.io.FileUtils;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import javax.ws.rs.core.Response;
import java.io.*;
import java.util.UUID;

public class ResourceUtils
{
	public static File getTempFile(String parentFolder, String filename)
		throws IOException
	{
		String path = PropertyWatcher.get(ServerProperty.DATABASE_NAME);
		File folder = new File(System.getProperty("java.io.tmpdir"), path);

		if (!StringUtils.isEmpty(parentFolder))
			folder = new File(folder, parentFolder);

		File file = new File(folder, filename);

		if (!isSubDirectory(folder, file))
		{
			throw new IOException("Invalid file access");
		}

		return file;
	}

	public static File createTempFile(String parentFolder, String filename, String extension, boolean create)
		throws IOException
	{
		extension = extension.replace(".", "");

		// Use the database name here as it's going to be unique per instance and usually path-safe
		String path = PropertyWatcher.get(ServerProperty.DATABASE_NAME);
		File folder = new File(System.getProperty("java.io.tmpdir"), path);
		folder.mkdirs();

		if (!StringUtils.isEmpty(parentFolder))
		{
			folder = new File(folder, parentFolder);
			folder.mkdirs();
		}

		File file;
		do
		{
			file = new File(folder, filename + "-" + UUID.randomUUID() + "." + extension);
		} while (file.exists());

		if (create)
			file.createNewFile();

		return file;
	}

	/**
	 * Checks, whether the child directory is a subdirectory of the base
	 * directory.
	 *
	 * @param base  the base directory.
	 * @param child the suspected child directory.
	 * @return true, if the child is a subdirectory of the base directory.
	 * @throws IOException if an IOError occured during the test.
	 */
	public static boolean isSubDirectory(File base, File child)
	{
		try
		{
			base = base.getCanonicalFile();
			child = child.getCanonicalFile();
		}
		catch (IOException e)
		{
			return false;
		}

		File parentFile = child;
		while (parentFile != null)
		{
			if (base.equals(parentFile))
			{
				return true;
			}
			parentFile = parentFile.getParentFile();
		}
		return false;
	}
}
