package raubach.fricklweb.server;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;

import raubach.fricklweb.server.database.tables.pojos.*;

import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * @author Sebastian Raubach
 */
public class ImageResource extends PaginatedServerResource
{
	private Integer albumId = null;
	private Integer imageId = null;

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
	}

	@Get("json")
	public List<Images> getJson()
	{
		if (albumId != null)
		{
			try (SelectSelectStep<Record> select = Database.context().select())
			{
				return select.from(IMAGES)
							 .where(IMAGES.ALBUM_ID.eq(albumId))
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
			try (SelectSelectStep<Record> select = Database.context().select())
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
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
}
