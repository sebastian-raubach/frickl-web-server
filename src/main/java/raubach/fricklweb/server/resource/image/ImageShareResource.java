package raubach.fricklweb.server.resource.image;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
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

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static raubach.fricklweb.server.database.tables.Images.IMAGES;

/**
 * @author Sebastian Raubach
 */
public class ImageShareResource extends PaginatedServerResource
{
	private Integer imageId = null;

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
	}

	@Get
	public Representation getHtml()
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
					step.and(IMAGES.IS_PUBLIC.eq((byte) 1));

				Images image = step.fetchAnyInto(Images.class);

				if (image != null)
				{
					HttpServletRequest req = ServletUtils.getRequest(getRequest());
					String userAgent = getRequest().getClientInfo().getAgent();

					boolean isBot = StringUtils.isEmpty(userAgent) || userAgent.toLowerCase().contains("bot") || userAgent.toLowerCase().contains("twitter") || userAgent.toLowerCase().contains("facebook");

					if (isBot) {
						URL url = Database.class.getClassLoader().getResource("index.html");

						if (url != null)
						{
							String imageUrl = Frickl.getServerBase(req, true) + "/api/image/" + imageId + "/img?size=MEDIUM";

							File targetFile = getTempDir(UUID.randomUUID().toString() + ".html");

							String content = new String(Files.readAllBytes(new File(url.toURI()).toPath()), StandardCharsets.UTF_8);

							content = content.replace("{{IMAGE}}", imageUrl);

							Files.write(targetFile.toPath(), content.getBytes(StandardCharsets.UTF_8));

							representation = new FileRepresentation(targetFile, MediaType.TEXT_HTML);
							representation.setSize(targetFile.length());
							representation.setAutoDeleting(true);
							return representation;
						}
					}
					else
					{
						String pageUrl = Frickl.getServerBase(req, false) + "/#/images/" + imageId;
						redirectPermanent(pageUrl);
						return null;
					}
				}
			}
			catch (SQLException | URISyntaxException | IOException e)
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
