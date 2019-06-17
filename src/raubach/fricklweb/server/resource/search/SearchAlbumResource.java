package raubach.fricklweb.server.resource.search;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.database.tables.pojos.Albums;
import raubach.fricklweb.server.resource.PaginatedServerResource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static raubach.fricklweb.server.database.tables.Albums.ALBUMS;

/**
 * @author Sebastian Raubach
 */
public class SearchAlbumResource extends PaginatedServerResource
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
	public List<Albums> getJson()
	{
		if (searchTerm != null)
		{
			searchTerm = "%" + searchTerm.replace(" ", "%") + "%";
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				return context.selectFrom(ALBUMS)
						.where(ALBUMS.NAME.like(searchTerm))
						.or(ALBUMS.DESCRIPTION.like(searchTerm))
						.offset(pageSize * currentPage)
						.limit(pageSize)
						.fetch()
						.into(Albums.class);
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
