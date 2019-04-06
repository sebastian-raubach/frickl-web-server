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
	}

	@Get("json")
	public List<Images> getJson()
	{
		if (albumId != null)
		{
			try (SelectSelectStep<Record> select = Database.select())
			{
				return select.from(IMAGES)
							 .where(IMAGES.ALBUM_ID.eq(albumId))
							 .offset(pageSize * currentPage)
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
