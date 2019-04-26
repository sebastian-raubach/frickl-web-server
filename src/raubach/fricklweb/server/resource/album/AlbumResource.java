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

	@Get("json")
	public List<Albums> getJson()
	{
		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
		{
			SelectJoinStep<Record> step = select.from(ALBUMS);

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
				step.where(ALBUMS.PARENT_ALBUM_ID.isNull());
			}

			return step.orderBy(ALBUMS.CREATED_ON.desc(), ALBUMS.NAME.desc())
					   .limit(pageSize)
					   .offset(pageSize * currentPage)
					   .fetch()
					   .into(Albums.class);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
