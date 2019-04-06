package raubach.fricklweb.server;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import raubach.fricklweb.server.database.tables.pojos.*;

import static raubach.fricklweb.server.database.tables.Albums.*;

/**
 * @author Sebastian Raubach
 */
public class AlbumResource extends PaginatedServerResource
{
	public static final String PARAM_PARENT_ALBUM_ID = "parentAlbumId";

	private Integer parentAlbumId = null;

	@Override
	protected void doInit()
		throws ResourceException
	{
		super.doInit();

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
		try (SelectSelectStep<Record> select = Database.select())
		{
			SelectJoinStep<Record> step = select.from(ALBUMS);

			if (parentAlbumId != null)
				step.where(ALBUMS.PARENT_ALBUM_ID.eq(parentAlbumId));

			return step.limit(pageSize)
					   .offset(pageSize * currentPage)
					   .fetch()
					   .into(Albums.class);
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
