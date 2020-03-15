package raubach.fricklweb.server.resource.album;

import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;

import static raubach.fricklweb.server.database.tables.Albums.ALBUMS;
import static raubach.fricklweb.server.database.tables.Images.IMAGES;

/**
 * @author Sebastian Raubach
 */
public class AlbumCountResource extends PaginatedServerResource
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
			albumId = Integer.parseInt(getRequestAttributes().get("albumId").toString());
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

	@Get("json")
	public int getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record1<Integer>> select = DSL.using(conn, SQLDialect.MYSQL).selectCount())
		{
			SelectJoinStep<?> step = select.from(ALBUMS);


			if (albumId != null)
			{
				step.where(ALBUMS.ID.eq(albumId));
			}
			else if (parentAlbumId != null)
			{
				if (parentAlbumId != -1)
					step.where(ALBUMS.PARENT_ALBUM_ID.eq(parentAlbumId));
			}
			else
			{
				if (!auth || !StringUtils.isEmpty(user.getToken()))
				{
					step.where(ALBUMS.PARENT_ALBUM_ID.isNull());
				}
			}

			// Restrict to only albums containing at least one public image
			if (auth && StringUtils.isEmpty(user.getToken()))
				step.where(DSL.exists(DSL.selectOne()
						.from(IMAGES)
						.where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)
								.and(IMAGES.IS_PUBLIC.eq((byte) 1)))));

			return step.fetchOne(0, int.class);
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
