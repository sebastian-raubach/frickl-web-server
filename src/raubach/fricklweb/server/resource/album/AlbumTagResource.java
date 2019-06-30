package raubach.fricklweb.server.resource.album;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.*;

import org.restlet.resource.Delete;
import raubach.fricklweb.server.*;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.database.tables.records.TagsRecord;
import raubach.fricklweb.server.resource.*;
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

	@Delete("json")
	public void deleteJson(Tags[] tags)
	{
		if (albumId != null && tags != null && tags.length > 0)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				List<Images> images = context.selectFrom(IMAGES)
						.where(IMAGES.ALBUM_ID.eq(albumId))
						.fetchInto(Images.class);

				List<Integer> tagIds = new ArrayList<>();

				for(Tags tag : tags)
					tagIds.add(tag.getId());

				List<String> tagNames = new ArrayList<>();

				for(Tags tag : tags)
					tagNames.add(tag.getName());

				if (images == null || images.size() < 1)
					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

				for(Images image : images) {
					context.deleteFrom(IMAGE_TAGS)
							.where(IMAGE_TAGS.IMAGE_ID.eq(image.getId()))
							.and(IMAGE_TAGS.TAG_ID.in(tagIds))
							.execute();

					File file = new File(Frickl.BASE_PATH, image.getPath());
					try {
						TagUtils.deleteTagFromImage(file, tagNames);
					} catch (IOException e) {
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

	@Post("json")
	public void postJson(Tags[] tags)
	{
		if (albumId != null && tags != null && tags.length > 0)
		{
			Logger.getLogger("").log(Level.INFO, tags.toString());
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				List<Images> images = context.selectFrom(IMAGES)
											 .where(IMAGES.ALBUM_ID.eq(albumId))
											 .fetchInto(Images.class);

				List<String> tagStrings = new ArrayList<>();

				for(Tags tag : tags)
					tagStrings.add(tag.getName());

				for (Tags tag : tags) {
					// If it doesn't exist, create it
					if (tag.getId() == null) {
						TagsRecord t = context.newRecord(TAGS, tag);
						t.store();
						tag.setId(t.getId());
					}
				}

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
