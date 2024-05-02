package raubach.frickl.next.util.async;

import org.jooq.*;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.Database;
import raubach.frickl.next.codegen.tables.pojos.Images;
import raubach.frickl.next.codegen.tables.records.ImagesRecord;
import raubach.frickl.next.util.CollectionUtils;

import java.io.File;
import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static raubach.frickl.next.codegen.tables.Images.IMAGES;

public class ImageZipExporter extends ZipExporter
{
	private List<Integer> ids;

	public ImageZipExporter(String[] args)
	{
		super(args);
	}

	public static void main(String[] args)
	{
		ImageZipExporter exporter = new ImageZipExporter(args);
		File idFile = new File(args[5]);

		try
		{
			exporter.ids = Arrays.stream(Files.readString(idFile.toPath()).split(",")).map(Integer::parseInt).collect(Collectors.toList());

			if (CollectionUtils.isEmpty(exporter.ids))
				System.exit(1);

			exporter.run();
		}
		catch (IOException | SQLException e)
		{
			e.printStackTrace();
		}
	}

	protected void init()
			throws IOException, SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			// Get the images
			SelectConditionStep<ImagesRecord> step = context.selectFrom(IMAGES)
															.where(IMAGES.ID.in(ids));
			if (StringUtils.isEmpty(userToken))
			{
				step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
			}
			images = step.fetchInto(Images.class);
		}
	}
}