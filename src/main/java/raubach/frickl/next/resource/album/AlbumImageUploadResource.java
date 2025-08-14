package raubach.frickl.next.resource.album;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.glassfish.jersey.media.multipart.*;
import org.jooq.DSLContext;
import raubach.frickl.next.*;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.enums.ImagesDataType;
import raubach.frickl.next.codegen.tables.records.*;
import raubach.frickl.next.resource.ContextResource;
import raubach.frickl.next.util.*;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.util.Set;

import static raubach.frickl.next.codegen.tables.Albums.ALBUMS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;

@Path("album/{albumId:\\d+}/upload/image")
@Secured(Permission.IMAGE_UPLOAD)
@MultipartConfig
public class AlbumImageUploadResource extends ContextResource
{
	@PathParam("albumId")
	Integer albumId;

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postImages(@FormDataParam("imageFile") InputStream fileIs, @FormDataParam("imageFile") FormDataContentDisposition fileDetails)
			throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		boolean userIsAdmin = Permission.IS_ADMIN.allows(userDetails.getPermissions());

		if (albumId == null)
			return Response.status(Response.Status.BAD_REQUEST).build();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			if (!userIsAdmin)
			{
				Set<Integer> albumAccess = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);

				if (!albumAccess.contains(albumId))
					return Response.status(Response.Status.FORBIDDEN).build();
			}

			AlbumsRecord album = context.selectFrom(ALBUMS)
										.where(ALBUMS.ID.eq(albumId))
										.fetchAny();

			if (album == null)
				return Response.status(Response.Status.NOT_FOUND).build();

			File basePath = new File(Frickl.BASE_PATH);
			File folder = new File(basePath, album.getPath());

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
			image.setCreatedBy(userDetails.getId());
			image.setCreatedOn(ts);

			if (album.getBannerImageId() == null)
			{
				album.setBannerImageId(image.getId());
				album.store(ALBUMS.BANNER_IMAGE_ID);
			}

			// TODO: Run the album count update task for this particular album again

			return Response.ok().build();
		}
	}
}
