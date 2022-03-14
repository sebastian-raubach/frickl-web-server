package raubach.fricklweb.server.resource.image;

import org.apache.commons.io.IOUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.*;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.computed.DateParameter;
import raubach.fricklweb.server.database.enums.ImagesDataType;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.database.tables.records.*;
import raubach.fricklweb.server.resource.AbstractAccessTokenResource;
import raubach.fricklweb.server.util.*;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.logging.*;

import static raubach.fricklweb.server.database.tables.AccessTokens.*;
import static raubach.fricklweb.server.database.tables.AlbumTokens.*;
import static raubach.fricklweb.server.database.tables.ImageTags.*;
import static raubach.fricklweb.server.database.tables.Images.*;
import static raubach.fricklweb.server.database.tables.Tags.*;

@Path("image")
@Secured
public class ImageBaseResource extends AbstractAccessTokenResource
{
	private final int CHUNK_SIZE = 1024 * 1024 * 2; // 2 MB chunks

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<Images> getImages(@QueryParam("fav") Boolean isFav, @QueryParam("date") DateParameter date)
		throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select().from(IMAGES);

			if (isFav != null && isFav)
				step.where(IMAGES.IS_FAVORITE.eq((byte) 1));
			if (date != null)
				step.where(DSL.date(IMAGES.CREATED_ON).eq(DSL.date(date.getDate())));

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

