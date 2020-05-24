package raubach.fricklweb.server.resource.image;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.Images;
import raubach.fricklweb.server.resource.AbstractAccessTokenResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;

import static raubach.fricklweb.server.database.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.fricklweb.server.database.tables.AlbumTokens.ALBUM_TOKENS;
import static raubach.fricklweb.server.database.tables.Images.IMAGES;

/**
 * @author Sebastian Raubach
 */
public class ImageRandomResource extends AbstractAccessTokenResource
{
	@Get("json")
	public Images getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		Images result = null;
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectConditionStep<Record> step = context.select().from(IMAGES)
					.where(IMAGES.IS_FAVORITE.eq((byte) 1));

			if (auth)
			{
				if (!StringUtils.isEmpty(accessToken))
				{
					step.and(DSL.exists(DSL.selectOne()
							.from(ALBUM_TOKENS)
							.leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
							.where(ACCESS_TOKENS.TOKEN.eq(accessToken)
									.and(ALBUM_TOKENS.ALBUM_ID.eq(IMAGES.ALBUM_ID)))));
				} else if (StringUtils.isEmpty(user.getToken())) {
					step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
				}
			}

			result = step.orderBy(DSL.rand())
					.limit(1)
					.fetchAny()
					.into(Images.class);
		}
		catch (SQLException | NullPointerException e)
		{
			e.printStackTrace();
		}

		if (result == null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectJoinStep<Record> step = context.select().from(IMAGES);

				if (auth)
				{
					if (!StringUtils.isEmpty(accessToken))
					{
						step.where(DSL.exists(DSL.selectOne()
								.from(ALBUM_TOKENS)
								.leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
								.where(ACCESS_TOKENS.TOKEN.eq(accessToken)
										.and(ALBUM_TOKENS.ALBUM_ID.eq(IMAGES.ALBUM_ID)))));
					} else if (StringUtils.isEmpty(user.getToken())) {
						step.where(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				result = step.orderBy(DSL.rand())
						.limit(1)
						.fetchAny()
						.into(Images.class);
			}
			catch (SQLException e)
			{
				e.printStackTrace();

				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
		}

		return result;
	}
}
