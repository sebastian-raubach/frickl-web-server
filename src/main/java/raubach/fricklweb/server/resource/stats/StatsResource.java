package raubach.fricklweb.server.resource.stats;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.computed.StatsDayHour;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.IOException;
import java.sql.*;

import static raubach.fricklweb.server.database.tables.Images.*;
import static raubach.fricklweb.server.database.tables.StatsCamera.*;

/**
 * @author Sebastian Raubach
 */
@Path("stats")
@Secured
@PermitAll
public class StatsResource extends PaginatedServerResource
{
	@GET
	@Path("dayhour")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDayHourStats()
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			Field<Short> day = DSL.field("weekday(?)", Short.class, IMAGES.CREATED_ON).as("day");
			Field<?> hour = DSL.hour(IMAGES.CREATED_ON).as("hour");

			return Response.ok(context.select(
										  day,
										  hour,
										  DSL.count()
									  )
									  .from(IMAGES)
									  .where(IMAGES.CREATED_ON.isNotNull())
									  .groupBy(day, hour)
									  .orderBy(day, hour)
									  .fetchInto(StatsDayHour.class))
						   .build();
		}
	}

	@GET
	@Path("camera")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCameraStats()
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			return Response.ok(context.select().from(STATS_CAMERA)
									  .limit(15)
									  .fetch()
									  .into(StatsCamera.class))
						   .build();
		}
	}
}
