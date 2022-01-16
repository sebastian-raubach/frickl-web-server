package raubach.fricklweb.server.resource.calendar;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.database.tables.pojos.CalendarData;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.sql.*;
import java.util.List;

import static raubach.fricklweb.server.database.tables.CalendarData.*;

/**
 * @author Sebastian Raubach
 */
@Path("calendar")
@Secured
@PermitAll
public class CalendarResource extends PaginatedServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<CalendarData> getCalendarData(@QueryParam("year") Integer year)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return null;
		}

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select().from(CALENDAR_DATA);

			if (year != null)
				step.where(DSL.year(CALENDAR_DATA.DATE).eq(year));

			return step.fetch()
					   .into(CalendarData.class);
		}
	}

	@Path("/year")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Integer> getCalendarYears()
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return null;
		}

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			return context.selectDistinct(DSL.year(CALENDAR_DATA.DATE).as("year"))
						  .from(CALENDAR_DATA)
						  .fetch()
						  .getValues("year", Integer.class);
		}
	}
}
