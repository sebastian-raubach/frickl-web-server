package raubach.fricklweb.server.resource.accesstoken;

import org.jooq.DSLContext;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.AlbumAccessToken;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static raubach.fricklweb.server.database.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.fricklweb.server.database.tables.AlbumAccessToken.ALBUM_ACCESS_TOKEN;

public class AccessTokenResource extends PaginatedServerResource
{
	private Integer tokenId;

	@Override
	protected void doInit() throws ResourceException
	{
		super.doInit();

		try
		{
			this.tokenId = Integer.parseInt(getRequestAttributes().get("tokenId").toString());
		}
		catch (Exception e)
		{
		}
	}

	@Delete("json")
	public boolean deleteJson(AlbumAccessToken token)
	{
		if (this.tokenId == null || token == null || token.getTokenId() == null || !Objects.equals(tokenId, token.getTokenId()))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			return context.deleteFrom(ACCESS_TOKENS)
				.where(ACCESS_TOKENS.ID.eq(tokenId))
				.execute() > 0;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Get("json")
	public List<AlbumAccessToken> getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(user.getToken()))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			return context.selectFrom(ALBUM_ACCESS_TOKEN)
					.limit(pageSize)
					.offset(pageSize * currentPage)
					.fetch()
					.into(AlbumAccessToken.class);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
