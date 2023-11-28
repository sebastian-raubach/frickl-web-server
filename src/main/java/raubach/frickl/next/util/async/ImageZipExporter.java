package raubach.frickl.next.util.async;

import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.Database;
import raubach.frickl.next.codegen.tables.pojos.*;
import raubach.frickl.next.codegen.tables.records.ImagesRecord;

import java.io.File;
import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.*;

import static raubach.frickl.next.codegen.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.frickl.next.codegen.tables.AlbumTokens.ALBUM_TOKENS;
import static raubach.frickl.next.codegen.tables.Albums.ALBUMS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;

public class ImageZipExporter
{
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private              File             fricklBasePath;
	private              File             folder;
	private              File             zipFile;
	private              Albums           album;
	private              List<Images>     images;
	private              String           userToken;
	private              String           accessToken;

	private final Instant start;

	public ImageZipExporter()
	{
		start = Instant.now();
	}

	public static void main(String[] args)
	{
		System.setProperty("org.jooq.no-logo", "true");
		System.setProperty("org.jooq.no-tips", "true");

		ImageZipExporter exporter = new ImageZipExporter();
		Database.init(args[0], args[1], args[2], args[3], args[4], false);
		Integer albumId = Integer.parseInt(args[5]);
		exporter.fricklBasePath = new File(args[6]);
		exporter.folder = new File(args[7]);
		exporter.userToken = args[8];
		exporter.accessToken = args[9];

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			exporter.album = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(albumId)).fetchAnyInto(Albums.class);

			if (exporter.album == null)
				System.exit(1);

			exporter.zipFile = new File(exporter.folder, exporter.folder.getName() + "-" + SDF.format(new Date()) + ".zip");

			exporter.run();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void init()
			throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			// Get the images
			SelectConditionStep<ImagesRecord> step = context.selectFrom(IMAGES)
															.where(IMAGES.ALBUM_ID.eq(album.getId()));

			if (!StringUtils.isEmpty(accessToken))
			{
				step.and(DSL.exists(DSL.selectOne()
									   .from(ALBUM_TOKENS)
									   .leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
									   .where(ACCESS_TOKENS.TOKEN.eq(accessToken)
																 .and(ALBUM_TOKENS.ALBUM_ID.eq(album.getId())))));
			}
			else if (StringUtils.isEmpty(userToken))
			{
				step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
			}
			images = step.fetchInto(Images.class);
		}
	}

	private void run()
			throws IOException, DataAccessException, SQLException
	{
		init();

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