package raubach.fricklweb.server.resource.image;

import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSelectStep;
import org.jooq.impl.DSL;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.Frickl;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.Images;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.ThumbnailUtils;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static raubach.fricklweb.server.database.tables.Images.IMAGES;

/**
 * @author Sebastian Raubach
 */
public class ImageImageResource extends PaginatedServerResource
{
	public static final String PARAM_SIZE = "size";
	public static final String PARAM_TOKEN = "token";

	private Integer imageId = null;
	private ThumbnailUtils.Size size = ThumbnailUtils.Size.ORIGINAL;
	private String token;

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
		token = getQueryValue(PARAM_TOKEN);
	}

	@Get
	public Representation getImage()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		FileRepresentation representation = null;

		if (imageId != null)
		{
			try (Connection conn = Database.getConnection();
				 SelectSelectStep<Record> select = DSL.using(conn, SQLDialect.MYSQL).select())
			{
				SelectConditionStep<Record> step = select.from(IMAGES)
						.where(IMAGES.ID.eq(imageId));

				if (auth && !CustomVerifier.isValidImageToken(token))
					step.and(IMAGES.IS_PUBLIC.eq((byte) 1));

				Images image = step.fetchAnyInto(Images.class);

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
							file = ThumbnailUtils.getOrCreateThumbnail(type, image.getId(), file, size);
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
