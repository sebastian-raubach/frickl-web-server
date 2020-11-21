package raubach.fricklweb.server.resource.album;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.LatLngs;
import raubach.fricklweb.server.resource.AbstractAccessTokenResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static raubach.fricklweb.server.database.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.fricklweb.server.database.tables.AlbumTokens.ALBUM_TOKENS;
import static raubach.fricklweb.server.database.tables.Images.IMAGES;
import static raubach.fricklweb.server.database.tables.LatLngs.LAT_LNGS;

/**
 * @author Sebastian Raubach
 */
public class AlbumLocationResource extends AbstractAccessTokenResource
{
	private Integer albumId = null;

	@Override
	protected void doInit()
			throws ResourceException
	{
		super.doInit();

		try
		{
			this.albumId = Integer.parseInt(getRequestAttributes().get("albumId").toString());
		}
		catch (Exception e)
		{
		}
	}

	@Get("json")
	public List<LatLngs> getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectJoinStep<Record> step = context.select().from(LAT_LNGS);

				if (auth)
				{
					if (!StringUtils.isEmpty(accessToken))
					{
						step.where(DSL.exists(DSL.selectOne()
								.from(ALBUM_TOKENS)
								.leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
								.where(ACCESS_TOKENS.TOKEN.eq(accessToken)
										.and(ALBUM_TOKENS.ALBUM_ID.eq(LAT_LNGS.ALBUM_ID)))));
					}
					else if (StringUtils.isEmpty(user.getToken()))
					{
						step.leftJoin(IMAGES).on(IMAGES.ID.eq(LAT_LNGS.ID))
								.where(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				step.where(LAT_LNGS.ALBUM_ID.eq(albumId));

				return step.fetch()
						.into(LatLngs.class);
			}
			catch (SQLException e)
			{
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
}
