package raubach.fricklweb.server.resource.image;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;
import raubach.fricklweb.server.*;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.Images;
import raubach.fricklweb.server.resource.AbstractAccessTokenResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.File;
import java.sql.*;
import java.util.logging.*;

import static raubach.fricklweb.server.database.tables.AccessTokens.*;
import static raubach.fricklweb.server.database.tables.AlbumTokens.*;
import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * @author Sebastian Raubach
 */
public class ImageVideoResource extends AbstractAccessTokenResource
{
	public static final String PARAM_TOKEN = "token";

	private Integer imageId = null;
	private String  token;

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
		token = getQueryValue(PARAM_TOKEN);
	}

	@Get
	public Representation getImage()
	{
		boolean auth = PropertyWatcher.authEnabled();

		FileRepresentation representation = null;

		if (imageId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectConditionStep<Record> step = context.select().from(IMAGES)
														  .where(IMAGES.ID.eq(imageId));

				if (auth)
				{
					if (!StringUtils.isEmpty(accessToken))
					{
						step.and(DSL.exists(DSL.selectOne()
											   .from(ALBUM_TOKENS)
											   .leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
											   .where(ACCESS_TOKENS.TOKEN.eq(accessToken)
																		 .and(ALBUM_TOKENS.ALBUM_ID.eq(IMAGES.ALBUM_ID)))));
					}
					else if (!CustomVerifier.isValidImageToken(token))
					{
						step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				Images image = step.fetchAnyInto(Images.class);

				if (image != null)
				{
					File file = new File(Frickl.BASE_PATH, image.getPath());
					String filename = file.getName();
					MediaType type;

					if (file.getName().toLowerCase().endsWith(".mp4"))
						type = MediaType.VIDEO_MP4;
					else if (file.getName().toLowerCase().endsWith(".avi"))
						type = MediaType.VIDEO_AVI;
					else if (file.getName().toLowerCase().endsWith(".mpg") || file.getName().toLowerCase().endsWith(".mpeg"))
						type = MediaType.VIDEO_MPEG;
					else if (file.getName().toLowerCase().endsWith(".wmv"))
						type = MediaType.VIDEO_WMV;
					else if (file.getName().toLowerCase().endsWith(".mov"))
						type = MediaType.VIDEO_QUICKTIME;
					else
						type = MediaType.VIDEO_ALL;

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
						throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
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