			return step.orderBy(IMAGES.CREATED_ON.desc(), IMAGES.ID.desc())
					   .offset(pageSize * currentPage)
					   .limit(pageSize)
					   .fetch()
					   .into(Images.class);
		}
	}

	@PATCH
	@Path("/{imageId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void patchImage(@PathParam("imageId") Integer imageId, Images image)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.UNAUTHORIZED.getStatusCode());
			return;
		}

		if (imageId != null && image != null && Objects.equals(image.getId(), imageId))
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				context.update(IMAGES)
					   .set(IMAGES.IS_FAVORITE, image.getIsFavorite())
					   .set(IMAGES.IS_PUBLIC, image.getIsPublic())
					   .where(IMAGES.ID.eq(imageId))
					   .execute();
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
		}
	}

	@GET
	@Path("/{imageId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<Images> getImageById(@PathParam("imageId") Integer imageId)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

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
				else if (StringUtils.isEmpty(userDetails.getToken()))
				{
					step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
				}
			}

			return step.fetch()
					   .into(Images.class);
		}
	}

	@GET
	@Path("/{imageId}/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<Tags> getImageTags(@PathParam("imageId") Integer imageId)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (imageId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectConditionStep<?> step = context.select(TAGS.ID, TAGS.NAME, TAGS.CREATED_ON, TAGS.UPDATED_ON)
													 .from(TAGS
														 .leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
														 .leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID)))
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

				return step.orderBy(TAGS.NAME)
						   .offset(pageSize * currentPage)
						   .limit(pageSize)
						   .fetch()
						   .into(Tags.class);
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}
	}

	@DELETE
	@Path("/{imageId}/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean deleteImageTags(@PathParam("imageId") Integer imageId, Tags tag)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return false;
		}

		if (imageId != null && tag != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				Images image = context.selectFrom(IMAGES)
									  .where(IMAGES.ID.eq(imageId))
									  .fetchAnyInto(Images.class);

				Tags t = context.selectFrom(TAGS)
								.where(TAGS.ID.eq(tag.getId()))
								.fetchAnyInto(Tags.class);

				if (t == null || image == null)
				{
					resp.sendError(Response.Status.NOT_FOUND.getStatusCode());
					return false;
				}

				int numberOfDeletedItems = context.deleteFrom(IMAGE_TAGS)
												  .where(IMAGE_TAGS.IMAGE_ID.eq(image.getId()))
												  .and(IMAGE_TAGS.TAG_ID.eq(tag.getId()))
												  .execute();

				if (image.getDataType() != ImagesDataType.video)
				{
					File file = new File(Frickl.BASE_PATH, image.getPath());
					try
					{
						TagUtils.deleteTagFromImage(file, Collections.singletonList(t.getName()));
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				// TODO: Delete all tags that have no more images associated with them

				return numberOfDeletedItems == 1;
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return false;
		}
	}

	@POST
	@Path("/{imageId}/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean postImageTags(@PathParam("imageId") Integer imageId, Tags[] tags)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return false;
		}

		boolean result = false;
		if (tags != null && tags.length > 0 && imageId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				Images image = context.selectFrom(IMAGES)
									  .where(IMAGES.ID.eq(imageId))
									  .fetchAnyInto(Images.class);

				List<String> tagStrings = new ArrayList<>();
				for (Tags tag : tags)
					tagStrings.add(tag.getName());

				Map<String, Integer> tagIds = context.selectFrom(TAGS)
													 .where(TAGS.NAME.in(tagStrings))
													 .fetchMap(TAGS.NAME, TAGS.ID);

				// Get all existing ids
				for (Tags tag : tags)
				{
					if (tag.getId() == null)
						tag.setId(tagIds.get(tag.getName()));
				}

				for (Tags tag : tags)
				{
					// If it doesn't exist, create it
					if (tag.getId() == null)
					{
						tag.setCreatedOn(new Timestamp(System.currentTimeMillis()));
						TagsRecord t = context.newRecord(TAGS, tag);
						t.store();
						tag.setId(t.getId());
					}

					List<Integer> existingIds = context.select(IMAGE_TAGS.IMAGE_ID)
													   .from(IMAGE_TAGS)
													   .where(IMAGE_TAGS.TAG_ID.eq(tag.getId()))
													   .fetchInto(Integer.class);

					if (!existingIds.contains(image.getId()))
					{

						result = context.insertInto(IMAGE_TAGS, IMAGE_TAGS.IMAGE_ID, IMAGE_TAGS.TAG_ID)
										.values(image.getId(), tag.getId())
										.execute() == 1;
					}
				}

				if (image.getDataType() != ImagesDataType.video)
				{
					// Run this in a separate thread, we don't need to wait for it to finish
					new Thread(() -> {
						File file = new File(Frickl.BASE_PATH, image.getPath());
						try
						{
							TagUtils.addTagToImage(file, tagStrings);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}).start();
				}
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return false;
		}

		return result;
	}

	@GET
	@Path("/{imageId}/img")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"image/png", "image/jpeg", "image/svg+xml", "image/*"})
	@PermitAll
	public Response getImageSrc(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @QueryParam("size") ThumbnailUtils.Size size)
		throws IOException, SQLException
	{
		boolean auth = PropertyWatcher.authEnabled();

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
					else if (!AuthenticationFilter.isValidImageToken(token))
					{
						step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				ImagesRecord image = step.fetchAnyInto(ImagesRecord.class);

				if (image != null)
				{
					File file = new File(Frickl.BASE_PATH, image.getPath());
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

					if (file == null) {
						resp.sendError(Response.Status.NOT_FOUND.getStatusCode());
						return null;
					}

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
						resp.sendError(Response.Status.NOT_FOUND.getStatusCode());
						return null;
					}
				}
			}
			catch (IOException e)
			{
				resp.sendError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
				return null;
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}

		return null;
	}


	@GET
	@Path("/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public int getImageCount(@QueryParam("fav") Boolean isFav, @QueryParam("date") DateParameter date)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record1<Integer>> step = context.selectCount().from(IMAGES);

			if (isFav != null && isFav)
				step.where(IMAGES.IS_FAVORITE.eq((byte) 1));
			if (date != null)
				step.where(DSL.date(IMAGES.CREATED_ON).eq(DSL.date(date.getDate())));

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

			return step.fetchAny(0, int.class);
		}
	}

	@Path("/fav/random")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Images getRandomFav()
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		Images result = null;
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectConditionStep<Record> step = context.select().from(IMAGES)
													  .where(IMAGES.IS_FAVORITE.eq((byte) 1));

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

			result = step.orderBy(DSL.rand())
						 .limit(1)
						 .fetchAny()
						 .into(Images.class);
		}
		catch (SQLException | NullPointerException e)
		{
			e.printStackTrace();
		}

		if (result == null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectJoinStep<Record> step = context.select().from(IMAGES);

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

				result = step.orderBy(DSL.rand())
							 .limit(1)
							 .fetchAny()
							 .into(Images.class);
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
				return null;
			}
		}

		return result;
	}

	@GET
	@Path("/{imageId}/share")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getHtml(@PathParam("imageId") Integer imageId, @HeaderParam("user-agent") String userAgent)
		throws IOException, SQLException
	{
		boolean auth = PropertyWatcher.authEnabled();

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
					boolean isBot = StringUtils.isEmpty(userAgent) || userAgent.toLowerCase().contains("bot") || userAgent.toLowerCase().contains("twitter") || userAgent.toLowerCase().contains("facebook");

					if (isBot)
					{
						URL url = Database.class.getClassLoader().getResource("index.html");

						if (url != null)
						{
							String imageUrl = Frickl.getServerBase(req, true) + "/api/image/" + imageId + "/img?size=MEDIUM";

							File targetFile = new File(getTempFolder(), UUID.randomUUID() + ".html");

							String content = Files.readString(new File(url.toURI()).toPath());

							content = content.replace("{{IMAGE}}", imageUrl);

							Files.write(targetFile.toPath(), content.getBytes(StandardCharsets.UTF_8));

							java.nio.file.Path path = targetFile.toPath();
							return Response.ok((StreamingOutput) output -> {
											   Files.copy(path, output);
											   Files.deleteIfExists(path);
										   })
										   .type(MediaType.TEXT_HTML)
										   .header("content-disposition", "attachment;filename= \"" + targetFile.getName() + "\"")
										   .header("content-length", targetFile.length())
										   .build();
						}
					}
					else
					{
						String pageUrl = Frickl.getServerBase(req, false) + "/#/images/" + imageId;
						return Response.status(Response.Status.MOVED_PERMANENTLY)
									   .location(URI.create(pageUrl))
									   .build();
					}
				}
			}
			catch (URISyntaxException | IOException e)
			{
				resp.sendError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
				return null;
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}

		return null;
	}

	@HEAD
	@Path("/{imageId}/video/{filename}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"video/quicktime", "video/mp4", "video/x-msvideo", "video/x-ms-wmv", "video/webm"})
	@PermitAll
	public Response getVideoByNameHead(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @HeaderParam("Range") String range)
		throws IOException, SQLException
	{
		return this.getVideo(imageId, token, range, true);
	}

	@GET
	@Path("/{imageId}/video/{filename}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"video/quicktime", "video/mp4", "video/x-msvideo", "video/x-ms-wmv", "video/webm"})
	@PermitAll
	public Response getVideoByName(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @HeaderParam("Range") String range)
		throws IOException, SQLException
	{
		return this.getVideo(imageId, token, range, false);
	}

	@HEAD
	@Path("/{imageId}/video")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"video/quicktime", "video/mp4", "video/x-msvideo", "video/x-ms-wmv", "video/webm"})
	@PermitAll
	public Response getVideoHead(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @HeaderParam("Range") String range)
		throws IOException, SQLException
	{
		return this.getVideo(imageId, token, range, true);
	}

	@GET
	@Path("/{imageId}/video")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"video/quicktime", "video/mp4", "video/x-msvideo", "video/x-ms-wmv", "video/webm"})
	@PermitAll
	public Response getVideo(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @HeaderParam("Range") String range)
		throws IOException, SQLException
	{
		return this.getVideo(imageId, token, range, false);
	}

	private Response getVideo(Integer imageId, String token, String range, boolean isHead)
		throws IOException, SQLException
	{
		boolean auth = PropertyWatcher.authEnabled();

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
					else if (!AuthenticationFilter.isValidImageToken(token))
					{
						step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				Images image = step.fetchAnyInto(Images.class);

				if (image != null)
				{
					File file = new File(Frickl.BASE_PATH, image.getPath());
					String filename = file.getName();
					String type;

					if (file.getName().toLowerCase().endsWith(".mp4"))
						type = "video/mp4";
					else if (file.getName().toLowerCase().endsWith(".avi"))
						type = "video/x-msvideo";
					else if (file.getName().toLowerCase().endsWith(".mpg") || file.getName().toLowerCase().endsWith(".mpeg"))
						type = "video/mpeg";
					else if (file.getName().toLowerCase().endsWith(".wmv"))
						type = "video/x-ms-wmv";
					else if (file.getName().toLowerCase().endsWith(".mov"))
						type = "video/quicktime";
					else
						type = "video/*";

					// Check if the image exists
					if (file.exists() && file.isFile())
					{
						if (isHead)
						{
							return Response.ok()
										   .status(Response.Status.PARTIAL_CONTENT)
										   .header(HttpHeaders.CONTENT_LENGTH, file.length())
										   .header("Accept-Ranges", "bytes")
										   .build();
						}
						else
						{
							return this.buildStream(file, range);
						}
					}
					else
					{
						Logger.getLogger("").log(Level.WARNING, "File not found: " + file.getAbsolutePath());
						resp.sendError(Response.Status.NOT_FOUND.getStatusCode());
						return null;
					}
				}
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}

		return null;
	}

	private Response buildStream(final File asset, final String range)
		throws IOException
	{
		// range not requested: firefox does not send range headers
		if (range == null)
		{
			StreamingOutput streamer = output -> {
				try (FileChannel inputChannel = new FileInputStream(asset).getChannel();
					 WritableByteChannel outputChannel = Channels.newChannel(output))
				{

					inputChannel.transferTo(0, inputChannel.size(), outputChannel);
				}
				catch (IOException io)
				{
					Logger.getLogger("").info(io.getMessage());
					io.printStackTrace();
				}
			};

			return Response.ok(streamer)
						   .status(Response.Status.OK)
						   .header(HttpHeaders.CONTENT_LENGTH, asset.length())
						   .build();
		}

		String[] ranges = range.split("=")[1].split("-");

		int from = Integer.parseInt(ranges[0]);

		// Chunk media if the range upper bound is unspecified
		int to = CHUNK_SIZE + from;

		if (to >= asset.length())
		{
			to = (int) (asset.length() - 1);
		}

		// uncomment to let the client decide the upper bound
		// we want to send 2 MB chunks all the time
		//if ( ranges.length == 2 ) {
		//    to = Integer.parseInt( ranges[1] );
		//}

		final String responseRange = String.format("bytes %d-%d/%d", from, to, asset.length());

		final RandomAccessFile raf = new RandomAccessFile(asset, "r");
		raf.seek(from);
		final int len = to - from + 1;
		final MediaStreamer mediaStreamer = new MediaStreamer(len, raf);
		return Response.ok(mediaStreamer)
					   .status(Response.Status.PARTIAL_CONTENT)
					   .header("Accept-Ranges", "bytes")
					   .header("Content-Range", responseRange)
					   .header(HttpHeaders.CONTENT_LENGTH, mediaStreamer.getLenth())
					   .header(HttpHeaders.LAST_MODIFIED, new Date(asset.lastModified()))
					   .build();
	}

	@GET
	@Path("/xago")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<Images> getImagesXago(@QueryParam("year") Integer year)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		currentPage = 0;
		pageSize = Integer.MAX_VALUE;

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select().from(IMAGES);

			Field<Integer> woyI = DSL.field("weekofyear(?)", Integer.class, IMAGES.CREATED_ON);
			Field<Integer> woyN = DSL.field("weekofyear(?)", Integer.class, DSL.now());

			step.where(woyI.eq(woyN)).and(DSL.year(IMAGES.CREATED_ON).eq(DSL.year(DSL.now()).minus(year)));

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

			return step.orderBy(IMAGES.CREATED_ON.desc(), IMAGES.ID.desc())
					   .fetch()
					   .into(Images.class);
		}
	}
}