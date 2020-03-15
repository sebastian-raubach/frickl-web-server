package raubach.fricklweb.server.resource.search;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.Images;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static raubach.fricklweb.server.database.tables.ImageTags.IMAGE_TAGS;
import static raubach.fricklweb.server.database.tables.Images.IMAGES;
import static raubach.fricklweb.server.database.tables.Tags.TAGS;

/**
 * @author Sebastian Raubach
 */
public class SearchImageResource extends PaginatedServerResource
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
	public List<Images> getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (searchTerm != null)
		{
			searchTerm = "%" + searchTerm.replace(" ", "%") + "%";
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				SelectOnConditionStep<Record> step = context.select(IMAGES.asterisk()).from(IMAGES)
						.leftJoin(IMAGE_TAGS).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID))
						.leftJoin(TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID));

				if (auth && StringUtils.isEmpty(user.getToken()))
					step.where(IMAGES.IS_PUBLIC.eq((byte) 1));

				return step.where(TAGS.NAME.like(searchTerm)
						.or(IMAGES.PATH.like(searchTerm)))
						.groupBy(IMAGES.ID)
						.offset(pageSize * currentPage)
						.limit(pageSize)
						.fetch()
						.into(Images.class);
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
