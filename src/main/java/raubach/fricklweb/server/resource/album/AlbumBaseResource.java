package raubach.fricklweb.server.resource.album;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.*;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.computed.AccessToken;
import raubach.fricklweb.server.database.enums.ImagesDataType;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.database.tables.records.*;
import raubach.fricklweb.server.resource.AbstractAccessTokenResource;
import raubach.fricklweb.server.util.*;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Path;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;

import static raubach.fricklweb.server.database.tables.AccessTokens.*;
import static raubach.fricklweb.server.database.tables.AlbumStats.*;
import static raubach.fricklweb.server.database.tables.AlbumTokens.*;
import static raubach.fricklweb.server.database.tables.Albums.*;
import static raubach.fricklweb.server.database.tables.ImageTags.*;
import static raubach.fricklweb.server.database.tables.Images.*;
import static raubach.fricklweb.server.database.tables.LatLngs.*;
import static raubach.fricklweb.server.database.tables.Tags.*;

@Path("album")
@Secured
public class AlbumBaseResource extends AbstractAccessTokenResource
{
	@GET
	@Path("/{albumId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<AlbumStats> getAlbumById(@PathParam("albumId") Integer albumId, @QueryParam("parentAlbumId") Integer parentAlbumId)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select().from(ALBUM_STATS);

			if (albumId != null)
			{
				step.where(ALBUM_STATS.ID.eq(albumId));
			}
			else if (parentAlbumId != null)
			{
				if (parentAlbumId != -1)
					step.where(ALBUM_STATS.PARENT_ALBUM_ID.eq(parentAlbumId));
			}
			else
			{
				if (!auth || !StringUtils.isEmpty(userDetails.getToken()))
					step.where(ALBUM_STATS.PARENT_ALBUM_ID.isNull());
			}

			// Restrict to only albums containing at least one public image
			if (!StringUtils.isEmpty(accessToken))
			{
				step.where(DSL.exists(DSL.selectOne()
										 .from(ALBUM_TOKENS)
										 .leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
										 .where(ACCESS_TOKENS.TOKEN.eq(accessToken)
																   .and(ALBUM_TOKENS.ALBUM_ID.eq(ALBUM_STATS.ID)))));
			}
			else if (StringUtils.isEmpty(userDetails.getToken()))
			{
				step.where(DSL.exists(DSL.selectOne()
										 .from(IMAGES)
										 .where(IMAGES.ALBUM_ID.eq(ALBUM_STATS.ID)
															   .and(IMAGES.IS_PUBLIC.eq((byte) 1)))));
			}

			return step.orderBy(DSL.greatest(DSL.coalesce(ALBUM_STATS.NEWEST_IMAGE, 0), DSL.coalesce(ALBUM_STATS.CREATED_ON, 0)).desc(), ALBUM_STATS.NAME.desc())
					   .limit(pageSize)
					   .offset(pageSize * currentPage)
					   .fetch()
					   .into(AlbumStats.class);
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<AlbumStats> getAlbums(@PathParam("albumId") Integer albumId, @QueryParam("parentAlbumId") Integer parentAlbumId)
		throws IOException, SQLException
	{
		return this.getAlbumById(null, parentAlbumId);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean postAlbum(Albums album)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return false;
		}

		if (album == null || StringUtils.isEmpty(album.getName()))
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return false;
		}

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			File base = new File(Frickl.BASE_PATH);
			File location = new File(Frickl.BASE_PATH);
			// Check the parent album exists
			if (album.getParentAlbumId() != null)
			{
				AlbumsRecord parent = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(album.getParentAlbumId())).fetchAny();

				if (parent == null)
				{
					resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
					return false;
				}

