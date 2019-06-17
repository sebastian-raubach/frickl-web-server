package raubach.fricklweb.server.resource.search;

import org.jooq.Record1;
import org.jooq.SQLDialect;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.resource.PaginatedServerResource;

import java.sql.Connection;
import java.sql.SQLException;

import static raubach.fricklweb.server.database.tables.Albums.ALBUMS;

/**
 * @author Sebastian Raubach
 */
public class SearchAlbumCountResource extends PaginatedServerResource
{
	private String searchTerm = null;

	@Override
	protected void doInit()
		throws ResourceException
	{
		super.doInit();

		searchTerm = getRequestAttributeAsString("searchTerm");
	}

	@Get("json")
	public int getJson()
	{
		if (searchTerm != null)
		{
			searchTerm = "%" + searchTerm.replace(" ", "%") + "%";
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record1<Integer>> select = DSL.using(conn, SQLDialect.MYSQL).selectCount())
			{
				return select.from(ALBUMS)
						.where(ALBUMS.NAME.like(searchTerm))
						.or(ALBUMS.DESCRIPTION.like(searchTerm))
						.fetchOne(0, int.class);
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
