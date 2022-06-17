package raubach.fricklweb.server.resource.location;

import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.database.tables.pojos.LatLngs;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;

import java.sql.*;
import java.util.List;

import static raubach.fricklweb.server.database.tables.LatLngs.*;

/**
 * @author Sebastian Raubach
 */
@Path("location")
@Secured
@PermitAll
public class LocationResource extends PaginatedServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocations()
		throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select().from(LAT_LNGS);

			if (auth && StringUtils.isEmpty(userDetails.getToken()))
				step.where(LAT_LNGS.IS_PUBLIC.eq((byte) 1));

			return Response.ok(step.fetchInto(LatLngs.class)).build();
		}
	}
}
