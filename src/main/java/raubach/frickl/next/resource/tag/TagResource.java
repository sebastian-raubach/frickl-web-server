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
import raubach.frickl.next.resource.PaginatedServerResource;
import raubach.frickl.next.util.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static raubach.frickl.next.codegen.tables.ImageTags.IMAGE_TAGS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;
import static raubach.frickl.next.codegen.tables.Tags.TAGS;

@Path("tag")
@Secured
@PermitAll
public class TagResource extends PaginatedServerResource
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

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			SelectJoinStep<Record> step = context.select(TAGS.asterisk(), DSL.count().as("count"))
												 .from(IMAGE_TAGS.leftJoin(TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
																 .leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID)));

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