				location = new File(location, parent.getPath());
			}

			location = new File(location, album.getName());

			if (location.exists())
			{
				resp.sendError(Response.Status.CONFLICT.getStatusCode());
				return false;
			}

			location.mkdirs();

			AlbumsRecord record = context.newRecord(ALBUMS, album);
			record.setPath(base.toURI().relativize(location.toURI()).getPath());
			record.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			return record.store() > 0;
		}
	}

	@PATCH
	@Path("/{albumId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void pathAlbum(@PathParam("albumId") Integer albumId, Albums album)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return;
		}

		if (albumId != null && album != null && Objects.equals(album.getId(), albumId))
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				context.update(ALBUMS)
					   .set(ALBUMS.BANNER_IMAGE_ID, album.getBannerImageId())
					   .where(ALBUMS.ID.eq(albumId))
					   .execute();
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return;
		}
	}

	@GET
	@Path("/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public int getAlbumCount(@QueryParam("parentAlbumId") Integer parentAlbumId)
		throws SQLException
	{
		return this.getAlbumByIdCount(null, parentAlbumId);
	}

	@GET
	@Path("/null/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public int getRootCount(@QueryParam("parentAlbumId") Integer parentAlbumId)
		throws SQLException
	{
		return this.getAlbumByIdCount(null, parentAlbumId);
	}

	@GET
	@Path("/{albumId}/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public int getAlbumByIdCount(@PathParam("albumId") Integer albumId, @QueryParam("parentAlbumId") Integer parentAlbumId)
		throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<?> step = context.selectCount().from(ALBUMS);


			if (albumId != null)
			{
				step.where(ALBUMS.ID.eq(albumId));
			}
			else if (parentAlbumId != null)
			{
				if (parentAlbumId != -1)
					step.where(ALBUMS.PARENT_ALBUM_ID.eq(parentAlbumId));
			}
			else
			{
				if (!auth || !StringUtils.isEmpty(userDetails.getToken()))
				{
					step.where(ALBUMS.PARENT_ALBUM_ID.isNull());
				}
			}

			// Restrict to only albums containing at least one public image
			if (!StringUtils.isEmpty(accessToken))
			{
				step.where(DSL.exists(DSL.selectOne()
										 .from(ALBUM_TOKENS)
										 .leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
										 .where(ACCESS_TOKENS.TOKEN.eq(accessToken)
																   .and(ALBUM_TOKENS.ALBUM_ID.eq(ALBUMS.ID)))));
			}
			else if (StringUtils.isEmpty(userDetails.getToken()))
			{
				step.where(DSL.exists(DSL.selectOne()
										 .from(IMAGES)
										 .where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)
															   .and(IMAGES.IS_PUBLIC.eq((byte) 1)))));
			}

			return step.fetchAny(0, int.class);
		}
	}

	@GET
	@Path("/null/download")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public String getRootDownload()
		throws IOException, SQLException
	{
		return this.getAlbumDownload(null);
	}

	@GET
	@Path("/{albumId}/download")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public String getAlbumDownload(@PathParam("albumId") Integer albumId)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		File zipFile;

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
				else if (StringUtils.isEmpty(userDetails.getToken()))
				{
					step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
				}

				List<Images> images = step.fetchInto(Images.class);

				if (album != null && images != null && images.size() > 0)
				{
					String uuid = UUID.randomUUID().toString();
					zipFile = ResourceUtils.createTempFile("frickl-download", uuid, "zip", false);

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

					return zipFile.getName();
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

	@GET
	@Path("/null/location")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<LatLngs> getRootLocations()
		throws IOException, SQLException
	{
		return this.getAlbumLocations(null);
	}

	@GET
	@Path("/{albumId}/location")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<LatLngs> getAlbumLocations(@PathParam("albumId") Integer albumId)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectJoinStep<Record> step = context.select().from(LAT_LNGS);

				if (auth)
				{
					if (!StringUtils.isEmpty(accessToken))
					{
						step.where(DSL.exists(DSL.selectOne()
												 .from(ALBUM_TOKENS)
												 .leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
												 .where(ACCESS_TOKENS.TOKEN.eq(accessToken)
																		   .and(ALBUM_TOKENS.ALBUM_ID.eq(LAT_LNGS.ALBUM_ID)))));
					}
					else if (StringUtils.isEmpty(userDetails.getToken()))
					{
						step.leftJoin(IMAGES).on(IMAGES.ID.eq(LAT_LNGS.ID))
							.where(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				step.where(LAT_LNGS.ALBUM_ID.eq(albumId));

				return step.fetch()
						   .into(LatLngs.class);
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}
	}

	@GET
	@Path("/null/image")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<Images> getRootImages()
		throws SQLException
	{
		return this.getAlbumImages(null);
	}

	@GET
	@Path("/{albumId}/image")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<Images> getAlbumImages(@PathParam("albumId") Integer albumId)
		throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectConditionStep<Record> step = context.select().from(IMAGES)
													  .where(DSL.trueCondition());

			if (albumId != null)
				step.and(IMAGES.ALBUM_ID.eq(albumId));

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

			return step.orderBy(IMAGES.CREATED_ON.desc(), IMAGES.ID.desc())
					   .offset(pageSize * currentPage)
					   .limit(pageSize)
					   .fetch()
					   .into(Images.class);
		}
	}

	@GET
	@Path("/null/image/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public int getRootImageCount()
		throws SQLException
	{
		return this.getAlbumImageCount(null);
	}

	@GET
	@Path("/{albumId}/image/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public int getAlbumImageCount(@PathParam("albumId") Integer albumId)
		throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectConditionStep<Record1<Integer>> step = context.selectCount().from(IMAGES)
																.where(DSL.trueCondition());

			if (albumId != null)
				step.and(IMAGES.ALBUM_ID.eq(albumId));

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

			return step.fetchAny(0, int.class);
		}
	}

	@POST
	@Path("/null/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void postRootTags(Tags[] tags)
		throws IOException, SQLException
	{
		this.getAlbumTags(null);
	}

	@POST
	@Path("/{albumId}/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void postAlbumTags(@PathParam("albumId") Integer albumId, Tags[] tags)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return;
		}

		if (albumId != null && tags != null && tags.length > 0)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				List<Images> images = context.selectFrom(IMAGES)
											 .where(IMAGES.ALBUM_ID.eq(albumId))
											 .fetchInto(Images.class);

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

					images.removeIf(i -> existingIds.contains(i.getId()));

					InsertValuesStep2<ImageTagsRecord, Integer, Integer> step = context.insertInto(IMAGE_TAGS, IMAGE_TAGS.IMAGE_ID, IMAGE_TAGS.TAG_ID);
					for (Images image : images)
						step.values(image.getId(), tag.getId());
					step.execute();
				}

				// Run this in a separate thread, we don't need to wait for it to finish
				new Thread(() -> {
					for (Images image : images)
					{
						if (image.getDataType() != ImagesDataType.video)
						{
							File file = new File(Frickl.BASE_PATH, image.getPath());
							try
							{
								TagUtils.addTagToImage(file, tagStrings);
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					}
				}).start();
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return;
		}
	}

	@DELETE
	@Path("/null/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void deleteRootTags(Tags[] tags)
		throws IOException, SQLException
	{
		this.deleteAlbumTags(null, tags);
	}

	@DELETE
	@Path("/{albumId}/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void deleteAlbumTags(@PathParam("albumId") Integer albumId, Tags[] tags)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return;
		}

		if (albumId != null && tags != null && tags.length > 0)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				List<Images> images = context.selectFrom(IMAGES)
											 .where(IMAGES.ALBUM_ID.eq(albumId))
											 .fetchInto(Images.class);

				if (images == null || images.size() < 1)
				{
					resp.sendError(Response.Status.NOT_FOUND.getStatusCode());
					return;
				}

				List<Integer> imageIds = images.stream().map(Images::getId).collect(Collectors.toList());

				List<Integer> tagIds = new ArrayList<>();
				List<String> tagNames = new ArrayList<>();
				for (Tags tag : tags)
				{
					tagIds.add(tag.getId());
					tagNames.add(tag.getName());
				}

				context.deleteFrom(IMAGE_TAGS)
					   .where(IMAGE_TAGS.IMAGE_ID.in(imageIds))
					   .and(IMAGE_TAGS.TAG_ID.in(tagIds))
					   .execute();

				// Run this in a separate thread, we don't need to wait for it to finish
				new Thread(() -> {
					for (Images image : images)
					{
						if (image.getDataType() != ImagesDataType.video)
						{
							File file = new File(Frickl.BASE_PATH, image.getPath());
							try
							{
								TagUtils.deleteTagFromImage(file, tagNames);
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					}
				}).start();

				// TODO: Delete all tags that have no more images associated with them
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return;
		}
	}

	@GET
	@Path("/null/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<Tags> getRootTags()
		throws SQLException, IOException
	{
		return this.getAlbumTags(null);
	}

	@GET
	@Path("/{albumId}/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<Tags> getAlbumTags(@PathParam("albumId") Integer albumId)
		throws SQLException, IOException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectConditionStep<?> step = context.selectDistinct(TAGS.ID, TAGS.NAME, TAGS.CREATED_ON, TAGS.UPDATED_ON)
												 .from(TAGS
													 .leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
													 .leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID))
													 .leftJoin(ALBUMS).on(ALBUMS.ID.eq(IMAGES.ALBUM_ID)))
												 .where(ALBUMS.ID.eq(albumId));

			// Restrict to only albums containing at least one public image
			if (!StringUtils.isEmpty(accessToken))
			{
				step.and(DSL.exists(DSL.selectOne()
									   .from(ALBUM_TOKENS)
									   .leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
									   .where(ACCESS_TOKENS.TOKEN.eq(accessToken)
																 .and(ALBUM_TOKENS.ALBUM_ID.eq(ALBUMS.ID)))));
			}
			else if (StringUtils.isEmpty(userDetails.getToken()))
			{
				step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
			}

			return step.orderBy(TAGS.NAME)
					   .offset(pageSize * currentPage)
					   .limit(pageSize)
					   .fetch()
					   .into(Tags.class);
		}
	}

	@GET
	@Path("/null/public")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public void getRootPublic(@QueryParam("public") Boolean publicParam)
		throws IOException, SQLException
	{
		this.getAlbumPublic(null, publicParam);
	}

	@GET
	@Path("/{albumId}/public")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public void getAlbumPublic(@PathParam("albumId") Integer albumId, @QueryParam("public") Boolean publicParam)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return;
		}

		if (publicParam == null || albumId == null)
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return;
		}

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			context.update(IMAGES)
				   .set(IMAGES.IS_PUBLIC, (byte) (publicParam ? 1 : 0))
				   .where(IMAGES.ALBUM_ID.eq(albumId))
				   .execute();
		}
	}

	@POST
	@Path("/null/accesstoken")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean postRootAccessToken(AccessToken accessToken)
		throws IOException, SQLException
	{
		return this.postAlbumAccessToken(null, accessToken);
	}

	@POST
	@Path("/{albumId}/accesstoken")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean postAlbumAccessToken(@PathParam("albumId") Integer albumId, AccessToken accessToken)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
			return false;
		}

		if (accessToken == null || StringUtils.isEmpty(accessToken.getToken()) || albumId == null)
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return false;
		}

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			try
			{
				UUID.fromString(accessToken.getToken());
			}
			catch (IllegalArgumentException | NullPointerException e)
			{
				resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
				return false;
			}

			Logger.getLogger("").log(Level.INFO, "TOKEN: " + accessToken);

			AccessTokensRecord token = context.newRecord(ACCESS_TOKENS);
			token.setToken(accessToken.getToken());
			token.setExpiresOn(accessToken.getExpiresOn());

			Logger.getLogger("").log(Level.INFO, "RECORD: " + token);
			token.store();

			AlbumTokensRecord albumToken = context.newRecord(ALBUM_TOKENS);
			albumToken.setAlbumId(albumId);
			albumToken.setAccessTokenId(token.getId());
			return albumToken.store() > 0;
		}
	}

	@POST
	@Path("/{albumId}/image")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean postImages(@PathParam("albumId") Integer albumId)
		throws IOException, SQLException
	{
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
			List<String> finalFilenames = FileUploadHandler.handleMultiple(req, resp, "imageFiles", folder);

			if (!CollectionUtils.isEmpty(finalFilenames))
			{
				boolean needsBannerImage = album.getBannerImageId() == null;

				int counter = 0;
				for (String file : finalFilenames)
				{
					String mimeType = URLConnection.guessContentTypeFromName(file);
					boolean isVideo = mimeType != null && mimeType.startsWith("video");

					Timestamp ts = new Timestamp(System.currentTimeMillis());
					File path = new File(folder, file);
					try
					{
						BasicFileAttributes attr = Files.readAttributes(path.toPath(), BasicFileAttributes.class);
						ts = new Timestamp(attr.creationTime().toMillis());
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					String relativePath = basePath.toURI().relativize(path.toURI()).getPath();

					ImagesRecord image = context.newRecord(IMAGES);
					image.setAlbumId(albumId);
					image.setName(file);
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
				}

				ApplicationListener.startImageScanner(folder);

				return counter > 0;
			}
		}

		return false;
	}
}
