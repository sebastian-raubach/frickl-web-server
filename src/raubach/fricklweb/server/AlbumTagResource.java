package raubach.fricklweb.server;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.util.*;

import static raubach.fricklweb.server.database.tables.Albums.*;
import static raubach.fricklweb.server.database.tables.ImageTags.*;
import static raubach.fricklweb.server.database.tables.Images.*;
import static raubach.fricklweb.server.database.tables.Tags.*;

/**
 * @author Sebastian Raubach
 */
public class AlbumTagResource extends PaginatedServerResource
{
	private Integer albumId = null;

	@Override
	protected void doInit()
		throws ResourceException
	{
		super.doInit();

		try
		{
			albumId = Integer.parseInt(getRequestAttributes().get("albumId").toString());
		}
		catch (Exception e)
		{
		}
	}

	@Put("json")
	public void putJson(List<Tags> tags)
	{
		if (albumId != null && tags != null && tags.size() > 0)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				List<Images> images = context.selectFrom(IMAGES)
											 .where(IMAGES.ALBUM_ID.eq(albumId))
											 .fetchInto(Images.class);

				List<String> tagStrings = tags.stream()
											  .map(Tags::getName)
											  .collect(Collectors.toList());

				for (Images image : images)
				{
					// Add to the database
					for (Tags tag : tags)
					{
						context.insertInto(IMAGE_TAGS, IMAGE_TAGS.IMAGE_ID, IMAGE_TAGS.TAG_ID)
							   .values(image.getId(), tag.getId())
							   .onDuplicateKeyIgnore()
							   .execute();
					}

					File file = new File(Frickl.BASE_PATH, image.getPath());
					try
					{
						TagUtils.addTagToImage(file, tagStrings);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	@Get("json")
	public List<Tags> getJson()
	{
		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				return context.selectDistinct(TAGS.ID, TAGS.NAME, TAGS.CREATED_ON, TAGS.UPDATED_ON)
							  .from(TAGS
								  .leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
								  .leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID))
								  .leftJoin(ALBUMS).on(ALBUMS.ID.eq(IMAGES.ALBUM_ID)))
							  .where(ALBUMS.ID.eq(albumId))
							  .orderBy(TAGS.NAME)
							  .offset(pageSize * currentPage)
							  .limit(pageSize)
							  .fetch()
							  .into(Tags.class);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
}
