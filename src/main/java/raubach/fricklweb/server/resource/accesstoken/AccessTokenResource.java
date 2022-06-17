package raubach.fricklweb.server.resource.accesstoken;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.DSLContext;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.database.tables.pojos.AlbumAccessToken;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

import static raubach.fricklweb.server.database.tables.AccessTokens.*;
import static raubach.fricklweb.server.database.tables.AlbumAccessToken.*;

@Path("accesstoken")
@Secured
public class AccessTokenResource extends PaginatedServerResource
{
	@DELETE
	@Path("/{tokenId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteAccessToken(@PathParam("tokenId") Integer tokenId, AlbumAccessToken token)
		throws IOException, SQLException
	{
		if (tokenId == null || token == null || token.getTokenId() == null || !Objects.equals(tokenId, token.getTokenId()))
			return Response.status(Response.Status.BAD_REQUEST).build();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			return Response.ok(context.deleteFrom(ACCESS_TOKENS)
									  .where(ACCESS_TOKENS.ID.eq(tokenId))
									  .execute() > 0)
						   .build();
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccessTokens()
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			return Response.ok(context.selectFrom(ALBUM_ACCESS_TOKEN)
									  .limit(pageSize)
									  .offset(pageSize * currentPage)
									  .fetch()
									  .into(AlbumAccessToken.class))
						   .build();
		}
	}

	@Path("/count")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccessTokenCount()
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			return Response.ok(context.selectCount()
									  .from(ALBUM_ACCESS_TOKEN)
									  .limit(pageSize)
									  .offset(pageSize * currentPage)
									  .fetchAny(0, int.class))
						   .build();
		}
	}
}
