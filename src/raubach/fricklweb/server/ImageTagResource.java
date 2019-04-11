package raubach.fricklweb.server;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import raubach.fricklweb.server.database.tables.pojos.*;

import static raubach.fricklweb.server.database.tables.ImageTags.*;
import static raubach.fricklweb.server.database.tables.Images.*;
import static raubach.fricklweb.server.database.tables.Tags.*;

/**
 * @author Sebastian Raubach
 */
public class ImageTagResource extends PaginatedServerResource
{
	private Integer imageId = null;
	private Integer tagId   = null;

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
		try
		{
			tagId = Integer.parseInt(getRequestAttributes().get("tagId").toString());
		}
		catch (Exception e)
		{
		}
	}

	@Put("json")
	public boolean addTagJson(ImageTags imageTag)
	{
		if (imageTag.getImageId() != null && imageTag.getTagId() != null && imageTag.getImageId() == imageId)
		{
			try (DSLContext insert = Database.context())
			{
				int numberOfInsertedItems = insert.insertInto(IMAGE_TAGS, IMAGE_TAGS.IMAGE_ID, IMAGE_TAGS.TAG_ID)
												  .values(imageTag.getImageId(), imageTag.getTagId())
												  .onDuplicateKeyIgnore()
												  .execute();

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
	public boolean removeTagJson(ImageTags imageTag)
	{
		if (imageTag.getImageId() != null && imageTag.getTagId() != null && imageTag.getImageId() == imageId)
		{
			try (DSLContext insert = Database.context())
			{
				int numberOfInsertedItems = insert.deleteFrom(IMAGE_TAGS)
												  .where(IMAGE_TAGS.IMAGE_ID.eq(imageTag.getImageId()))
												  .and(IMAGE_TAGS.TAG_ID.eq(imageTag.getTagId()))
												  .execute();

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

	@Get("json")
	public List<Tags> getJson()
	{
		if (imageId != null)
		{
			try (SelectSelectStep<Record> select = Database.context().select())
			{
				return select.from(IMAGES
					.leftJoin(IMAGE_TAGS).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID))
					.leftJoin(TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID)))
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
