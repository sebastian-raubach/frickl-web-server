package raubach.fricklweb.server.resource.calendar;

import org.jooq.Record;
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
import raubach.fricklweb.server.database.tables.pojos.CalendarData;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static raubach.fricklweb.server.database.tables.CalendarData.CALENDAR_DATA;

/**
 * @author Sebastian Raubach
 */
public class CalendarResource extends PaginatedServerResource
{
	public static final String PARAM_YEAR = "year";

	private Integer year = null;

	@Override
	protected void doInit()
			throws ResourceException
	{
		super.doInit();

		try
		{
			this.year = Integer.parseInt(getQueryValue(PARAM_YEAR));
		}
		catch (Exception e)
		{
		}
	}

	@Get("json")
	public List<CalendarData> getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.getBoolean(ServerProperty.AUTHENTICATION_ENABLED);

		if (auth && StringUtils.isEmpty(user.getToken()))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
		{
			SelectJoinStep<Record> step = select.from(CALENDAR_DATA);

			if (year != null)
				step.where(DSL.year(CALENDAR_DATA.DATE).eq(year));

			return step.fetch()
					.into(CalendarData.class);
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
