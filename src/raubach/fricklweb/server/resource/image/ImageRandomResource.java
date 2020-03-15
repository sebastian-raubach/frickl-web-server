package raubach.fricklweb.server.resource.image;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.Images;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;

import static raubach.fricklweb.server.database.tables.Images.IMAGES;

/**
 * @author Sebastian Raubach
 */
public class ImageRandomResource extends PaginatedServerResource
{
	@Get("json")
	public Images getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.getBoolean(ServerProperty.AUTHENTICATION_ENABLED);

		Images result = null;
		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
		{

			SelectConditionStep<Record> step = select.from(IMAGES)
					.where(IMAGES.IS_FAVORITE.eq((byte) 1));

			if (auth && StringUtils.isEmpty(user.getToken()))
				step.and(IMAGES.IS_PUBLIC.eq((byte) 1));

			result = step.orderBy(DSL.rand())
					.limit(1)
					.fetchOne()
					.into(Images.class);
		}
		catch (SQLException | NullPointerException e)
		{
			e.printStackTrace();
		}

		if (result == null)
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
			{
				SelectJoinStep<Record> step = select.from(IMAGES);

				if (auth && StringUtils.isEmpty(user.getToken()))
					step.where(IMAGES.IS_PUBLIC.eq((byte) 1));


				result = step.orderBy(DSL.rand())
						.limit(1)
						.fetchOne()
						.into(Images.class);
			}
			catch (SQLException e)
			{
				e.printStackTrace();

				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
		}

		return result;
	}
}
