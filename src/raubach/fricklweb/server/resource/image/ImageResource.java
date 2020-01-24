package raubach.fricklweb.server.resource.image;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.sql.Date;
import java.text.*;
import java.util.*;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.resource.*;

import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * @author Sebastian Raubach
 */
public class ImageResource extends PaginatedServerResource
{
	public static final String PARAM_DATE = "date";
	public static final String PARAM_FAV  = "fav";

	private SimpleDateFormat sdf     = new SimpleDateFormat("yyyy-MM-dd");
	private Integer          albumId = null;
	private Integer          imageId = null;

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
			imageId = Integer.parseInt(getRequestAttributes().get("imageId").toString());
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

	@Patch("json")
	public void patchJson(Images image)
	{
		if (imageId != null && image != null && Objects.equals(image.getId(), imageId))
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				context.update(IMAGES)
					   .set(IMAGES.IS_FAVORITE, image.getIsFavorite())
					   .where(IMAGES.ID.eq(imageId))
					   .execute();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	@Get("json")
	public List<Images> getJson()
	{
		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
			{
				return select.from(IMAGES)
							 .where(IMAGES.ALBUM_ID.eq(albumId))
							 .orderBy(IMAGES.CREATED_ON.desc())
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
		else if (imageId != null)
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
			{
				return select.from(IMAGES)
							 .where(IMAGES.ID.eq(imageId))
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
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
			{
				SelectJoinStep<Record> step = select.from(IMAGES);

				if (isFav != null && isFav)
					step.where(IMAGES.IS_FAVORITE.eq((byte) 1));
				if (date != null)
					step.where(DSL.date(IMAGES.CREATED_ON).eq(getDate(date)));

				return step.orderBy(IMAGES.CREATED_ON.desc())
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
	}
}
