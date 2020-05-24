package raubach.fricklweb.server.resource.album;

import org.jooq.DSLContext;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.computed.AccessToken;
import raubach.fricklweb.server.database.tables.records.AccessTokensRecord;
import raubach.fricklweb.server.database.tables.records.AlbumTokensRecord;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static raubach.fricklweb.server.database.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.fricklweb.server.database.tables.AlbumTokens.ALBUM_TOKENS;

/**
 * @author Sebastian Raubach
 */
public class AlbumAccessTokenResource extends PaginatedServerResource
{
	private Integer albumId = null;

	@Override
	protected void doInit()
			throws ResourceException
	{
		super.doInit();

		try
		{
			albumId = Integer.parseInt(getRequestAttributes().get("albumId").toString());
		}
		catch (Exception e)
		{
		}
	}

	@Post("json")
	public boolean postJson(AccessToken accessToken)
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(user.getToken()))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

		if (accessToken == null || StringUtils.isEmpty(accessToken.getToken()) || albumId == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			try {
				UUID.fromString(accessToken.getToken());
			} catch (IllegalArgumentException | NullPointerException e) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}

			AccessTokensRecord token = context.newRecord(ACCESS_TOKENS);
			token.setToken(accessToken.getToken());
			token.setExpiresOn(accessToken.getExpiresOn());
			token.store();

			AlbumTokensRecord albumToken = context.newRecord(ALBUM_TOKENS);
			albumToken.setAlbumId(albumId);
			albumToken.setAccessTokenId(token.getId());
			return albumToken.store() > 0;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
