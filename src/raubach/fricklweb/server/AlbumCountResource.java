package raubach.fricklweb.server;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;

import static raubach.fricklweb.server.database.tables.Albums.*;

/**
 * @author Sebastian Raubach
 */
public class AlbumCountResource extends PaginatedServerResource
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
		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record1<Integer>> select = DSL.using(conn, SQLDialect.MYSQL).selectCount())
		{
			SelectJoinStep<?> step = select.from(ALBUMS);

			if (albumId != null)
				step.where(ALBUMS.ID.eq(albumId));
			else if (parentAlbumId != null)
				step.where(ALBUMS.PARENT_ALBUM_ID.eq(parentAlbumId));
			else
				step.where(ALBUMS.PARENT_ALBUM_ID.isNull());

			return step.fetchOne(0, int.class);
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
