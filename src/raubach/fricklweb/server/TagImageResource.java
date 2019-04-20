package raubach.fricklweb.server;

import org.jooq.*;
import org.jooq.impl.*;
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
public class TagImageResource extends PaginatedServerResource
{
	private Integer tagId = null;

	@Override
	protected void doInit()
		throws ResourceException
	{
		super.doInit();

		try
		{
			tagId = Integer.parseInt(getRequestAttributes().get("tagId").toString());
		}
		catch (Exception e)
		{
		}
	}

	@Get("json")
	public List<Images> getJson()
	{
		if (tagId != null)
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
			{
				return select.from(TAGS
					.leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
					.leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID)))
							 .where(TAGS.ID.eq(tagId))
							 .offset(pageSize * currentPage)
							 .limit(pageSize)
							 .fetch()
							 .into(Images.class);
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
