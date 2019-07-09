package raubach.fricklweb.server.resource.image;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.*;
import org.restlet.data.Status;
import org.restlet.representation.*;
import org.restlet.resource.*;

import java.io.*;
import java.sql.*;
import java.util.logging.*;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.resource.*;

import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * @author Sebastian Raubach
 */
public class ImageRandomResource extends PaginatedServerResource
{
	@Get("json")
	public Images getJson()
	{
		Images result = null;
		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select()) {
			result = select.from(IMAGES)
					.where(IMAGES.IS_FAVORITE.eq((byte) 1))
					.orderBy(DSL.rand())
					.limit(1)
					.fetchOne()
					.into(Images.class);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		if (result == null) {
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select()) {
				result = select.from(IMAGES)
						.orderBy(DSL.rand())
						.limit(1)
						.fetchOne()
						.into(Images.class);
			}
			catch (SQLException e)
			{
				e.printStackTrace();

				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}

		return result;
	}
}
