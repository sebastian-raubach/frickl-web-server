package raubach.fricklweb.server.resource.calendar;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.database.tables.pojos.CalendarData;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.IOException;
import java.sql.*;

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
	public Response getCalendarData(@QueryParam("year") Integer year)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select().from(CALENDAR_DATA);

			if (year != null)
				step.where(DSL.year(CALENDAR_DATA.DATE).eq(year));

			return Response.ok(step.fetch()
								   .into(CalendarData.class))
						   .build();
		}
	}

	@Path("/year")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCalendarYears()
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			return Response.ok(context.selectDistinct(DSL.year(CALENDAR_DATA.DATE).as("year"))
									  .from(CALENDAR_DATA)
									  .fetch()
									  .getValues("year", Integer.class))
						   .build();
		}
	}
}
