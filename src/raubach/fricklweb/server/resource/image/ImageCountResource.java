package raubach.fricklweb.server.resource.image;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.text.*;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.resource.*;

import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * @author Sebastian Raubach
 */
public class ImageCountResource extends PaginatedServerResource
{
	public static final String PARAM_DATE = "date";
	public static final String PARAM_FAV  = "fav";

	private SimpleDateFormat sdf     = new SimpleDateFormat("yyyy-MM-dd");
	private Integer          albumId = null;

	private String  date;
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
		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record1<Integer>> select = DSL.using(conn, SQLDialect.MYSQL).selectCount())
			{
				return select.from(IMAGES)
							 .where(IMAGES.ALBUM_ID.eq(albumId))
							 .fetchOne(0, int.class);
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

				return step.fetchOne(0, int.class);
			}
			catch (SQLException e)
			{
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
	}
}
