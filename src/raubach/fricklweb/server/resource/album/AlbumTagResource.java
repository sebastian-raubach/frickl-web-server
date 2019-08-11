package raubach.fricklweb.server.resource.album;

import org.jooq.*;
import org.jooq.impl.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.*;

import org.restlet.resource.Delete;
import raubach.fricklweb.server.*;
import raubach.fricklweb.server.database.tables.pojos.*;
import raubach.fricklweb.server.database.tables.records.ImageTagsRecord;
import raubach.fricklweb.server.database.tables.records.TagsRecord;
import raubach.fricklweb.server.resource.*;
import raubach.fricklweb.server.util.*;

import static raubach.fricklweb.server.database.tables.Albums.*;
import static raubach.fricklweb.server.database.tables.ImageTags.*;
import static raubach.fricklweb.server.database.tables.Images.*;
import static raubach.fricklweb.server.database.tables.Tags.*;

/**
 * @author Sebastian Raubach
 */
public class AlbumTagResource extends PaginatedServerResource
{
	private Integer albumId = null;

	@Override
	protected void doInit()
		throws ResourceException
	{
		super.doInit();

		try
		{
			albumId = Integer.parseInt(getRequestAttributes().get("albumId").toString());
		}
		catch (Exception e)
		{
		}
	}

	@Delete("json")
	public void deleteJson(Tags[] tags)
	{
		if (albumId != null && tags != null && tags.length > 0)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				List<Images> images = context.selectFrom(IMAGES)
						.where(IMAGES.ALBUM_ID.eq(albumId))
						.fetchInto(Images.class);

				if (images == null || images.size() < 1)
					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

				List<Integer> imageIds = images.stream().map(Images::getId).collect(Collectors.toList());

				List<Integer> tagIds = new ArrayList<>();
				List<String> tagNames = new ArrayList<>();
				for(Tags tag : tags) {
					tagIds.add(tag.getId());
					tagNames.add(tag.getName());
				}

				context.deleteFrom(IMAGE_TAGS)
						.where(IMAGE_TAGS.IMAGE_ID.in(imageIds))
						.and(IMAGE_TAGS.TAG_ID.in(tagIds))
						.execute();

				// Run this in a separate thread, we don't need to wait for it to finish
				new Thread(() -> {
					for(Images image : images) {
						File file = new File(Frickl.BASE_PATH, image.getPath());
						try {
							TagUtils.deleteTagFromImage(file, tagNames);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();

				// TODO: Delete all tags that have no more images associated with them
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	@Post("json")
	public void postJson(Tags[] tags)
	{
		if (albumId != null && tags != null && tags.length > 0)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				List<Images> images = context.selectFrom(IMAGES)
											 .where(IMAGES.ALBUM_ID.eq(albumId))
											 .fetchInto(Images.class);

				List<String> tagStrings = new ArrayList<>();
				for(Tags tag : tags)
					tagStrings.add(tag.getName());

				Map<String, Integer> tagIds = context.selectFrom(TAGS)
						.where(TAGS.NAME.in(tagStrings))
						.fetchMap(TAGS.NAME, TAGS.ID);

				// Get all existing ids
				for(Tags tag : tags)
				{
					if (tag.getId() == null)
						tag.setId(tagIds.get(tag.getName()));
				}

				for (Tags tag : tags) {
					// If it doesn't exist, create it
					if (tag.getId() == null) {
						tag.setCreatedOn(new Timestamp(System.currentTimeMillis()));
						TagsRecord t = context.newRecord(TAGS, tag);
						t.store();
						tag.setId(t.getId());
					}

					List<Integer> existingIds = context.select(IMAGE_TAGS.IMAGE_ID)
							.from(IMAGE_TAGS)
							.where(IMAGE_TAGS.TAG_ID.eq(tag.getId()))
							.fetchInto(Integer.class);

					images.removeIf(i -> existingIds.contains(i.getId()));

					InsertValuesStep2<ImageTagsRecord, Integer, Integer> step = context.insertInto(IMAGE_TAGS, IMAGE_TAGS.IMAGE_ID, IMAGE_TAGS.TAG_ID);
					for (Images image : images)
						step.values(image.getId(), tag.getId());
					step.execute();
				}

				// Run this in a separate thread, we don't need to wait for it to finish
				new Thread(() -> {
					for (Images image : images) {
						File file = new File(Frickl.BASE_PATH, image.getPath());
						try
						{
							TagUtils.addTagToImage(file, tagStrings);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}).start();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	@Get("json")
	public List<Tags> getJson()
	{
		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				return context.selectDistinct(TAGS.ID, TAGS.NAME, TAGS.CREATED_ON, TAGS.UPDATED_ON)
							  .from(TAGS
								  .leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
								  .leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID))
								  .leftJoin(ALBUMS).on(ALBUMS.ID.eq(IMAGES.ALBUM_ID)))
							  .where(ALBUMS.ID.eq(albumId))
							  .orderBy(TAGS.NAME)
							  .offset(pageSize * currentPage)
							  .limit(pageSize)
							  .fetch()
							  .into(Tags.class);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}
}
