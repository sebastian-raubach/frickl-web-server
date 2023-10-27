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
import raubach.frickl.next.resource.AbstractAccessTokenResource;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.sql.*;

import static raubach.frickl.next.codegen.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.frickl.next.codegen.tables.AlbumTokens.ALBUM_TOKENS;
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
public class StatsResource extends AbstractAccessTokenResource
{
	@GET
	@Path("/year/{year:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getYearData(@PathParam("year") Integer year)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			Field<Date> date = IMAGES.CREATED_ON.cast(Date.class).as("date");

			SelectConditionStep<?> step = context.select(date, DSL.count().as("count"))
												 .from(IMAGES)
												 .where(IMAGES.CREATED_ON.isNotNull())
												 .and(DSL.year(IMAGES.CREATED_ON).eq(year));

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
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			Field<Integer> date = DSL.year(IMAGES.CREATED_ON).as("year");

			SelectConditionStep<Record2<Integer, Integer>> step = context.select(date, DSL.count().as("count"))
																		 .from(IMAGES)
																		 .where(IMAGES.CREATED_ON.isNotNull());

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
		boolean auth = PropertyWatcher.authEnabled();

		Counts result = new Counts();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			// Get image count
			SelectJoinStep<Record1<Integer>> step = context.selectCount().from(IMAGES);
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
			result.setImages(step.fetchOne(0, int.class));

			// Get album count
			step = context.selectCount().from(ALBUMS);
			if (auth)
			{
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
					step.whereExists(DSL.selectOne()
										.from(IMAGES)
										.where(IMAGES.ALBUM_ID.eq(ALBUMS.ID))
										.and(IMAGES.IS_PUBLIC.eq((byte) 1)));
				}
			}
			result.setAlbums(step.fetchOne(0, int.class));

			// Get tags
			step = context.selectCount().from(TAGS);
			if (auth)
			{
				if (!StringUtils.isEmpty(accessToken))
				{
					step.whereExists(DSL.selectOne()
										.from(IMAGE_TAGS)
										.leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID))
										.leftJoin(ALBUMS).on(ALBUMS.ID.eq(IMAGES.ALBUM_ID))
										.leftJoin(ALBUM_TOKENS).on(ALBUM_TOKENS.ALBUM_ID.eq(ALBUMS.ID))
										.leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
										.where(ACCESS_TOKENS.TOKEN.eq(accessToken))
										.and(IMAGE_TAGS.TAG_ID.eq(TAGS.ID)));
				}
				else if (StringUtils.isEmpty(userDetails.getToken()))
				{
					step.whereExists(DSL.selectOne()
										.from(IMAGE_TAGS)
										.leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID))
										.where(IMAGES.IS_PUBLIC.eq((byte) 1))
										.and(IMAGE_TAGS.TAG_ID.eq(TAGS.ID)));
				}
				else
				{
					step.whereExists(DSL.selectOne().from(IMAGE_TAGS).where(IMAGE_TAGS.TAG_ID.eq(TAGS.ID)));
				}
			}
			else
			{
				step.whereExists(DSL.selectOne().from(IMAGE_TAGS).where(IMAGE_TAGS.TAG_ID.eq(TAGS.ID)));
			}

			result.setTags(step.fetchOne(0, int.class));

			// Get favorites
			step = context.selectCount().from(IMAGES);
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
