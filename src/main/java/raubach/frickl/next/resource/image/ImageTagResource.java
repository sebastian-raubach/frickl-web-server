package raubach.frickl.next.resource.image;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.*;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.enums.ImagesDataType;
import raubach.frickl.next.codegen.tables.pojos.*;
import raubach.frickl.next.resource.AbstractAccessTokenResource;
import raubach.frickl.next.util.TagUtils;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.*;
import java.io.File;
import java.sql.*;
import java.util.Collections;
import java.util.logging.Logger;

import static raubach.frickl.next.codegen.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.frickl.next.codegen.tables.AlbumTokens.ALBUM_TOKENS;
import static raubach.frickl.next.codegen.tables.ImageTags.IMAGE_TAGS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;
import static raubach.frickl.next.codegen.tables.Tags.TAGS;

@Path("image/{imageId:\\d+}/tag")
@Secured
public class ImageTagResource extends AbstractAccessTokenResource
{
	@PathParam("imageId")
	Integer imageId;

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getImageTags()
			throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (imageId != null)
		{
			try (Connection conn = Database.getConnection())
			{
				DSLContext context = Database.getContext(conn);
				SelectConditionStep<?> step = context.select(TAGS.ID, TAGS.NAME, TAGS.CREATED_ON, TAGS.UPDATED_ON)
													 .from(TAGS
																   .leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
																   .leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID)))
													 .where(IMAGES.ID.eq(imageId));

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

				return Response.ok(step.orderBy(TAGS.NAME)
									   .offset(pageSize * currentPage)
									   .limit(pageSize)
									   .fetch()
									   .into(Tags.class))
							   .build();
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@DELETE
	@Path("/{tagId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteTagFromImage(@PathParam("tagId") Integer tagId)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(userDetails.getToken()))
			return Response.status(Response.Status.FORBIDDEN).build();

		if (imageId != null && tagId != null)
		{
			try (Connection conn = Database.getConnection())
			{
				DSLContext context = Database.getContext(conn);
				Images image = context.selectFrom(IMAGES)
									  .where(IMAGES.ID.eq(imageId))
									  .fetchAnyInto(Images.class);

				Tags t = context.selectFrom(TAGS)
								.where(TAGS.ID.eq(tagId))
								.fetchAnyInto(Tags.class);

				if (t == null || image == null)
					return Response.status(Response.Status.NOT_FOUND).build();

				int numberOfDeletedItems = context.deleteFrom(IMAGE_TAGS)
												  .where(IMAGE_TAGS.IMAGE_ID.eq(image.getId()))
												  .and(IMAGE_TAGS.TAG_ID.eq(tagId))
												  .execute();

				if (image.getDataType() != ImagesDataType.video)
				{
					File file = new File(Frickl.BASE_PATH, image.getPath());
					try
					{
						TagUtils.deleteTagFromFileOrFolder(file, Collections.singletonList(t.getName()));
					}
					catch (IOException e)
					{
						Logger.getLogger("").severe(e.getMessage());
						e.printStackTrace();
					}
				}

				// TODO: Delete all tags that have no more images associated with them

				return Response.ok(numberOfDeletedItems == 1).build();
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}
}
