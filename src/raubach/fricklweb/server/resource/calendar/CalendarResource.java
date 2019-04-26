package raubach.fricklweb.server.resource.calendar;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.resource.*;

import static raubach.fricklweb.server.database.tables.CalendarData.*;

/**
 * @author Sebastian Raubach
 */
public class CalendarResource extends PaginatedServerResource
{
	public static final String PARAM_YEAR = "year";

	private Integer year = null;

	@Override
	protected void doInit()
		throws ResourceException
	{
		super.doInit();

		try
		{
			this.year = Integer.parseInt(getQueryValue(PARAM_YEAR));
		}
		catch (Exception e)
		{
		}
	}

	@Get("json")
	public List<CalendarData> getJson()
	{
		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
		{
			SelectJoinStep<Record> step = select.from(CALENDAR_DATA);

			if (year != null)
				step.where(DSL.year(CALENDAR_DATA.DATE).eq(year));

			return step.fetch()
					   .into(CalendarData.class);
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
