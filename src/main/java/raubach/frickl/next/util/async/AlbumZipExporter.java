package raubach.frickl.next.util.async;

import org.jooq.*;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.Database;
import raubach.frickl.next.codegen.tables.pojos.*;
import raubach.frickl.next.codegen.tables.records.ImagesRecord;

import java.io.IOException;
import java.sql.*;

import static raubach.frickl.next.codegen.tables.Albums.ALBUMS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;

public class AlbumZipExporter extends ZipExporter
{
	private Albums album;

	public AlbumZipExporter(String[] args)
	{
		super(args);
	}

	public static void main(String[] args)
	{
		AlbumZipExporter exporter = new AlbumZipExporter(args);
		Integer albumId = Integer.parseInt(args[5]);

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			exporter.album = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(albumId)).fetchAnyInto(Albums.class);

			if (exporter.album == null)
				System.exit(1);

			exporter.run();
		}
		catch (Exception e)
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
															.where(IMAGES.ALBUM_ID.eq(album.getId()));

			if (StringUtils.isEmpty(userToken))
			{
				step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
			}
			images = step.fetchInto(Images.class);
		}
	}
}