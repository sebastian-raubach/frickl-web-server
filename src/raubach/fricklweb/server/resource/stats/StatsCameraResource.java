package raubach.fricklweb.server.resource.stats;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.resource.*;

import static raubach.fricklweb.server.database.tables.StatsCamera.*;

/**
 * @author Sebastian Raubach
 */
public class StatsCameraResource extends PaginatedServerResource
{
	@Get("json")
	public List<StatsCamera> getJson()
	{
		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
		{
			return select.from(STATS_CAMERA)
						 .limit(10)
						 .fetch()
						 .into(StatsCamera.class);
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
