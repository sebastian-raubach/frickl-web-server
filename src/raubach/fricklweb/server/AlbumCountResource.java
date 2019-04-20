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

	@Get("json")
	public int getJson()
	{
		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record1<Integer>> select = DSL.using(conn, SQLDialect.MYSQL).selectCount())
			{
				return select.from(ALBUMS)
							 .where(ALBUMS.ID.eq(albumId))
							 .fetchOne(0, int.class);
			}
			catch (SQLException e)
			{
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record1<Integer>> select = DSL.using(conn, SQLDialect.MYSQL).selectCount())
			{
				return select.from(ALBUMS)
							 .fetchOne(0, int.class);
			}
			catch (SQLException e)
			{
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
	}
}
