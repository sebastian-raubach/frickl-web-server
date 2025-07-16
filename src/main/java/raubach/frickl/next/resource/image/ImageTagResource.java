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
import raubach.frickl.next.codegen.tables.records.*;
import raubach.frickl.next.resource.*;
import raubach.frickl.next.util.*;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.*;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import static raubach.frickl.next.codegen.tables.ImageTags.IMAGE_TAGS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;
import static raubach.frickl.next.codegen.tables.Tags.TAGS;

@Path("image/{imageId:\\d+}/tag")
public class ImageTagResource extends PaginatedServerResource
{
	@PathParam("imageId")
	Integer imageId;

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	@Secured
	public Response getImageTags()
			throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

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
	@Secured(Permission.TAG_DELETE)
	public Response deleteTagFromImage(@PathParam("tagId") Integer tagId)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		if (imageId != null && tagId != null)
		{
			try (Connection conn = Database.getConnection())
			{
				DSLContext context = Database.getContext(conn);
				SelectConditionStep<ImagesRecord> step = context.selectFrom(IMAGES)
																 .where(IMAGES.ID.eq(imageId));

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

				Images image = step.fetchAnyInto(Images.class);

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

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(Permission.TAG_ADD)
	public Response postImageTags(String[] tags)
			throws IOException, SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		boolean result = false;
		if (tags != null && tags.length > 0 && imageId != null)
		{
			List<String> tagList = Arrays.stream(tags).map(String::toLowerCase).toList();

			try (Connection conn = Database.getConnection())
			{
				DSLContext context = Database.getContext(conn);
				SelectConditionStep<ImagesRecord> step = context.selectFrom(IMAGES)
																.where(IMAGES.ID.eq(imageId));

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

				Images image = step.fetchAnyInto(Images.class);

				Map<String, Integer> tagIds = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

				context.selectFrom(TAGS)
					   .where(TAGS.NAME.in(tagList))
					   .forEach(t -> {
						   if (!tagIds.containsKey(t.getName()))
							   tagIds.put(t.getName(), t.getId());
					   });

				for (String tag : tagList)
				{
					Integer id =  tagIds.get(tag);
					if (id == null)
					{
						TagsRecord t = context.newRecord(TAGS);
						t.setName(tag);
						t.setCreatedOn(new Timestamp(System.currentTimeMillis()));
						t.store();
						tagIds.put(tag, t.getId());
						id = t.getId();
					}

					ImageTagsRecord it = context.selectFrom(IMAGE_TAGS).where(IMAGE_TAGS.TAG_ID.eq(id)).and(IMAGE_TAGS.IMAGE_ID.eq(imageId)).fetchAny();

					if (it == null)
					{
						it = context.newRecord(IMAGE_TAGS);
						it.setImageId(imageId);
						it.setTagId(id);
						it.store();
					}
				}

				if (image.getDataType() != ImagesDataType.video)
				{
					// Run this in a separate thread, we don't need to wait for it to finish
					new Thread(() -> {
						File file = new File(Frickl.BASE_PATH, image.getPath());
						try
						{
							TagUtils.addTagToFileOrFolder(file, tagList);
						}
						catch (IOException e)
						{
							Logger.getLogger("").severe(e.getMessage());
							e.printStackTrace();
						}
					}).start();
				}
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST)
						   .build();
		}

		return Response.ok()
					   .build();
	}
}
