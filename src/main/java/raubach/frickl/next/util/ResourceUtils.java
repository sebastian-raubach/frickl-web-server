package raubach.frickl.next.util;

import org.jooq.tools.StringUtils;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.*;
import java.net.*;
import java.util.UUID;

public class ResourceUtils
{
	/**
	 * Returns the location of the project's lib filter as a {@link File}
	 *
	 * @return The location of the project's lib filter as a {@link File}
	 * @throws URISyntaxException Thrown if the URI of the folder location is invalid
	 */
	public static File getLibFolder()
			throws URISyntaxException
	{
		URL resource = PropertyWatcher.class.getClassLoader().getResource("logging.properties");
		if (resource != null)
		{
			File file = new File(resource.toURI());
			return new File(file.getParentFile().getParentFile(), "lib");
		}

		return null;
	}

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
