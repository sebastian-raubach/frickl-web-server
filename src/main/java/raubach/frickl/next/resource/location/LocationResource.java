package raubach.frickl.next.resource.location;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.Database;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.tables.pojos.LatLngs;
import raubach.frickl.next.resource.ContextResource;
import raubach.frickl.next.util.*;

import java.sql.*;
import java.util.Set;

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

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			SelectJoinStep<Record> step = context.select().from(LAT_LNGS);

			// Restrict to only albums containing at least one public image
			if (Permission.IS_ADMIN.allows(userDetails.getPermissions()))
			{
				// Nothing required here, admins can see everything
			}
			else if (StringUtils.isEmpty(userDetails.getToken()))
			{
				// Check if the album contains public images
				step.where(LAT_LNGS.IS_PUBLIC.eq((byte) 1));
			}
			else
			{
				// Check user permissions for the album
				Set<Integer> albumAccess = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);
				step.where(LAT_LNGS.ALBUM_ID.in(albumAccess));
			}

			return Response.ok(step.fetchInto(LatLngs.class)).build();
		}
	}
}
