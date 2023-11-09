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
import raubach.frickl.next.resource.AbstractAccessTokenResource;
import raubach.frickl.next.util.*;
import raubach.frickl.next.util.async.ImageZipExporter;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.File;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import static raubach.frickl.next.codegen.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.frickl.next.codegen.tables.AlbumStats.ALBUM_STATS;
import static raubach.frickl.next.codegen.tables.AlbumTokens.ALBUM_TOKENS;
import static raubach.frickl.next.codegen.tables.Albums.ALBUMS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;

@Path("album")
@Secured
public class AlbumResource extends AbstractAccessTokenResource
{
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response postAlbums(AlbumRequest request)
			throws SQLException
	{
		return postAlbumById(null, request);
	}

	@POST
	@Path("/{albumId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
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
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			SelectSelectStep<Record> select = context.select();

			if (previousCount == -1)
				select.hint("SQL_CALC_FOUND_ROWS");

			SelectJoinStep<Record> step = select.from(ALBUM_STATS);

			if (albumId != null)
				step.where(ALBUM_STATS.ID.eq(albumId));
			else if (request.getParentAlbumId() != null && request.getParentAlbumId() != -1)
				step.where(ALBUM_STATS.PARENT_ALBUM_ID.eq(request.getParentAlbumId()));
			else
				step.where(ALBUM_STATS.PARENT_ALBUM_ID.isNull());

			boolean onlyPublic = false;
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
					onlyPublic = true;
					step.where(DSL.exists(DSL.selectOne()
											 .from(IMAGES)
											 .where(IMAGES.ALBUM_ID.eq(ALBUM_STATS.ID)
																   .and(IMAGES.IS_PUBLIC.eq((byte) 1)))));
				}
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
	public Response postAlbum(Albums album)
			throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

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

	@GET
	@Path("/{albumId:\\d+}/scan")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startImageScanner(@PathParam("albumId") Integer albumId)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
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
	@Path("/download/{uuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("application/zip")
	@PermitAll
	public Response downloadAlbumByToken(@PathParam("uuid") String uuid)
			throws SQLException
	{
		String jobId = ApplicationListener.SCHEDULER_IDS.get(uuid);

		try
		{
			if (ApplicationListener.SCHEDULER.isJobFinished(jobId))
			{
				String version = PropertyWatcher.get(ServerProperty.API_VERSION);
				File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-exports" + "-" + version);
				File targetFolder = new File(folder, uuid);
				// Get zip result files (there'll only be one per folder)
				File[] zipFiles = targetFolder.listFiles((dir, name) -> name.endsWith(".zip"));

				if (!CollectionUtils.isEmpty(zipFiles))
				{
					java.nio.file.Path zipFilePath = zipFiles[0].toPath();
					return Response.ok((StreamingOutput) output -> {
									   java.nio.file.Files.copy(zipFilePath, output);
									   // Delete the whole folder once we're done
									   FileUtils.deleteDirectory(targetFolder);
								   })
								   .type("application/zip")
								   .header("content-disposition", "attachment;filename= \"" + zipFiles[0].getName() + "\"")
								   .header("content-length", zipFiles[0].length())
								   .build();
				}
				else
				{
					return Response.status(Response.Status.NOT_FOUND).build();
				}
			}
			else
			{
				return Response.status(Response.Status.ACCEPTED).build();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/{albumId:\\d+}/download")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response downloadAlbum(@PathParam("albumId") Integer albumId)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

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
			args.add(ImageZipExporter.class.getCanonicalName());
			args.add(StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_SERVER)));
			args.add(StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_NAME)));
			args.add(StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_PORT)));
			args.add(StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_USERNAME)));
			args.add(StringUtils.orEmptyQuotes(PropertyWatcher.get(ServerProperty.DATABASE_PASSWORD)));
			args.add(Integer.toString(albumId));
			args.add(Frickl.BASE_PATH);
			args.add(targetFolder.getAbsolutePath());
			args.add(StringUtils.orEmptyQuotes(userDetails != null ? userDetails.getToken() : ""));
			args.add(StringUtils.orEmptyQuotes(accessToken));

			JobInfo info = ApplicationListener.SCHEDULER.submit("ImageZipExporter", "java", args, targetFolder.getAbsolutePath());
			ApplicationListener.SCHEDULER_IDS.put(uuid, info.getId());

			return Response.ok(new AsyncAlbumExportResult()
									   .setToken(uuid)
									   .setAlbumName(album.getName())
									   .setCreatedOn(new Date(System.currentTimeMillis()))).build();
		}
		catch (Exception e)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
