package raubach.fricklweb.server.resource.album;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.resource.*;

import static raubach.fricklweb.server.database.tables.Albums.*;
import static raubach.fricklweb.server.database.tables.AlbumStats.*;

/**
 * @author Sebastian Raubach
 */
public class AlbumResource extends PaginatedServerResource
{
	public static final String PARAM_PARENT_ALBUM_ID = "parentAlbumId";

	private Integer albumId       = null;
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
		if (albumId != null && album != null && Objects.equals(album.getId(), albumId))
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
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
		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
		{
			SelectJoinStep<Record> step = select.from(ALBUM_STATS);

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
				step.where(ALBUM_STATS.PARENT_ALBUM_ID.isNull());
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
