package raubach.frickl.next.resource.tag;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.Database;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.tables.pojos.Tags;
import raubach.frickl.next.pojo.TagCount;
import raubach.frickl.next.resource.AbstractAccessTokenResource;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static raubach.frickl.next.codegen.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.frickl.next.codegen.tables.AlbumTokens.ALBUM_TOKENS;
import static raubach.frickl.next.codegen.tables.ImageTags.IMAGE_TAGS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;
import static raubach.frickl.next.codegen.tables.Tags.TAGS;

@Path("tag")
@Secured
@PermitAll
public class TagResource extends AbstractAccessTokenResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTags()
			throws IOException, SQLException
	{
		return this.getTagsById(null);
	}

	@GET
	@Path("/{tagId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTagsById(@PathParam("tagId") Integer tagId)
			throws SQLException, IOException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
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

			return Response.ok(tagCounts).build();
		}
	}
}
