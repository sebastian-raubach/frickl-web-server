package raubach.fricklweb.server.resource.search;

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
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (searchTerm != null)
		{
			searchTerm = "%" + searchTerm.replace(" ", "%") + "%";
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record1<Integer>> select = DSL.using(conn, SQLDialect.MYSQL).selectCount())
			{
				SelectJoinStep<Record1<Integer>> step = select.from(ALBUMS);

				if (auth && StringUtils.isEmpty(user.getToken()))
					step.where(DSL.exists(DSL.selectOne()
							.from(IMAGES)
							.where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)
									.and(IMAGES.IS_PUBLIC.eq((byte) 1)))));

				return step.where(ALBUMS.NAME.like(searchTerm)
						.or(ALBUMS.DESCRIPTION.like(searchTerm)))
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
