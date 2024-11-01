package raubach.frickl.next.resource.image;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jhi.oddjob.JobInfo;
import org.apache.commons.io.IOUtils;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.*;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.tables.pojos.*;
import raubach.frickl.next.codegen.tables.records.ImagesRecord;
import raubach.frickl.next.pojo.*;
import raubach.frickl.next.resource.PaginatedServerResource;
import raubach.frickl.next.util.*;
import raubach.frickl.next.util.async.ImageZipExporter;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.File;
import java.io.*;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;

import static raubach.frickl.next.codegen.tables.Albums.ALBUMS;
import static raubach.frickl.next.codegen.tables.ImageTags.IMAGE_TAGS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;
import static raubach.frickl.next.codegen.tables.Tags.TAGS;

@Path("image")
public class ImageResource extends PaginatedServerResource
{
	@GET
	@Path("/{imageId:\\d+}/hierarchy")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response getImageAlbumHierarchy(@PathParam("imageId") Integer imageId)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			Set<Integer> albumsForUser = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);

			List<Albums> hierarchy = new ArrayList<>();

			Images image = context.selectFrom(IMAGES).where(IMAGES.ID.eq(imageId)).fetchAnyInto(Images.class);

			if (image != null && image.getAlbumId() != null)
			{
				Albums current = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(image.getAlbumId())).fetchAnyInto(Albums.class);

				if (Permission.IS_ADMIN.allows(userDetails.getPermissions()) || albumsForUser.contains(current.getId()))
				{
					hierarchy.add(0, current);

					while (current != null && current.getParentAlbumId() != null)
					{
						current = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(current.getParentAlbumId())).fetchAnyInto(Albums.class);

						if (Permission.IS_ADMIN.allows(userDetails.getPermissions()) || albumsForUser.contains(current.getId()))
						{
							if (current != null)
								hierarchy.add(0, current);
						}
					}
				}
			}

			return Response.ok(hierarchy).build();
		}
	}

	private PaginatedResult<List<Images>> getImages(ImageRequest request)
			throws SQLException
	{
		processRequest(request);

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			SelectSelectStep<Record> select = context.select(IMAGES.fields());

			if (previousCount == -1)
				select.hint("SQL_CALC_FOUND_ROWS");

			SelectJoinStep<Record> step = select.from(IMAGES).leftJoin(ALBUMS).on(ALBUMS.ID.eq(IMAGES.ALBUM_ID));

			restrict(context, request, step);

			List<Images> result = setPaginationAndOrderBy(step)
					.fetchInto(Images.class);

			long count = previousCount == -1 ? context.fetchOne("SELECT FOUND_ROWS()").into(Long.class) : previousCount;

			return new PaginatedResult<>(result, count);
		}
	}

	private List<Integer> getIds(ImageRequest request)
			throws SQLException
	{
		processRequest(request);

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			SelectSelectStep<Record1<Integer>> select = context.select(IMAGES.ID);

			SelectJoinStep<Record1<Integer>> step = select.from(IMAGES);

			restrict(context, request, step);

			return step.fetchInto(Integer.class);
		}
	}

	private void restrict(DSLContext context, ImageRequest request, SelectJoinStep<?> step)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		// Restrict to only albums containing at least one public image
		if (Permission.IS_ADMIN.allows(userDetails.getPermissions()))
		{
			// Nothing required here, admins can see everything
		}
		else if (StringUtils.isEmpty(userDetails.getToken()))
		{
			// Check if the album contains public images
			step.where(IMAGES.IS_PUBLIC.eq((byte) 1));
		}
		else
		{
			// Check user permissions for the album
			Set<Integer> albumAccess = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);
			step.where(IMAGES.ALBUM_ID.in(albumAccess));
		}

		if (request.getImageId() != null)
			step.where(IMAGES.ID.eq(request.getImageId()));
		if (request.getIsFav() != null && request.getIsFav())
			step.where(IMAGES.IS_FAVORITE.eq((byte) 1));
		if (request.getDate() != null)
			step.where(DSL.date(IMAGES.CREATED_ON).eq(DSL.date(request.getDate())));
		if (!StringUtils.isBlank(searchTerm))
			step.where(IMAGES.NAME.containsIgnoreCase(searchTerm)
								  .or(ALBUMS.NAME.containsIgnoreCase(searchTerm))
								  .or(DSL.exists(DSL.selectOne()
													.from(TAGS)
													.leftJoin(IMAGE_TAGS).on(IMAGE_TAGS.TAG_ID.eq(TAGS.ID))
													.where(TAGS.NAME.containsIgnoreCase(searchTerm))
													.and(IMAGE_TAGS.IMAGE_ID.eq(IMAGES.ID)))));
		if (request.getTagId() != null)
			step.whereExists(DSL.selectOne()
								.from(IMAGE_TAGS)
								.where(IMAGE_TAGS.IMAGE_ID.eq(IMAGES.ID))
								.and(IMAGE_TAGS.TAG_ID.eq(request.getTagId())));
		if (request.getAlbumId() != null)
		{
			if (request.getAlbumId() == -1)
				step.where(IMAGES.ALBUM_ID.isNull());
			else
				step.where(IMAGES.ALBUM_ID.eq(request.getAlbumId()));
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response postImages(ImageRequest request)
			throws SQLException
	{
		processRequest(request);

		return Response.ok(getImages(request)).build();
	}

	@POST
	@Path("/ids")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response getImageIds(ImageRequest request)
			throws SQLException
	{
		processRequest(request);

		return Response.ok(getIds(request)).build();
	}

	@GET
	@Path("/{imageId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response getImageById(@PathParam("imageId") Integer imageId)
			throws SQLException
	{
		PaginatedResult<List<Images>> images = getImages(new ImageRequest().setImageId(imageId));
		if (!CollectionUtils.isEmpty(images.getData()))
			return Response.ok(images.getData().get(0)).build();
		else
			return Response.ok(null).build();
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(Permission.IMAGE_DELETE)
	public Response deleteImages(List<Integer> imageIds)
			throws SQLException
	{
		if (!CollectionUtils.isEmpty(imageIds))
		{
			AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

			try (Connection conn = Database.getConnection())
			{
				DSLContext context = Database.getContext(conn);

				List<ImagesRecord> images = getImageRecord(context, imageIds, userDetails);

				images.forEach(image -> {
					try
					{
						deleteImage(image);
					}
					catch (FileNotFoundException e)
					{
						// Don't throw for the multi-delete
					}
				});

				return Response.ok().build();
			}
		}

		return Response.ok().build();
	}

	@DELETE
	@Path("/{imageId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(Permission.IMAGE_DELETE)
	public Response deleteImage(@PathParam("imageId") Integer imageId)
			throws SQLException
	{
		if (imageId != null)
		{
			AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

			try (Connection conn = Database.getConnection())
			{
				DSLContext context = Database.getContext(conn);

				List<ImagesRecord> images = getImageRecord(context, imageId, userDetails);

				if (!CollectionUtils.isEmpty(images))
				{
					try
					{
						deleteImage(images.get(0));
					}
					catch (FileNotFoundException e)
					{
						return Response.status(Response.Status.NOT_FOUND).build();
					}

					return Response.ok().build();
				}
			}
		}

		return Response.ok().build();
	}

	private void deleteImage(ImagesRecord image)
			throws FileNotFoundException
	{
		java.io.File file = new File(Frickl.BASE_PATH, image.getPath());

		image.delete();

		if (file.exists() && file.isFile())
		{
			String type;

			if (file.getName().toLowerCase().endsWith(".jpg"))
				type = "image/jpeg";
			else if (file.getName().toLowerCase().endsWith(".png"))
				type = "image/png";
			else
				type = "image/*";
			// Delete the image
			file.delete();

			// Delete all thumbnails
			for (ThumbnailUtils.Size size : ThumbnailUtils.Size.values())
			{
				file = ThumbnailUtils.getThumbnail(type, image.getId(), size);

				if (file.exists() && file.isFile())
					file.delete();
			}
		}
		else
		{
			throw new FileNotFoundException();
		}
	}

	@PATCH
	@Path("/{imageId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(Permission.IMAGE_UPLOAD)
	public Response patchImage(@PathParam("imageId") Integer imageId, Images image)
			throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		if (imageId != null && image != null && Objects.equals(image.getId(), imageId))
		{
			try (Connection conn = Database.getConnection())
			{
				DSLContext context = Database.getContext(conn);

				SelectConditionStep<ImagesRecord> step = context.selectFrom(IMAGES).where(IMAGES.ID.eq(imageId));

				// Restrict to only albums containing at least one public image
				if (Permission.IS_ADMIN.allows(userDetails.getPermissions()))
				{
					// Nothing required here, admins can see everything
				}
				else if (StringUtils.isEmpty(userDetails.getToken()))
				{
					// Check if the album contains public images
					step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
				}
				else
				{
					// Check user permissions for the album
					Set<Integer> albumAccess = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);
					step.and(IMAGES.ALBUM_ID.in(albumAccess));
				}

				ImagesRecord existingImage = step.fetchAny();

				if (existingImage != null)
				{
					existingImage.setIsFavorite(image.getIsFavorite());
					existingImage.setIsPublic(image.getIsPublic());
					existingImage.store(IMAGES.IS_FAVORITE, IMAGES.IS_PUBLIC);
				}
				else
				{
					return Response.status(Response.Status.NOT_FOUND).build();
				}
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return Response.ok(true).build();
	}

	@GET
	@Path("/{imageId:\\d+}/img/{filename}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"image/png", "image/jpeg", "image/svg+xml", "image/*"})
	@Secured
	@PermitAll
	public Response getImageSrcWName(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @QueryParam("size") ThumbnailUtils.Size size)
			throws IOException, SQLException
	{
		return getImageSrc(imageId, token, size);
	}

	private List<ImagesRecord> getImageRecord(DSLContext context, Integer imageId, String token)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = AuthenticationFilter.getUserDetailsFromImageToken(token);

		return getImageRecord(context, imageId, userDetails);
	}

	private List<ImagesRecord> getImageRecord(DSLContext context, Integer imageId, AuthenticationFilter.UserDetails userDetails)
			throws SQLException
	{
		return getImageRecord(context, Collections.singletonList(imageId), userDetails);
	}

	private List<ImagesRecord> getImageRecord(DSLContext context, List<Integer> imageIds, AuthenticationFilter.UserDetails userDetails)
			throws SQLException
	{
		SelectConditionStep<Record> step = context.select().from(IMAGES)
												  .where(IMAGES.ID.in(imageIds));

		// Restrict to only albums containing at least one public image
		if (Permission.IS_ADMIN.allows(userDetails.getPermissions()))
		{
			// Nothing required here, admins can see everything
		}
		else if (StringUtils.isEmpty(userDetails.getToken()))
		{
			// Check if the album contains public images
			step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
		}
		else
		{
			// Check user permissions for the album
			Set<Integer> albumAccess = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);
			step.and(IMAGES.ALBUM_ID.in(albumAccess));
		}

		return step.fetchInto(ImagesRecord.class);
	}

	@GET
	@Path("/{imageId:\\d+}/img")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"image/png", "image/jpeg", "image/svg+xml", "image/*"})
	@Secured
	@PermitAll
	public Response getImageSrc(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @QueryParam("size") ThumbnailUtils.Size size)
			throws IOException, SQLException
	{
		if (imageId != null)
		{
			try (Connection conn = Database.getConnection())
			{
				DSLContext context = Database.getContext(conn);
				List<ImagesRecord> images = getImageRecord(context, imageId, token);

				if (!CollectionUtils.isEmpty(images))
				{
					ImagesRecord image = images.get(0);
					java.io.File file = new File(Frickl.BASE_PATH, image.getPath());
					String filename = file.getName();
					String type;

					if (file.getName().toLowerCase().endsWith(".jpg"))
						type = "image/jpeg";
					else if (file.getName().toLowerCase().endsWith(".png"))
						type = "image/png";
					else
						type = "image/*";

					if (size != ThumbnailUtils.Size.ORIGINAL)
					{
						try
						{
							file = ThumbnailUtils.getOrCreateThreaded(type, image, file, size);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}

					if (file == null)
						return Response.status(Response.Status.NOT_FOUND).build();

					// Set it again
					if (file.getName().toLowerCase().endsWith(".jpg"))
						type = "image/jpeg";
					else if (file.getName().toLowerCase().endsWith(".png"))
						type = "image/png";
					else
						type = "image/*";

					// Check if the image exists
					if (file.exists() && file.isFile())
					{
						if (size == ThumbnailUtils.Size.ORIGINAL)
						{
							image.setViewCount(image.getViewCount() + 1);
							image.store(IMAGES.VIEW_COUNT);
						}

						byte[] bytes = IOUtils.toByteArray(file.toURI());

						return Response.ok(new ByteArrayInputStream(bytes))
									   .header("Content-Type", type)
									   .header("content-disposition", "attachment;filename= \"" + file.getName() + "\"")
									   .header("content-length", file.length())
									   .build();
					}
					else
					{
						Logger.getLogger("").log(Level.WARNING, "File not found: " + file.getAbsolutePath());

						return Response.status(Response.Status.NOT_FOUND).build();
					}
				}
			}
			catch (IOException e)
			{
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return Response.noContent().build();
	}

	@POST
	@Path("/download")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response downloadImages(List<Integer> imageIds)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			List<ImagesRecord> images = getImageRecord(context, imageIds, userDetails);

			String idString = images.stream().map(i -> Integer.toString(i.getId())).collect(Collectors.joining(","));

			String uuid = UUID.randomUUID().toString();

			String version = PropertyWatcher.get(ServerProperty.API_VERSION);
			File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-exports" + "-" + version);
			File targetFolder = new File(folder, uuid);
			targetFolder.mkdirs();

			File idFile = new File(targetFolder, "ids.txt");
			java.nio.file.Files.writeString(idFile.toPath(), idString, StandardOpenOption.CREATE);

			File libFolder = ResourceUtils.getLibFolder();
			List<String> args = new ArrayList<>();
			args.add("-cp");
			args.add(libFolder.getAbsolutePath() + File.separator + "*");
			args.add(ImageZipExporter.class.getCanonicalName());
			args.add(raubach.frickl.next.util.StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_SERVER)));
			args.add(raubach.frickl.next.util.StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_NAME)));
			args.add(raubach.frickl.next.util.StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_PORT)));
			args.add(raubach.frickl.next.util.StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_USERNAME)));
			args.add(raubach.frickl.next.util.StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_PASSWORD)));
			args.add(idFile.getAbsolutePath());
			args.add(Frickl.BASE_PATH);
			args.add(targetFolder.getAbsolutePath());
			args.add(raubach.frickl.next.util.StringUtils.orEmptyQuotes(userDetails != null ? userDetails.getToken() : ""));

			JobInfo info = ApplicationListener.SCHEDULER.submit("ImageZipExporter", "java", args, targetFolder.getAbsolutePath());
			AsyncExportResult result = new AsyncImageExportResult()
					.setImageCount(images.size())
					.setUserToken(userDetails.getToken())
					.setToken(uuid)
					.setJobId(info.getId())
					.setStatus(ExportStatus.RUNNING)
					.setCreatedOn(new Date(System.currentTimeMillis()));
			ApplicationListener.SCHEDULER_IDS.put(uuid, result);

			return Response.ok(result).build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Logger.getLogger("").info(e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
