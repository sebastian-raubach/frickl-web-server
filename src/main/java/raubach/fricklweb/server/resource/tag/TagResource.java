package raubach.fricklweb.server.resource.tag;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.computed.TagCount;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.resource.AbstractAccessTokenResource;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static raubach.fricklweb.server.database.tables.AccessTokens.*;
import static raubach.fricklweb.server.database.tables.AlbumTokens.*;
import static raubach.fricklweb.server.database.tables.ImageTags.*;
import static raubach.fricklweb.server.database.tables.Images.*;
import static raubach.fricklweb.server.database.tables.Tags.*;

/**
 * @author Sebastian Raubach
 */
@Path("tag")
@Secured
@PermitAll
public class TagResource extends AbstractAccessTokenResource
{
	@GET
	@Path("/{tagId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<TagCount> getTagsById(@PathParam("tagId") Integer tagId)
		throws SQLException, IOException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select(TAGS.asterisk(), DSL.count().as("count"))
												 .from(IMAGE_TAGS.leftJoin(TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
																 .leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID)));

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

			if (tagId != null)
				step.where(TAGS.ID.eq(tagId));

			List<TagCount> tagCounts = new ArrayList<>();
			step.groupBy(TAGS.ID)
				.orderBy(TAGS.NAME)
				.limit(pageSize)
				.offset(pageSize * currentPage)
				.fetchStream()
				.forEach(t -> {
					Tags tag = new Tags(t.get(TAGS.ID), t.get(TAGS.NAME), t.get(TAGS.CREATED_ON), t.get(TAGS.UPDATED_ON));
					Integer count = t.get("count", Integer.class);

					tagCounts.add(new TagCount(tag, count));
				});

			return tagCounts;
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<TagCount> getTags()
		throws IOException, SQLException
	{
		return this.getTagsById(null);
	}

	@GET
	@Path("/{tagId}/image")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Images> getTagImages(@PathParam("tagId") Integer tagId)
		throws SQLException, IOException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (tagId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectJoinStep<Record> step = context.select().from(TAGS
					.leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
					.leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID)));

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

				return step.where(TAGS.ID.eq(tagId))
						   .offset(pageSize * currentPage)
						   .limit(pageSize)
						   .fetch()
						   .into(Images.class);
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}
	}

	@GET
	@Path("/{tagId}/image/count")
	public int getTagImageCount(@PathParam("tagId") Integer tagId)
		throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (tagId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectJoinStep<Record1<Integer>> step = context.selectCount().from(TAGS
					.leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
					.leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID)));

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

				return step.where(TAGS.ID.eq(tagId))
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
