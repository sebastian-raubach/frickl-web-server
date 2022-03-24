package raubach.fricklweb.server.resource.search;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.database.tables.pojos.Albums;
import raubach.fricklweb.server.database.tables.records.AlbumsRecord;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.IOException;
import java.sql.*;
import java.util.List;

import static raubach.fricklweb.server.database.tables.Albums.*;
import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * @author Sebastian Raubach
 */
@Path("search/{searchTerm}")
@Secured
@PermitAll
public class SearchAlbumResource extends PaginatedServerResource
{
	@PathParam("searchTerm")
	String searchTerm;

	@GET
	@Path("/album")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Albums> getAlbums()
		throws SQLException, IOException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (searchTerm != null)
		{
			searchTerm = "%" + searchTerm.replace(" ", "%") + "%";
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectWhereStep<AlbumsRecord> step = context.selectFrom(ALBUMS);

				if (auth && StringUtils.isEmpty(userDetails.getToken()))
					step.where(DSL.exists(DSL.selectOne()
											 .from(IMAGES)
											 .where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)
																   .and(IMAGES.IS_PUBLIC.eq((byte) 1)))));

				return step.where(ALBUMS.NAME.like(searchTerm)
											 .or(ALBUMS.DESCRIPTION.like(searchTerm)))
						   .offset(pageSize * currentPage)
						   .limit(pageSize)
						   .fetch()
						   .into(Albums.class);
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}
	}

	@GET
	@Path("/album/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public int getAlbumCount()
		throws SQLException, IOException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (searchTerm != null)
		{
			searchTerm = "%" + searchTerm.replace(" ", "%") + "%";
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectJoinStep<Record1<Integer>> step = context.selectCount().from(ALBUMS);

				if (auth && StringUtils.isEmpty(userDetails.getToken()))
					step.where(DSL.exists(DSL.selectOne()
											 .from(IMAGES)
											 .where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)
																   .and(IMAGES.IS_PUBLIC.eq((byte) 1)))));

				return step.where(ALBUMS.NAME.like(searchTerm)
											 .or(ALBUMS.DESCRIPTION.like(searchTerm)))
						   .fetchAny(0, int.class);
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return 0;
		}
	}
}
