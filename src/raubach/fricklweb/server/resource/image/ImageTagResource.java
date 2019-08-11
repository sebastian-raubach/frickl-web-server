package raubach.fricklweb.server.resource.image;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.database.tables.records.TagsRecord;
import raubach.fricklweb.server.resource.*;
import raubach.fricklweb.server.util.*;

import static raubach.fricklweb.server.database.tables.ImageTags.*;
import static raubach.fricklweb.server.database.tables.Images.*;
import static raubach.fricklweb.server.database.tables.Tags.*;

/**
 * @author Sebastian Raubach
 */
public class ImageTagResource extends PaginatedServerResource
{
	private Integer imageId = null;

	@Override
	protected void doInit()
		throws ResourceException
	{
		super.doInit();

		try
		{
			imageId = Integer.parseInt(getRequestAttributes().get("imageId").toString());
		}
		catch (Exception e)
		{
		}
	}

	@Post("json")
	public boolean addTagJson(Tags tag)
	{
		if (tag != null && imageId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				Images image = context.selectFrom(IMAGES)
						.where(IMAGES.ID.eq(imageId))
						.fetchOneInto(Images.class);

				TagsRecord t = context.selectFrom(TAGS)
						.where(TAGS.NAME.eq(tag.getName()))
						.fetchOneInto(TagsRecord.class);

				if (t == null) {
					tag.setCreatedOn(new Timestamp(System.currentTimeMillis()));
					t = context.newRecord(TAGS, tag);
					t.store();
				}

				if (image == null)
					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

				int numberOfInsertedItems = context.insertInto(IMAGE_TAGS, IMAGE_TAGS.IMAGE_ID, IMAGE_TAGS.TAG_ID)
												   .values(imageId, t.getId())
												   .onDuplicateKeyIgnore()
												   .execute();

				File file = new File(Frickl.BASE_PATH, image.getPath());
				try
				{
					TagUtils.addTagToImage(file, Collections.singletonList(tag.getName()));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				return numberOfInsertedItems == 1;
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

	@Delete("json")
	public boolean removeTagJson(Tags tag)
	{
		if (imageId != null && tag != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				Images image = context.selectFrom(IMAGES)
						.where(IMAGES.ID.eq(imageId))
						.fetchOneInto(Images.class);

				Tags t = context.selectFrom(TAGS)
						.where(TAGS.ID.eq(tag.getId()))
						.fetchOneInto(Tags.class);

				if (t == null || image == null)
					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

				int numberOfDeletedItems = context.deleteFrom(IMAGE_TAGS)
												   .where(IMAGE_TAGS.IMAGE_ID.eq(image.getId()))
												   .and(IMAGE_TAGS.TAG_ID.eq(tag.getId()))
												   .execute();

				File file = new File(Frickl.BASE_PATH, image.getPath());
				try
				{
					TagUtils.deleteTagFromImage(file, Collections.singletonList(tag.getName()));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				// TODO: Delete all tags that have no more images associated with them

				return numberOfDeletedItems == 1;
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
		if (imageId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				return context.select(TAGS.ID, TAGS.NAME, TAGS.CREATED_ON, TAGS.UPDATED_ON)
							  .from(TAGS
								  .leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
								  .leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID)))
							  .where(IMAGES.ID.eq(imageId))
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
