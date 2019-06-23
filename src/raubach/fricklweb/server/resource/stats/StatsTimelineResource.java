package raubach.fricklweb.server.resource.stats;

import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.database.tables.pojos.ImageTimeline;
import raubach.fricklweb.server.resource.PaginatedServerResource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static raubach.fricklweb.server.database.tables.ImageTimeline.IMAGE_TIMELINE;

/**
 * @author Sebastian Raubach
 */
public class StatsTimelineResource extends PaginatedServerResource
{
	@Get("json")
	public List<ImageTimeline> getJson()
	{
		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
		{
			return select.from(IMAGE_TIMELINE)
						 .limit(10)
						 .fetch()
						 .into(ImageTimeline.class);
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
