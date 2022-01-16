package raubach.fricklweb.server.resource.stats;

import org.jooq.DSLContext;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.database.tables.pojos.StatsCamera;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.sql.*;
import java.util.List;

import static raubach.fricklweb.server.database.tables.StatsCamera.*;

/**
 * @author Sebastian Raubach
 */
@Path("stats/camera")
@Secured
@PermitAll
public class StatsCameraResource extends PaginatedServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<StatsCamera> getJson()
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
			return context.select().from(STATS_CAMERA)
						  .limit(15)
						  .fetch()
						  .into(StatsCamera.class);
		}
	}
}
