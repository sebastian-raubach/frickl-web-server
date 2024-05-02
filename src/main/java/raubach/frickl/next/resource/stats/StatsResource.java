package raubach.frickl.next.resource.stats;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.Database;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.pojo.*;
import raubach.frickl.next.resource.PaginatedServerResource;
import raubach.frickl.next.util.*;

import java.sql.*;
import java.util.Set;

import static raubach.frickl.next.codegen.tables.Albums.ALBUMS;
import static raubach.frickl.next.codegen.tables.ImageTags.IMAGE_TAGS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;
import static raubach.frickl.next.codegen.tables.Tags.TAGS;

/**
 * @author Sebastian Raubach
 */
@Path("stats")
@Secured
@PermitAll
public class StatsResource extends PaginatedServerResource
{
	@GET
	@Path("/year/{year:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getYearData(@PathParam("year") Integer year)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			Field<Date> date = IMAGES.CREATED_ON.cast(Date.class).as("date");

			SelectConditionStep<?> step = context.select(date, DSL.count().as("count"))
												 .from(IMAGES)
												 .where(IMAGES.CREATED_ON.isNotNull())
												 .and(DSL.year(IMAGES.CREATED_ON).eq(year));

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

			return Response.ok(step.groupBy(date)
								   .orderBy(date)
								   .fetchInto(YearCount.class))
						   .build();
		}
	}

	@GET
	@Path("/year")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getYears()
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			Field<Integer> date = DSL.year(IMAGES.CREATED_ON).as("year");

			SelectConditionStep<Record2<Integer, Integer>> step = context.select(date, DSL.count().as("count"))
																		 .from(IMAGES)
																		 .where(IMAGES.CREATED_ON.isNotNull());

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

			return Response.ok(step.groupBy(date)
								   .orderBy(date)
								   .fetchInto(YearCounts.class))
						   .build();
		}
	}

	@GET
	@Path("/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCounts()
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		Counts result = new Counts();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			// Get image count
			SelectJoinStep<Record1<Integer>> step = context.selectCount().from(IMAGES);
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
			result.setImages(step.fetchOne(0, int.class));

			// Get album count
			step = context.selectCount().from(ALBUMS);
			// Restrict to only albums containing at least one public image
			if (Permission.IS_ADMIN.allows(userDetails.getPermissions()))
			{
				// Nothing required here, admins can see everything
			}
			else if (StringUtils.isEmpty(userDetails.getToken()))
			{
				// Check if the album contains public images
				step.where(DSL.exists(DSL.selectOne()
										 .from(IMAGES)
										 .where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)
															   .and(IMAGES.IS_PUBLIC.eq((byte) 1)))));
			}
			else
			{
				// Check user permissions for the album
				Set<Integer> albumAccess = UserAlbumAccessStore.getAlbumsForUser(context, userDetails);
				step.where(ALBUMS.ID.in(albumAccess));
			}
			result.setAlbums(step.fetchOne(0, int.class));

			// Get tags
			step = context.selectCount().from(TAGS)
						  .leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
						  .leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID));
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

			result.setTags(step.fetchOne(0, int.class));

			// Get favorites
			step = context.selectCount().from(IMAGES);
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
			result.setFavorites(step.where(IMAGES.IS_FAVORITE.eq((byte) 1)).fetchOne(0, int.class));
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
			return Response.noContent().build();
		}

		return Response.ok(result).build();
	}
}
