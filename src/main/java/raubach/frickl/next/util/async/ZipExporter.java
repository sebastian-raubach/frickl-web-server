package raubach.frickl.next.util.async;

import org.jooq.exception.DataAccessException;
import raubach.frickl.next.Database;
import raubach.frickl.next.codegen.tables.pojos.Images;

import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

public abstract class ZipExporter
{
	protected static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	protected final        Instant          start;
	protected              File             fricklBasePath;
	protected              File             folder;
	protected              File             zipFile;
	protected              List<Images>     images;
	protected              String           userToken;

	public ZipExporter(String[] args)
	{
		System.setProperty("org.jooq.no-logo", "true");
		System.setProperty("org.jooq.no-tips", "true");

		start = Instant.now();

		Database.init(args[0], args[1], args[2], args[3], args[4], false);
		this.fricklBasePath = new File(args[6]);
		this.folder = new File(args[7]);
		this.userToken = args[8];
	}

	protected abstract void init()
			throws IOException, SQLException;

	protected void run()
			throws IOException, DataAccessException, SQLException
	{
		init();

		zipFile = new File(folder, folder.getName() + "-" + SDF.format(new Date()) + ".zip");

		// Make sure it doesn't exist
		if (zipFile.exists())
			zipFile.delete();

		String prefix = zipFile.getAbsolutePath().replace("\\", "/");
		if (prefix.startsWith("/"))
			prefix = prefix.substring(1);
		URI uri = URI.create("jar:file:/" + prefix);
		Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		env.put("encoding", "UTF-8");

		try (FileSystem fs = FileSystems.newFileSystem(uri, env, null))
		{
			images.forEach(i -> {
				File source = new File(fricklBasePath, i.getPath());

				if (source.exists())
				{
					String targetName;
					if (i.getCreatedOn() != null)
						targetName = SDF.format(new Date(i.getCreatedOn().getTime())) + "-" + i.getName();
					else
						targetName = i.getName();
					String fileExtension = i.getName().substring(i.getName().lastIndexOf("."));
					Path target = fs.getPath("/", targetName + fileExtension);
					try
					{
						Files.createDirectories(target.getParent());
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					int counter = 1;
					while (Files.exists(target))
					{
						String tempName = targetName + "-" + (counter++) + fileExtension;
						target = fs.getPath("/", tempName);
					}

					try
					{
						Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			});
		}

		Duration duration = Duration.between(start, Instant.now());
		System.out.println("DURATION: " + duration);
	}
}
