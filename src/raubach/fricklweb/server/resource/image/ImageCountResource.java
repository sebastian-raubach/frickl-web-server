package raubach.fricklweb.server.resource.image;

import org.jooq.*;
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
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import static raubach.fricklweb.server.database.tables.Images.IMAGES;

/**
 * @author Sebastian Raubach
 */
public class ImageCountResource extends PaginatedServerResource
{
	public static final String PARAM_DATE = "date";
	public static final String PARAM_FAV = "fav";

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private Integer albumId = null;

	private String date;
	private Boolean isFav;

	@Override
	protected void doInit()
			throws ResourceException
	{
		super.doInit();

		try
		{
			albumId = Integer.parseInt(getRequestAttributes().get("albumId").toString());
		}
		catch (Exception e)
		{
		}
		try
		{
			date = getQueryValue(PARAM_DATE);
		}
		catch (Exception e)
		{
		}
		try
		{
			isFav = Boolean.parseBoolean(getQueryValue(PARAM_FAV));
		}
		catch (Exception e)
		{
		}
	}

	private synchronized Date getDate(String date)
	{
		try
		{
			return new Date(sdf.parse(date).getTime());
		}
		catch (Exception e)
		{
			return null;
		}
	}

	@Get("json")
	public int getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record1<Integer>> select = DSL.using(conn, SQLDialect.MYSQL).selectCount())
			{
				SelectConditionStep<Record1<Integer>> step = select.from(IMAGES)
						.where(IMAGES.ALBUM_ID.eq(albumId));

				if (auth && StringUtils.isEmpty(user.getToken()))
					step.and(IMAGES.IS_PUBLIC.eq((byte) 1));

				return step.fetchAny(0, int.class);
			}
			catch (SQLException e)
			{
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record1<Integer>> select = DSL.using(conn, SQLDialect.MYSQL).selectCount())
			{
				SelectJoinStep<Record1<Integer>> step = select.from(IMAGES);

				if (isFav != null && isFav)
					step.where(IMAGES.IS_FAVORITE.eq((byte) 1));
				if (date != null)
					step.where(DSL.date(IMAGES.CREATED_ON).eq(getDate(date)));
				if (auth && StringUtils.isEmpty(user.getToken()))
					step.where(IMAGES.IS_PUBLIC.eq((byte) 1));

				return step.fetchAny(0, int.class);
			}
			catch (SQLException e)
			{
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
	}
}
