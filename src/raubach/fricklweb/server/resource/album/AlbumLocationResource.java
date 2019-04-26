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

import static raubach.fricklweb.server.database.tables.LatLngs.*;

/**
 * @author Sebastian Raubach
 */
public class AlbumLocationResource extends PaginatedServerResource
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
		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
			{
				return select.from(LAT_LNGS)
							 .where(LAT_LNGS.ALBUM_ID.eq(albumId))
							 .fetch()
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
