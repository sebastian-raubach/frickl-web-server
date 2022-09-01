package raubach.fricklweb.server.resource.album;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.*;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.computed.AccessToken;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.database.tables.records.*;
import raubach.fricklweb.server.resource.AbstractAccessTokenResource;
import raubach.fricklweb.server.util.*;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
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
	public Response getAlbumById(@PathParam("albumId") Integer albumId, @QueryParam("parentAlbumId") Integer parentAlbumId)
		throws SQLException
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
				step.where(ALBUM_STATS.PARENT_ALBUM_ID.isNull());
			}

			if (auth)
			{
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
			}

			return Response.ok(step.orderBy(DSL.coalesce(ALBUM_STATS.NEWEST_IMAGE, 0).desc(), DSL.coalesce(ALBUM_STATS.CREATED_ON, 0).desc(), ALBUM_STATS.NAME.desc())
								   .limit(pageSize)
								   .offset(pageSize * currentPage)
								   .fetch()
								   .into(AlbumStats.class))
						   .build();
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getAlbums(@PathParam("albumId") Integer albumId, @QueryParam("parentAlbumId") Integer parentAlbumId)
		throws IOException, SQLException
	{
		return this.getAlbumById(null, parentAlbumId);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postAlbum(Albums album)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		if (album == null || StringUtils.isEmpty(album.getName()))
			return Response.status(Response.Status.BAD_REQUEST).build();

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
					return Response.status(Response.Status.BAD_REQUEST).build();

				location = new File(location, parent.getPath());
			}

			location = new File(location, album.getName());

			if (location.exists())
				return Response.status(Response.Status.CONFLICT).build();

			location.mkdirs();

			AlbumsRecord record = context.newRecord(ALBUMS, album);
			record.setPath(base.toURI().relativize(location.toURI()).getPath());
			record.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			return Response.ok(record.store() > 0).build();
		}
	}

	@PATCH
	@Path("/{albumId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response patchAlbum(@PathParam("albumId") Integer albumId, Albums album)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

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
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return Response.ok(true).build();
	}

	@GET
	@Path("/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getAlbumCount(@QueryParam("parentAlbumId") Integer parentAlbumId)
		throws SQLException
	{
		return this.getAlbumByIdCount(null, parentAlbumId);
	}

	@GET
	@Path("/null/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getRootCount(@QueryParam("parentAlbumId") Integer parentAlbumId)
		throws SQLException
	{
		return this.getAlbumByIdCount(null, parentAlbumId);
	}

	@GET
	@Path("/{albumId}/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getAlbumByIdCount(@PathParam("albumId") Integer albumId, @QueryParam("parentAlbumId") Integer parentAlbumId)
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
				step.where(ALBUMS.PARENT_ALBUM_ID.isNull());
			}

			if (auth)
			{
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
			}

			return Response.ok(step.fetchAny(0, int.class)).build();
		}
	}

	@GET
	@Path("/null/download")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getRootDownload()
		throws IOException, SQLException
	{
		return this.getAlbumDownload(null);
	}

	@GET
	@Path("/{albumId}/download")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getAlbumDownload(@PathParam("albumId") Integer albumId)
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

				if (auth)
				{
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

					return Response.ok(zipFile.getName()).build();
				}
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return Response.noContent().build();
	}

	@GET
	@Path("/null/location")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getRootLocations()
		throws IOException, SQLException
	{
		return this.getAlbumLocations(null);
	}

	@GET
	@Path("/{albumId}/location")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getAlbumLocations(@PathParam("albumId") Integer albumId)
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

				return Response.ok(step.fetch()
									   .into(LatLngs.class))
							   .build();
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path("/null/image")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getRootImages()
		throws SQLException
	{
		return this.getAlbumImages(null);
	}

	@GET
	@Path("/{albumId}/image")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getAlbumImages(@PathParam("albumId") Integer albumId)
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

			return Response.ok(step.orderBy(IMAGES.CREATED_ON.desc(), IMAGES.ID.desc())
								   .offset(pageSize * currentPage)
								   .limit(pageSize)
								   .fetch()
								   .into(Images.class))
						   .build();
		}
	}

	@GET
	@Path("/null/image/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getRootImageCount()
		throws SQLException
	{
		return this.getAlbumImageCount(null);
	}

	@GET
	@Path("/{albumId}/image/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getAlbumImageCount(@PathParam("albumId") Integer albumId)
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

			return Response.ok(step.fetchAny(0, int.class)).build();
		}
	}

	@POST
	@Path("/null/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postRootTags(Tags[] tags)
		throws IOException, SQLException
	{
		return this.postAlbumTags(null, tags);
	}

	@POST
	@Path("/{albumId}/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postAlbumTags(@PathParam("albumId") Integer albumId, Tags[] tags)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		if (albumId != null && tags != null && tags.length > 0)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				Albums album = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(albumId)).fetchAnyInto(Albums.class);

				List<Images> images = context.selectFrom(IMAGES)
											 .where(IMAGES.ALBUM_ID.eq(albumId))
											 .fetchInto(Images.class);

				List<String> tagStrings = new ArrayList<>();
				for (Tags tag : tags)
					tagStrings.add(tag.getName());

				Map<String, Integer> tagIds = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

				context.selectFrom(TAGS)
					   .where(TAGS.NAME.in(tagStrings))
					   .forEach(t -> {
						   if (!tagIds.containsKey(t.getName()))
							   tagIds.put(t.getName(), t.getId());
					   });

				for (Tags tag : tags)
				{
					if (tag.getId() == null)
					{
						Integer existing = tagIds.get(tag.getName());

						if (existing == null)
						{
							// Lower-case it
							tag.setName(tag.getName().toLowerCase());
							tag.setCreatedOn(new Timestamp(System.currentTimeMillis()));
							TagsRecord t = context.newRecord(TAGS, tag);
							t.store();
							tag.setId(t.getId());
							tagIds.put(tag.getName(), tag.getId());
						}
						else
						{
							tag.setId(existing);
						}
					}

					for (Images image : images)
					{
						ImageTagsRecord it = context.selectFrom(IMAGE_TAGS).where(IMAGE_TAGS.TAG_ID.eq(tag.getId())).and(IMAGE_TAGS.IMAGE_ID.eq(image.getId())).fetchAny();

						if (it == null)
						{
							it = context.newRecord(IMAGE_TAGS);
							it.setImageId(image.getId());
							it.setTagId(tag.getId());
							it.store();
						}
					}
				}

				// Run this in a separate thread, we don't need to wait for it to finish
				new Thread(() -> {
					try
					{
						TagUtils.addTagToFileOrFolder(new File(Frickl.BASE_PATH, album.getPath()), tagStrings);
					}
					catch (IOException e)
					{
						Logger.getLogger("").severe(e.getMessage());
						e.printStackTrace();
					}
				}).start();
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return Response.ok(true).build();
	}

	@DELETE
	@Path("/null/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRootTags(Tags[] tags)
		throws IOException, SQLException
	{
		return this.deleteAlbumTags(null, tags);
	}

	@DELETE
	@Path("/{albumId}/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteAlbumTags(@PathParam("albumId") Integer albumId, Tags[] tags)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		if (albumId != null && tags != null && tags.length > 0)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				Albums album = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(albumId)).fetchAnyInto(Albums.class);

				List<Images> images = context.selectFrom(IMAGES)
											 .where(IMAGES.ALBUM_ID.eq(albumId))
											 .fetchInto(Images.class);

				if (images == null || images.size() < 1)
					return Response.status(Response.Status.NOT_FOUND).build();

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
					try
					{
						TagUtils.deleteTagFromFileOrFolder(new File(Frickl.BASE_PATH, album.getPath()), tagNames);
					}
					catch (IOException e)
					{
						Logger.getLogger("").severe(e.getMessage());
						e.printStackTrace();
					}
				}).start();

				// TODO: Delete all tags that have no more images associated with them
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
		}

		return Response.ok(true).build();
	}

	@GET
	@Path("/null/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getRootTags()
		throws SQLException, IOException
	{
		return this.getAlbumTags(null);
	}

	@GET
	@Path("/{albumId}/tag")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getAlbumTags(@PathParam("albumId") Integer albumId)
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

			if (auth)
			{
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
			}

			return Response.ok(step.orderBy(TAGS.NAME)
								   .offset(pageSize * currentPage)
								   .limit(pageSize)
								   .fetch()
								   .into(Tags.class))
						   .build();
		}
	}

	@GET
	@Path("/null/public")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getRootPublic(@QueryParam("public") Boolean publicParam)
		throws IOException, SQLException
	{
		return this.getAlbumPublic(null, publicParam);
	}

	@GET
	@Path("/{albumId}/public")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getAlbumPublic(@PathParam("albumId") Integer albumId, @QueryParam("public") Boolean publicParam)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		if (publicParam == null || albumId == null)
			return Response.status(Response.Status.BAD_REQUEST).build();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			context.update(IMAGES)
				   .set(IMAGES.IS_PUBLIC, (byte) (publicParam ? 1 : 0))
				   .where(IMAGES.ALBUM_ID.eq(albumId))
				   .execute();
		}

		return Response.ok(true).build();
	}

	@POST
	@Path("/null/accesstoken")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postRootAccessToken(AccessToken accessToken)
		throws IOException, SQLException
	{
		return this.postAlbumAccessToken(null, accessToken);
	}

	@POST
	@Path("/{albumId}/accesstoken")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postAlbumAccessToken(@PathParam("albumId") Integer albumId, AccessToken accessToken)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		if (accessToken == null || StringUtils.isEmpty(accessToken.getToken()) || albumId == null)
			return Response.status(Response.Status.BAD_REQUEST).build();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			try
			{
				UUID.fromString(accessToken.getToken());
			}
			catch (IllegalArgumentException | NullPointerException e)
			{
				return Response.status(Response.Status.BAD_REQUEST).build();
			}

			AccessTokensRecord token = context.newRecord(ACCESS_TOKENS);
			token.setToken(accessToken.getToken());
			token.setExpiresOn(accessToken.getExpiresOn());
			token.store();

			AlbumTokensRecord albumToken = context.newRecord(ALBUM_TOKENS);
			albumToken.setAlbumId(albumId);
			albumToken.setAccessTokenId(token.getId());
			return Response.ok(albumToken.store() > 0).build();
		}
	}

	@GET
	@Path("/{albumId}/scan")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startImageScanner(@PathParam("albumId") Integer albumId)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			AlbumsRecord album = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(albumId)).fetchAny();
			File basePath = new File(Frickl.BASE_PATH);
			File folder = new File(basePath, album.getPath());
			ApplicationListener.startImageScanner(folder);
		}

		return Response.ok(true).build();
	}

	@GET
	@Path("/xago")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getImagesXago(@QueryParam("year") Integer year)
		throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select().from(ALBUM_STATS);

			Field<Serializable> date = DSL.greatest(DSL.coalesce(ALBUM_STATS.NEWEST_IMAGE, 0), DSL.coalesce(ALBUM_STATS.CREATED_ON, 0));

			step.where(date.between(DSL.dateSub(DSL.dateSub(DSL.currentDate(), year, DatePart.YEAR), 7, DatePart.DAY), DSL.dateAdd(DSL.dateSub(DSL.currentDate(), year, DatePart.YEAR), 7, DatePart.DAY)));

			if (auth)
			{
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
			}

			return Response.ok(step.orderBy(date.desc(), ALBUM_STATS.NAME.desc())
								   .limit(pageSize)
								   .offset(pageSize * currentPage)
								   .fetch()
								   .into(AlbumStats.class))
						   .build();
		}
	}
}
