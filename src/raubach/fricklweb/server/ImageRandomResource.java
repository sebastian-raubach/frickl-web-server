package raubach.fricklweb.server;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.*;
import org.restlet.data.Status;
import org.restlet.representation.*;
import org.restlet.resource.*;

import java.io.*;
import java.sql.*;
import java.util.logging.*;

import raubach.fricklweb.server.database.tables.pojos.*;

import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * @author Sebastian Raubach
 */
public class ImageRandomResource extends PaginatedServerResource
{
	@Get
	public Representation getImage()
	{
		FileRepresentation representation = null;

		try (Connection conn = Database.getConnection();
			 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
		{
			Images image = select.from(IMAGES)
								 .where(IMAGES.IS_FAVORITE.eq((byte) 1))
								 .orderBy(DSL.rand())
								 .limit(1)
								 .fetchOne()
								 .into(Images.class);

			Logger.getLogger("").log(Level.INFO, "IMAGE: " + image);

			if (image != null)
			{
				File file = new File(Frickl.BASE_PATH, image.getPath());
				MediaType type;

				if (file.getName().toLowerCase().endsWith(".jpg"))
					type = MediaType.IMAGE_JPEG;
				else if (file.getName().toLowerCase().endsWith(".png"))
					type = MediaType.IMAGE_PNG;
				else
					type = MediaType.IMAGE_ALL;

				// Check if the image exists
				if (file.exists() && file.isFile())
				{

					representation = new FileRepresentation(file, type);
					representation.setSize(file.length());
					representation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT));
				}
				else
				{
					Logger.getLogger("").log(Level.WARNING, "File not found: " + file.getAbsolutePath());
				}
			}
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}

		return representation;
	}
}
