package raubach.fricklweb.server;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import static raubach.fricklweb.server.database.tables.CalendarData.*;

/**
 * @author Sebastian Raubach
 */
public class CalendarYearResource extends PaginatedServerResource
{
	@Get("json")
	public List<Integer> getJson()
	{
		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record1<Integer>> select = DSL.using(conn, SQLDialect.MYSQL).selectDistinct(DSL.year(CALENDAR_DATA.DATE).as("year")))
		{
			return select.from(CALENDAR_DATA)
						 .fetch()
						 .getValues("year", Integer.class);
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
