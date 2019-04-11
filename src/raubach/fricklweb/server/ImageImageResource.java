package raubach.fricklweb.server;

import net.coobird.thumbnailator.*;

import org.jooq.*;
import org.restlet.data.*;
import org.restlet.data.Status;
import org.restlet.representation.*;
import org.restlet.resource.*;

import java.io.*;
import java.sql.*;
import java.util.logging.*;

import javax.servlet.*;

import raubach.fricklweb.server.database.tables.pojos.*;

import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * @author Sebastian Raubach
 */
public class ImageImageResource extends PaginatedServerResource
{
	public static final String PARAM_SIZE = "small";

	private Integer imageId = null;
	private boolean small   = false;

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
			this.small = Boolean.parseBoolean(getQueryValue(PARAM_SIZE));
		}
		catch (Exception e)
		{
		}
	}

	@Get()
	public Representation getImage()
	{
		FileRepresentation representation = null;

		if (imageId != null)
		{
			try (SelectSelectStep<Record> select = Database.context().select())
			{
				Images image = select.from(IMAGES)
									 .where(IMAGES.ID.eq(imageId))
									 .fetchOne()
									 .into(Images.class);

				if (image != null)
				{
					File file = new File(Frickl.BASE_PATH, image.getPath());

					// Check if the image exists
					if (file.exists() && file.isFile())
					{
						try
						{
							MediaType type;

							if (file.getName().toLowerCase().endsWith(".jpg"))
								type = MediaType.IMAGE_JPEG;
							else if (file.getName().toLowerCase().endsWith(".png"))
								type = MediaType.IMAGE_PNG;
							else
								type = MediaType.IMAGE_ALL;

							if (small)
							{
								ServletContext servlet = (ServletContext) getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext");
								String version = servlet.getInitParameter("version");
								File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-thumbnails" + "-" + version);
								folder.mkdirs();

								String extension = type == MediaType.IMAGE_PNG ? ".png" : ".jpg";

								File target = new File(folder, image.getId() + "-small" + extension);

								// Delete the thumbnail if it's older than the source image
								if (target.lastModified() < file.lastModified())
									target.delete();

								// If it exists, fine, just return it
								if (target.exists())
								{
									file = target;
								}
								// If not, create a new thumbnail
								else
								{
									Thumbnails.of(file)
											  .height(400)
											  .keepAspectRatio(true)
											  .toFile(target);

									file = target;
								}
							}

							representation = new FileRepresentation(file, type);
							representation.setSize(file.length());
							representation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT));
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
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
