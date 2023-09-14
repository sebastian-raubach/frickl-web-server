package raubach.frickl.next.resource.image;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.apache.commons.io.IOUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.*;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.tables.pojos.Images;
import raubach.frickl.next.codegen.tables.records.ImagesRecord;
import raubach.frickl.next.pojo.*;
import raubach.frickl.next.resource.AbstractAccessTokenResource;
import raubach.frickl.next.util.ThumbnailUtils;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.*;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import static raubach.frickl.next.codegen.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.frickl.next.codegen.tables.AlbumTokens.ALBUM_TOKENS;
import static raubach.frickl.next.codegen.tables.ImageTags.IMAGE_TAGS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;

@Path("image")
@Secured
public class ImageResource extends AbstractAccessTokenResource
{
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response postImages(ImageRequest request)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		processRequest(request);
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			SelectSelectStep<Record> select = context.select();

			if (previousCount == -1)
				select.hint("SQL_CALC_FOUND_ROWS");

			SelectJoinStep<Record> step = select.from(IMAGES);

			if (request.getIsFav() != null && request.getIsFav())
				step.where(IMAGES.IS_FAVORITE.eq((byte) 1));
			if (request.getDate() != null)
				step.where(DSL.date(IMAGES.CREATED_ON).eq(DSL.date(request.getDate())));
			if (!StringUtils.isBlank(request.getSearchTerm()))
				step.where(IMAGES.NAME.containsIgnoreCase(request.getSearchTerm()));
			if (request.getTagId() != null)
				step.whereExists(DSL.selectOne()
									.from(IMAGE_TAGS)
									.where(IMAGE_TAGS.IMAGE_ID.eq(IMAGES.ID))
									.and(IMAGE_TAGS.TAG_ID.eq(request.getTagId())));
			if (request.getAlbumId() == null)
				step.where(IMAGES.ALBUM_ID.isNull());
			else if (request.getAlbumId() != -1)
				step.where(IMAGES.ALBUM_ID.eq(request.getAlbumId()));

			if (auth)
			{
				if (!StringUtils.isEmpty(accessToken))
				{
					step.where(DSL.exists(DSL.selectOne()
											 .from(ALBUM_TOKENS)
											 .leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
											 .where(ACCESS_TOKENS.TOKEN.eq(accessToken)
																	   .and(ALBUM_TOKENS.ALBUM_ID.eq(IMAGES.ALBUM_ID)))));
				}
				else if (StringUtils.isEmpty(userDetails.getToken()))
				{
					step.where(IMAGES.IS_PUBLIC.eq((byte) 1));
				}
			}

			List<Images> result = setPaginationAndOrderBy(step)
					.fetchInto(Images.class);

			long count = previousCount == -1 ? context.fetchOne("SELECT FOUND_ROWS()").into(Long.class) : previousCount;

			return Response.ok(new PaginatedResult<>(result, count))
						   .build();
		}
	}

	@GET
	@Path("/{imageId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getImageById(@PathParam("imageId") Integer imageId)
			throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			// Update the view count
			context.update(IMAGES)
				   .set(IMAGES.VIEW_COUNT, IMAGES.VIEW_COUNT.plus(1))
				   .where(IMAGES.ID.eq(imageId))
				   .execute();

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
				else if (StringUtils.isEmpty(userDetails.getToken()))
				{
					step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
				}
			}

			return Response.ok(step.fetchAnyInto(Images.class))
						   .build();
		}
	}

	@PATCH
	@Path("/{imageId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response patchImage(@PathParam("imageId") Integer imageId, Images image)
			throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.UNAUTHORIZED).build();

		if (imageId != null && image != null && Objects.equals(image.getId(), imageId))
		{
			try (Connection conn = Database.getConnection())
			{
				DSLContext context = Database.getContext(conn);
				context.update(IMAGES)
					   .set(IMAGES.IS_FAVORITE, image.getIsFavorite())
					   .set(IMAGES.IS_PUBLIC, image.getIsPublic())
					   .where(IMAGES.ID.eq(imageId))
					   .execute();
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
	@PermitAll
	public Response getImageSrcWName(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @QueryParam("size") ThumbnailUtils.Size size)
			throws IOException, SQLException
	{
		return getImageSrc(imageId, token, size);
	}

	@GET
	@Path("/{imageId:\\d+}/img")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"image/png", "image/jpeg", "image/svg+xml", "image/*"})
	@PermitAll
	public Response getImageSrc(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @QueryParam("size") ThumbnailUtils.Size size)
			throws IOException, SQLException
	{
		boolean auth = PropertyWatcher.authEnabled();

		if (imageId != null)
		{
			try (Connection conn = Database.getConnection())
			{
				DSLContext context = Database.getContext(conn);
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
					else if (!AuthenticationFilter.isValidImageToken(token))
					{
						step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				ImagesRecord image = step.fetchAnyInto(ImagesRecord.class);

				if (image != null)
				{
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
}
