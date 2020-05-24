package raubach.fricklweb.server.resource.album;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
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
import raubach.fricklweb.server.database.tables.pojos.Albums;
import raubach.fricklweb.server.database.tables.pojos.Images;
import raubach.fricklweb.server.resource.AbstractAccessTokenResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static raubach.fricklweb.server.database.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.fricklweb.server.database.tables.AlbumTokens.ALBUM_TOKENS;
import static raubach.fricklweb.server.database.tables.Albums.ALBUMS;
import static raubach.fricklweb.server.database.tables.Images.IMAGES;

/**
 * @author Sebastian Raubach
 */
public class AlbumDownloadResource extends AbstractAccessTokenResource
{
	private Integer albumId = null;

	@Override
	protected void doInit()
			throws ResourceException
	{
		super.doInit();

		try
		{
			this.albumId = Integer.parseInt(getRequestAttributes().get("albumId").toString());
		}
		catch (Exception e)
		{
		}
	}

	@Get("application/zip")
	public Representation getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		FileRepresentation representation = null;

		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				Albums album = context.select().from(ALBUMS)
						.where(ALBUMS.ID.eq(albumId))
						.fetchAnyInto(Albums.class);

				SelectConditionStep<Record> step = context.select().from(IMAGES)
						.where(IMAGES.ALBUM_ID.eq(albumId));

				// Restrict to only albums containing at least one public image
				if (!StringUtils.isEmpty(accessToken))
				{
					step.and(DSL.exists(DSL.selectOne()
							.from(ALBUM_TOKENS)
							.leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
							.where(ACCESS_TOKENS.TOKEN.eq(accessToken)
									.and(ALBUM_TOKENS.ALBUM_ID.eq(IMAGES.ALBUM_ID)))));
				}
				else if (StringUtils.isEmpty(user.getToken()))
				{
					step.and(DSL.exists(DSL.selectOne()
							.from(IMAGES)
							.where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)
									.and(IMAGES.IS_PUBLIC.eq((byte) 1)))));
				}

				List<Images> images = step.fetchInto(Images.class);

				if (album != null && images != null && images.size() > 0)
				{
					ServletContext servlet = (ServletContext) getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext");
					String version = servlet.getInitParameter("version");
					File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-download" + "-" + version);
					folder.mkdirs();
					File zipFile = new File(folder, album.getName() + ".zip");

					String prefix = zipFile.getAbsolutePath().replace("\\", "/");
					if (prefix.startsWith("/"))
						prefix = prefix.substring(1);
					URI uri = URI.create("jar:file:/" + prefix);

					Map<String, String> env = new HashMap<>();
					env.put("create", "true");
					env.put("encoding", "UTF-8");

					try (FileSystem fs = FileSystems.newFileSystem(uri, env, null))
					{
						for (Images image : images)
						{
							Files.copy(new File(Frickl.BASE_PATH, image.getPath()).toPath(), fs.getPath("/" + image.getName()));
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
					disposition.setSize(zipFile.length());
					disposition.setFilename(zipFile.getName());
					representation = new FileRepresentation(zipFile, MediaType.APPLICATION_ZIP);
					representation.setSize(zipFile.length());
					representation.setDisposition(disposition);
					// Remember to delete this after the call, we don't need it anymore
					representation.setAutoDeleting(true);
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
