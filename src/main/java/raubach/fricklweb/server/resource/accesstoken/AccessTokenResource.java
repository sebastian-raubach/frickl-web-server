package raubach.fricklweb.server.resource.accesstoken;

import org.jooq.DSLContext;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.database.tables.pojos.AlbumAccessToken;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

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
	public boolean deleteAccessToken(@PathParam("tokenId") Integer tokenId, AlbumAccessToken token)
		throws IOException, SQLException
	{
		if (tokenId == null || token == null || token.getTokenId() == null || !Objects.equals(tokenId, token.getTokenId()))
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return false;
		}

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			return context.deleteFrom(ACCESS_TOKENS)
						  .where(ACCESS_TOKENS.ID.eq(tokenId))
						  .execute() > 0;
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<AlbumAccessToken> getAccessTokens()
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
			return context.selectFrom(ALBUM_ACCESS_TOKEN)
						  .limit(pageSize)
						  .offset(pageSize * currentPage)
						  .fetch()
						  .into(AlbumAccessToken.class);
		}
	}

	@Path("/count")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public int getAccessTokenCount()
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return 0;
		}

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			return context.selectCount()
						  .from(ALBUM_ACCESS_TOKEN)
						  .limit(pageSize)
						  .offset(pageSize * currentPage)
						  .fetchAny(0, int.class);
		}
	}
}
