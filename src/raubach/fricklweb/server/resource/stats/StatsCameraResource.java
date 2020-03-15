package raubach.fricklweb.server.resource.stats;

import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.StatsCamera;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static raubach.fricklweb.server.database.tables.StatsCamera.STATS_CAMERA;

/**
 * @author Sebastian Raubach
 */
public class StatsCameraResource extends PaginatedServerResource
{
	@Get("json")
	public List<StatsCamera> getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.getBoolean(ServerProperty.AUTHENTICATION_ENABLED);

		if (auth && StringUtils.isEmpty(user.getToken()))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
		{
			return select.from(STATS_CAMERA)
					.limit(10)
					.fetch()
					.into(StatsCamera.class);
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
