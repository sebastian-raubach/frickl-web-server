package raubach.fricklweb.server.resource.tag;

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
import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;

import static raubach.fricklweb.server.database.tables.ImageTags.IMAGE_TAGS;
import static raubach.fricklweb.server.database.tables.Images.IMAGES;
import static raubach.fricklweb.server.database.tables.Tags.TAGS;

/**
 * @author Sebastian Raubach
 */
public class TagImageCountResource extends PaginatedServerResource
{
	private Integer tagId = null;

	@Override
	protected void doInit()
			throws ResourceException
	{
		super.doInit();

		try
		{
			tagId = Integer.parseInt(getRequestAttributes().get("tagId").toString());
		}
		catch (Exception e)
		{
		}
	}

	@Get("json")
	public int getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.getBoolean(ServerProperty.AUTHENTICATION_ENABLED);

		if (tagId != null)
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record1<Integer>> select = DSL.using(conn, SQLDialect.MYSQL).selectCount())
			{
				SelectJoinStep<Record1<Integer>> step = select.from(TAGS
						.leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
						.leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID)));

				if (auth && StringUtils.isEmpty(user.getToken()))
					step.where(IMAGES.IS_PUBLIC.eq((byte) 1));

				return step.where(TAGS.ID.eq(tagId))
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
