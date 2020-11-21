package raubach.fricklweb.server.resource.album;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Patch;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.AlbumStats;
import raubach.fricklweb.server.database.tables.pojos.Albums;
import raubach.fricklweb.server.resource.AbstractAccessTokenResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static raubach.fricklweb.server.database.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.fricklweb.server.database.tables.AlbumStats.ALBUM_STATS;
import static raubach.fricklweb.server.database.tables.AlbumTokens.ALBUM_TOKENS;
import static raubach.fricklweb.server.database.tables.Albums.ALBUMS;
import static raubach.fricklweb.server.database.tables.Images.IMAGES;

/**
 * @author Sebastian Raubach
 */
public class AlbumResource extends AbstractAccessTokenResource
{
	public static final String PARAM_PARENT_ALBUM_ID = "parentAlbumId";

	private Integer albumId = null;
	private Integer parentAlbumId = null;

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
		try
		{
			this.parentAlbumId = Integer.parseInt(getQueryValue(PARAM_PARENT_ALBUM_ID));
		}
		catch (Exception e)
		{
		}
	}

	@Patch("json")
	public void patchJson(Albums album)
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(user.getToken()))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

		if (albumId != null && album != null && Objects.equals(album.getId(), albumId))
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				context.update(ALBUMS)
						.set(ALBUMS.BANNER_IMAGE_ID, album.getBannerImageId())
						.where(ALBUMS.ID.eq(albumId))
						.execute();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	@Get("json")
	public List<AlbumStats> getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select().from(ALBUM_STATS);


			if (albumId != null)
			{
				step.where(ALBUM_STATS.ID.eq(albumId));
			}
			else if (parentAlbumId != null)
			{
				if (parentAlbumId != -1)
					step.where(ALBUM_STATS.PARENT_ALBUM_ID.eq(parentAlbumId));
			}
			else
			{
				if (!auth || !StringUtils.isEmpty(user.getToken()))
					step.where(ALBUM_STATS.PARENT_ALBUM_ID.isNull());
			}

			// Restrict to only albums containing at least one public image
			if (!StringUtils.isEmpty(accessToken))
			{
				step.where(DSL.exists(DSL.selectOne()
						.from(ALBUM_TOKENS)
						.leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
						.where(ACCESS_TOKENS.TOKEN.eq(accessToken)
								.and(ALBUM_TOKENS.ALBUM_ID.eq(ALBUM_STATS.ID)))));
			}
			else if (StringUtils.isEmpty(user.getToken()))
			{
				step.where(DSL.exists(DSL.selectOne()
						.from(IMAGES)
						.where(IMAGES.ALBUM_ID.eq(ALBUM_STATS.ID)
								.and(IMAGES.IS_PUBLIC.eq((byte) 1)))));
			}

			return step.orderBy(ALBUM_STATS.CREATED_ON.desc(), ALBUM_STATS.NAME.desc())
					.limit(pageSize)
					.offset(pageSize * currentPage)
					.fetch()
					.into(AlbumStats.class);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
