package raubach.frickl.next.util.task;

import org.apache.commons.io.FileUtils;
import raubach.frickl.next.util.ServerProperty;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.*;
import java.util.Arrays;
import java.util.logging.*;

/**
 * @author Sebastian Raubach
 */
public class AlbumDownloadFolderCleanupTask implements Runnable
{
	@Override
	public void run()
	{
		String version = PropertyWatcher.get(ServerProperty.API_VERSION);
		File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-exports" + "-" + version);

		File[] subFolders = folder.listFiles(File::isDirectory);

		Logger.getLogger("").log(Level.INFO, "Running AlbumDownloadFolderCleanupTask");

		if (subFolders != null)
		{
			for (File f : subFolders)
			{
				Long timestamp = getLastModifiedForFolder(f);

				if (timestamp != null && (System.currentTimeMillis() - timestamp) > 604_800_000)
				{
					try
					{
						FileUtils.forceDelete(f);
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private Long getLastModifiedForFolder(File folder)
	{
		if (folder == null)
			return null;

		long result = folder.lastModified();
		File[] files = folder.listFiles();

		if (files != null)
		{
			return Arrays.stream(files)
						 .mapToLong(File::lastModified)
						 .max()
						 .orElse(result);
		}
		else
		{
			return result;
		}
	}
}
