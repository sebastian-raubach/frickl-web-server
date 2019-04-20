package raubach.fricklweb.server;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import raubach.fricklweb.server.database.tables.pojos.*;

import static raubach.fricklweb.server.database.tables.Tags.*;

/**
 * @author Sebastian Raubach
 */
public class TagResource extends PaginatedServerResource
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
	public List<Tags> getJson()
	{
		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
		{
			SelectJoinStep<Record> step = select.from(TAGS);

			if (tagId != null)
				step.where(TAGS.ID.eq(tagId));

			return step.orderBy(TAGS.NAME)
					   .limit(pageSize)
					   .offset(pageSize * currentPage)
					   .fetch()
					   .into(Tags.class);
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
