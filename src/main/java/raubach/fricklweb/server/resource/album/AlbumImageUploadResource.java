package raubach.fricklweb.server.resource.album;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.glassfish.jersey.media.multipart.*;
import org.jooq.DSLContext;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.*;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.database.enums.ImagesDataType;
import raubach.fricklweb.server.database.tables.records.*;
import raubach.fricklweb.server.resource.ContextResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;

import static raubach.fricklweb.server.database.tables.Albums.*;
import static raubach.fricklweb.server.database.tables.Images.*;


@Path("album/{albumId}/upload/image")
@Secured
@MultipartConfig
public class AlbumImageUploadResource extends ContextResource
{
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean postImages(@PathParam("albumId") Integer albumId, @FormDataParam("imageFile") InputStream fileIs, @FormDataParam("imageFile") FormDataContentDisposition fileDetails)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return false;
		}

		if (albumId == null)
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return false;
		}

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			AlbumsRecord album = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(albumId)).fetchAny();

			if (album == null)
			{
				resp.sendError(Response.Status.NOT_FOUND.getStatusCode());
				return false;
			}

			File basePath = new File(Frickl.BASE_PATH);
			File folder = new File(basePath, album.getPath());

			boolean needsBannerImage = album.getBannerImageId() == null;

			int counter = 0;

			File targetFile = new File(folder, fileDetails.getFileName());
			Files.copy(fileIs, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			String mimeType = URLConnection.guessContentTypeFromName(targetFile.getName());
			boolean isVideo = mimeType != null && mimeType.startsWith("video");

			Timestamp ts = new Timestamp(System.currentTimeMillis());
			try
			{
				BasicFileAttributes attr = Files.readAttributes(targetFile.toPath(), BasicFileAttributes.class);
				ts = new Timestamp(attr.creationTime().toMillis());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			String relativePath = basePath.toURI().relativize(targetFile.toURI()).getPath();

			ImagesRecord image = context.newRecord(IMAGES);
			image.setAlbumId(albumId);
			image.setName(targetFile.getName());
			image.setPath(relativePath);
			image.setDataType(isVideo ? ImagesDataType.video : ImagesDataType.image);
			image.setCreatedOn(ts);
			counter += image.store() > 0 ? 1 : 0;

			if (needsBannerImage)
			{
				album.setBannerImageId(image.getId());
				album.store(ALBUMS.BANNER_IMAGE_ID);
				needsBannerImage = false;
			}

			return counter > 0;
		}
	}
}
