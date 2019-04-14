package raubach.fricklweb.server;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import raubach.fricklweb.server.database.tables.pojos.*;

import static raubach.fricklweb.server.database.tables.StatsCamera.*;

/**
 * @author Sebastian Raubach
 */
public class StatsCameraResource extends PaginatedServerResource
{
	@Get("json")
	public List<StatsCamera> getJson()
	{
		try (SelectSelectStep<Record> select = Database.context().select())
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
