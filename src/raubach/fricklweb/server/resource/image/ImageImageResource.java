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

import javax.servlet.*;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.resource.*;
import raubach.fricklweb.server.util.*;

import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * @author Sebastian Raubach
 */
public class ImageImageResource extends PaginatedServerResource
{
	public static final String PARAM_SIZE = "size";

	private Integer imageId = null;
	private ThumbnailUtils.Size size = ThumbnailUtils.Size.ORIGINAL;

	@Override
	protected void doInit()
		throws ResourceException
	{
		super.doInit();

		try
		{
			imageId = Integer.parseInt(getRequestAttributes().get("imageId").toString());
		}
		catch (Exception e)
		{
		}
		try
		{
			this.size = ThumbnailUtils.Size.valueOf(getQueryValue(PARAM_SIZE));
		}
		catch (Exception e)
		{
			this.size = ThumbnailUtils.Size.ORIGINAL;
		}
	}

	@Get
	public Representation getImage()
	{
		FileRepresentation representation = null;

		if (imageId != null)
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
			{
				Images image = select.from(IMAGES)
									 .where(IMAGES.ID.eq(imageId))
									 .fetchOne()
									 .into(Images.class);

				if (image != null)
				{
					File file = new File(Frickl.BASE_PATH, image.getPath());
					String filename = file.getName();
					MediaType type;

					if (file.getName().toLowerCase().endsWith(".jpg"))
						type = MediaType.IMAGE_JPEG;
					else if (file.getName().toLowerCase().endsWith(".png"))
						type = MediaType.IMAGE_PNG;
					else
						type = MediaType.IMAGE_ALL;

					if (size != ThumbnailUtils.Size.ORIGINAL)
					{
						try
						{
							ServletContext servlet = (ServletContext) getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext");
							file = ThumbnailUtils.getOrCreateThumbnail(servlet, type, image.getId(), file, size);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					// Check if the image exists
					if (file.exists() && file.isFile())
					{

						Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
						disposition.setFilename(filename);
						disposition.setSize(file.length());
						representation = new FileRepresentation(file, type);
						representation.setSize(file.length());
						representation.setDisposition(disposition);
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
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}

		return representation;
	}
}
