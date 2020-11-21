package raubach.fricklweb.server.resource.location;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.LatLngs;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static raubach.fricklweb.server.database.tables.LatLngs.LAT_LNGS;

/**
 * @author Sebastian Raubach
 */
public class LocationResource extends PaginatedServerResource
{
	@Get("json")
	public List<LatLngs> getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select().from(LAT_LNGS);

			if (auth && StringUtils.isEmpty(user.getToken()))
				step.where(LAT_LNGS.IS_PUBLIC.eq((byte) 1));

			return step.fetchInto(LatLngs.class);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
