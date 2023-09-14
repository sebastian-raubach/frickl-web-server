package raubach.frickl.next.resource.location;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.Database;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.tables.pojos.LatLngs;
import raubach.frickl.next.resource.*;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.sql.*;

import static raubach.frickl.next.codegen.tables.LatLngs.LAT_LNGS;

@Secured
@PermitAll
@Path("location")
public class LocationResource extends ContextResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocations()
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			SelectJoinStep<Record> step = context.select().from(LAT_LNGS);

			if (auth && StringUtils.isEmpty(userDetails.getToken()))
				step.where(LAT_LNGS.IS_PUBLIC.eq((byte) 1));

			return Response.ok(step.fetchInto(LatLngs.class)).build();
		}
	}
}
