package raubach.fricklweb.server;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import raubach.fricklweb.server.computed.*;
import raubach.fricklweb.server.database.tables.pojos.*;

import static raubach.fricklweb.server.database.tables.ImageTags.*;
import static raubach.fricklweb.server.database.tables.Images.*;
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
	public List<TagCount> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
		{
			SelectJoinStep<Record> step = context.select(TAGS.asterisk(), DSL.count().as("count"))
												 .from(TAGS.leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
														   .leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID)));

			if (tagId != null)
				step.where(TAGS.ID.eq(tagId));

			List<TagCount> tagCounts = new ArrayList<>();
			step.groupBy(TAGS.ID)
				.orderBy(TAGS.NAME)
				.limit(pageSize)
				.offset(pageSize * currentPage)
				.fetchStream()
				.forEach(t -> {
					Tags tag = new Tags(t.get(TAGS.ID), t.get(TAGS.NAME), t.get(TAGS.CREATED_ON), t.get(TAGS.UPDATED_ON));
					Integer count = t.get("count", Integer.class);

					tagCounts.add(new TagCount(tag, count));
				});

			return tagCounts;
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
