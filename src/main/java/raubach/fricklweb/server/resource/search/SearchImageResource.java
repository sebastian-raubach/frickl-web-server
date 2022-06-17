package raubach.fricklweb.server.resource.search;

import org.jooq.*;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.database.tables.pojos.Images;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.IOException;
import java.sql.*;
import java.util.List;

import static raubach.fricklweb.server.database.tables.ImageTags.*;
import static raubach.fricklweb.server.database.tables.Images.*;
import static raubach.fricklweb.server.database.tables.Tags.*;

/**
 * @author Sebastian Raubach
 */
@Path("search/{searchTerm}/image")
@Secured
public class SearchImageResource extends PaginatedServerResource
{
	@PathParam("searchTerm")
	String searchTerm;


	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSearchImages()
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (searchTerm != null)
		{
			searchTerm = "%" + searchTerm.replace(" ", "%") + "%";
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectOnConditionStep<Record> step = context.select(IMAGES.asterisk()).from(IMAGES)
															.leftJoin(IMAGE_TAGS).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID))
															.leftJoin(TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID));

				if (auth && StringUtils.isEmpty(userDetails.getToken()))
					step.where(IMAGES.IS_PUBLIC.eq((byte) 1));

				return Response.ok(step.where(TAGS.NAME.like(searchTerm)
										   .or(IMAGES.PATH.like(searchTerm)))
						   .groupBy(IMAGES.ID)
						   .offset(pageSize * currentPage)
						   .limit(pageSize)
						   .fetch()
						   .into(Images.class))
					.build();
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path("/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSearchImageCount()
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (searchTerm != null)
		{
			searchTerm = "%" + searchTerm.replace(" ", "%") + "%";
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectOnConditionStep<Record1<Integer>> step = context.selectCount().from(IMAGES)
																	  .leftJoin(IMAGE_TAGS).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID))
																	  .leftJoin(TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID));

				if (auth && StringUtils.isEmpty(userDetails.getToken()))
					step.where(IMAGES.IS_PUBLIC.eq((byte) 1));

				return Response.ok(step.where(TAGS.NAME.like(searchTerm)
										   .or(IMAGES.PATH.like(searchTerm)))
						   .fetchAny(0, int.class))
					.build();
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
}
