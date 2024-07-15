package raubach.frickl.next.resource.album;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jhi.oddjob.JobInfo;
import org.apache.commons.io.FileUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import raubach.frickl.next.*;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.tables.pojos.*;
import raubach.frickl.next.codegen.tables.records.AlbumsRecord;
import raubach.frickl.next.pojo.*;
import raubach.frickl.next.resource.PaginatedServerResource;
import raubach.frickl.next.util.*;
import raubach.frickl.next.util.async.AlbumZipExporter;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.File;
import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static raubach.frickl.next.codegen.tables.AlbumStats.ALBUM_STATS;
import static raubach.frickl.next.codegen.tables.Albums.ALBUMS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;

@Path("album")
public class AlbumResource extends PaginatedServerResource
{
	@GET
	@Path("/{albumId:\\d+}/hierarchy")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response getImageAlbumHierarchy(@PathParam("albumId") Integer albumId)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			Set<Integer> albumsForUser = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);

			List<Albums> hierarchy = new ArrayList<>();

			Albums current = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(albumId)).fetchAnyInto(Albums.class);

			if (current != null)
			{
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

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response postAlbums(AlbumRequest request)
			throws SQLException
	{
		return postAlbumById(null, request);
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(Permission.ALBUM_DELETE)
	public Response deleteAlbums(List<Integer> albumIds)
			throws SQLException
	{
		if (CollectionUtils.isEmpty(albumIds))
			return Response.status(Response.Status.BAD_REQUEST).build();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			return deleteAlbums(context, albumIds);
		}
	}

	@GET
	@Path("/{albumId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response getAlbumById(@PathParam("albumId") Integer albumId)
			throws SQLException
	{
		PaginatedResult<List<Album>> albums = getAlbums(albumId, new AlbumRequest());
		if (!CollectionUtils.isEmpty(albums.getData()))
			return Response.ok(albums.getData().get(0)).build();
		else
			return Response.ok(null).build();
	}

	@DELETE
	@Path("/{albumId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(Permission.ALBUM_DELETE)
	public Response deleteAlbum(@PathParam("albumId") Integer albumId)
			throws SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			return deleteAlbums(context, Collections.singletonList(albumId));
		}
	}

	private Response deleteAlbums(DSLContext context, List<Integer> startingIds)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		Set<Integer> albumsForUser = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);

		for (Integer id : startingIds)
		{
			if (!albumsForUser.contains(id))
				return Response.status(Response.Status.FORBIDDEN).build();
		}

		Set<Integer> ids = new HashSet<>(startingIds);

		while (true)
		{
			List<Integer> albumIds = context.select(ALBUMS.ID).from(ALBUMS).where(ALBUMS.PARENT_ALBUM_ID.in(ids)).andNot(ALBUMS.ID.in(ids)).fetchInto(Integer.class);

			if (CollectionUtils.isEmpty(albumIds))
				break;
			else
				ids.addAll(albumIds);
		}

		// Now get all the albums in reverse order so we're deleting the child albums first
		List<AlbumsRecord> albums = context.selectFrom(ALBUMS).where(ALBUMS.ID.in(ids)).orderBy(ALBUMS.ID.desc()).fetchInto(AlbumsRecord.class);

		for (AlbumsRecord album : albums)
		{
			File location = new File(Frickl.BASE_PATH);
			File albumFolder = new File(location, album.getPath());

			if (albumFolder.exists() && albumFolder.isDirectory())
			{
				try
				{
					FileUtils.deleteDirectory(albumFolder);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					Logger.getLogger("").severe(e.getMessage());
				}
			}

			album.delete();
		}

		UserAlbumAccessStore.initialize();

		return Response.ok().build();
	}

	@POST
	@Path("/{albumId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response postAlbumById(@PathParam("albumId") Integer albumId, AlbumRequest request)
			throws SQLException
	{
		processRequest(request);

		return Response.ok(getAlbums(albumId, request))
					   .build();
	}

	private PaginatedResult<List<Album>> getAlbums(Integer albumId, AlbumRequest request)
			throws SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			SelectSelectStep<Record> select = context.select();

			if (previousCount == -1)
				select.hint("SQL_CALC_FOUND_ROWS");

			SelectJoinStep<Record> step = select.from(ALBUM_STATS);

			if (albumId != null)
			{
				step.where(ALBUM_STATS.ID.eq(albumId));
			}
			else if (request.getParentAlbumId() != null)
			{
				if (request.getParentAlbumId() != -1)
					step.where(ALBUM_STATS.PARENT_ALBUM_ID.eq(request.getParentAlbumId()));
				else
					step.where(ALBUM_STATS.PARENT_ALBUM_ID.isNull());
			}

			if (request != null && !StringUtils.isEmpty(request.getSearchTerm()))
				step.where(ALBUM_STATS.NAME.containsIgnoreCase(request.getSearchTerm()));

			boolean onlyPublic = false;
			AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
			// Restrict to only albums containing at least one public image
			if (Permission.IS_ADMIN.allows(userDetails.getPermissions()))
			{
				// Nothing required here, admins can see everything
			}
			else if (StringUtils.isEmpty(userDetails.getToken()))
			{
				// Check if the album contains public images
				onlyPublic = true;
				step.where(DSL.exists(DSL.selectOne()
										 .from(IMAGES)
										 .where(IMAGES.ALBUM_ID.eq(ALBUM_STATS.ALBUM_ID)
															   .and(IMAGES.IS_PUBLIC.eq((byte) 1)))));
			}
			else
			{
				// Check user permissions for the album
				Set<Integer> albumAccess = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);
				step.where(ALBUM_STATS.ALBUM_ID.in(albumAccess));
			}

			final boolean b = onlyPublic;

			List<Album> result = setPaginationAndOrderBy(step)
					.fetchInto(AlbumStats.class)
					.stream()
					.map(a -> new Album(a, b))
					.collect(Collectors.toList());

			long count = previousCount == -1 ? context.fetchOne("SELECT FOUND_ROWS()").into(Long.class) : previousCount;

			return new PaginatedResult<>(result, count);
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(Permission.ALBUM_CREATE)
	public Response putAlbum(Albums album)
			throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		if (album == null || StringUtils.isEmpty(album.getName()))
			return Response.status(Response.Status.BAD_REQUEST).build();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			File base = new File(Frickl.BASE_PATH);
			File location = new File(Frickl.BASE_PATH);
			// Check the parent album exists
			if (album.getParentAlbumId() != null)
			{
				if (!Permission.IS_ADMIN.allows(userDetails.getPermissions()))
				{
					// Check the user has access to that parent album
					Set<Integer> albumAccess = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);
					if (!albumAccess.contains(album.getParentAlbumId()))
						return Response.status(Response.Status.FORBIDDEN).build();
				}

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
			record.setCreatedBy(userDetails.getId());
			record.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			boolean result = record.store() > 0;

			UserAlbumAccessStore.initialize();

			return Response.ok(result).build();
		}
	}

	@GET
	@Path("/{albumId:\\d+}/scan")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(Permission.IMAGE_UPLOAD)
	public Response startImageScanner(@PathParam("albumId") Integer albumId)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			if (!Permission.IS_ADMIN.allows(userDetails.getPermissions()))
			{
				// Check the user has access to that album
				Set<Integer> albumAccess = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);
				if (!albumAccess.contains(albumId))
					return Response.status(Response.Status.FORBIDDEN).build();
			}

			AlbumsRecord album = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(albumId)).fetchAny();
			if (album == null)
				return Response.status(Response.Status.NOT_FOUND).build();
			File basePath = new File(Frickl.BASE_PATH);
			File folder = new File(basePath, album.getPath());
			ApplicationListener.startImageScanner(folder);
		}

		return Response.ok(true).build();
	}

	@GET
	@Path("/{albumId:\\d+}/download")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response downloadAlbum(@PathParam("albumId") Integer albumId)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			if (!Permission.IS_ADMIN.allows(userDetails.getPermissions()))
			{
				Set<Integer> albumsForUser = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);
				if (!albumsForUser.contains(albumId))
					return Response.status(Response.Status.FORBIDDEN).build();
			}

			AlbumRequest r = new AlbumRequest();
			r.setPage(0);
			r.setLimit(1);
			PaginatedResult<List<Album>> albums = getAlbums(albumId, r);

			Album album = null;
			if (CollectionUtils.isEmpty(albums.getData()))
				return Response.status(Response.Status.NOT_FOUND).build();
			else
				album = albums.getData().get(0);

			String uuid = UUID.randomUUID().toString();

			String version = PropertyWatcher.get(ServerProperty.API_VERSION);
			File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-exports" + "-" + version);
			File targetFolder = new File(folder, uuid);
			targetFolder.mkdirs();
			File libFolder = ResourceUtils.getLibFolder();
			List<String> args = new ArrayList<>();
			args.add("-cp");
			args.add(libFolder.getAbsolutePath() + File.separator + "*");
			args.add(AlbumZipExporter.class.getCanonicalName());
			args.add(StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_SERVER)));
			args.add(StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_NAME)));
			args.add(StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_PORT)));
			args.add(StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_USERNAME)));
			args.add(StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_PASSWORD)));
			args.add(Integer.toString(albumId));
			args.add(Frickl.BASE_PATH);
			args.add(targetFolder.getAbsolutePath());
			args.add(StringUtils.orEmptyQuotes(userDetails != null ? userDetails.getToken() : ""));

			JobInfo info = ApplicationListener.SCHEDULER.submit("AlbumZipExporter", "java", args, targetFolder.getAbsolutePath());
			AsyncExportResult result = new AsyncAlbumExportResult()
					.setAlbumName(album.getName())
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
