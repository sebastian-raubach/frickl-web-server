package raubach.fricklweb.server;

import org.jooq.*;
import org.restlet.data.Status;
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

	@Get("json")
	public List<Tags> getJson()
	{
		if (imageId != null)
		{
			try (SelectSelectStep<Record> select = Database.select())
			{
				return select.from(IMAGES
					.leftJoin(IMAGE_TAGS).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID))
					.leftJoin(TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID)))
							 .where(IMAGES.ID.eq(imageId))
							 .offset(pageSize * currentPage)
							 .fetch()
							 .into(Tags.class);
			}
			catch (SQLException e)
			{
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
}
