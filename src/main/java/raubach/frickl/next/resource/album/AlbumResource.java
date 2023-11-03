package raubach.frickl.next.resource.album;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.*;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.tables.pojos.AlbumStats;
import raubach.frickl.next.codegen.tables.records.AlbumsRecord;
import raubach.frickl.next.pojo.*;
import raubach.frickl.next.resource.AbstractAccessTokenResource;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.File;
import java.sql.*;
import java.util.List;
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
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		processRequest(request);
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

			return Response.ok(new PaginatedResult<>(result, count))
						   .build();
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
}
