package raubach.fricklweb.server.resource.search;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.SelectWhereStep;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.Albums;
import raubach.fricklweb.server.database.tables.records.AlbumsRecord;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static raubach.fricklweb.server.database.tables.Albums.ALBUMS;
import static raubach.fricklweb.server.database.tables.Images.IMAGES;

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
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (searchTerm != null)
		{
			searchTerm = "%" + searchTerm.replace(" ", "%") + "%";
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectWhereStep<AlbumsRecord> step = context.selectFrom(ALBUMS);

				if (auth && StringUtils.isEmpty(user.getToken()))
					step.where(DSL.exists(DSL.selectOne()
							.from(IMAGES)
							.where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)
									.and(IMAGES.IS_PUBLIC.eq((byte) 1)))));

				return step.where(ALBUMS.NAME.like(searchTerm)
						.or(ALBUMS.DESCRIPTION.like(searchTerm)))
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
