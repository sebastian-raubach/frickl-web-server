package raubach.fricklweb.server.resource.album;

import org.jooq.DSLContext;
import org.jooq.InsertValuesStep2;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.Frickl;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.tables.pojos.Images;
import raubach.fricklweb.server.database.tables.pojos.Tags;
import raubach.fricklweb.server.database.tables.records.ImageTagsRecord;
import raubach.fricklweb.server.database.tables.records.TagsRecord;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.TagUtils;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static raubach.fricklweb.server.database.tables.Albums.ALBUMS;
import static raubach.fricklweb.server.database.tables.ImageTags.IMAGE_TAGS;
import static raubach.fricklweb.server.database.tables.Images.IMAGES;
import static raubach.fricklweb.server.database.tables.Tags.TAGS;

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
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.getBoolean(ServerProperty.AUTHENTICATION_ENABLED);

		if (auth && StringUtils.isEmpty(user.getToken()))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

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
				for (Tags tag : tags)
				{
					tagIds.add(tag.getId());
					tagNames.add(tag.getName());
				}

				context.deleteFrom(IMAGE_TAGS)
						.where(IMAGE_TAGS.IMAGE_ID.in(imageIds))
						.and(IMAGE_TAGS.TAG_ID.in(tagIds))
						.execute();

				// Run this in a separate thread, we don't need to wait for it to finish
				new Thread(() -> {
					for (Images image : images)
					{
						File file = new File(Frickl.BASE_PATH, image.getPath());
						try
						{
							TagUtils.deleteTagFromImage(file, tagNames);
						}
						catch (IOException e)
						{
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
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.getBoolean(ServerProperty.AUTHENTICATION_ENABLED);

		if (auth && StringUtils.isEmpty(user.getToken()))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

		if (albumId != null && tags != null && tags.length > 0)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				List<Images> images = context.selectFrom(IMAGES)
						.where(IMAGES.ALBUM_ID.eq(albumId))
						.fetchInto(Images.class);

				List<String> tagStrings = new ArrayList<>();
				for (Tags tag : tags)
					tagStrings.add(tag.getName());

				Map<String, Integer> tagIds = context.selectFrom(TAGS)
						.where(TAGS.NAME.in(tagStrings))
						.fetchMap(TAGS.NAME, TAGS.ID);

				// Get all existing ids
				for (Tags tag : tags)
				{
					if (tag.getId() == null)
						tag.setId(tagIds.get(tag.getName()));
				}

				for (Tags tag : tags)
				{
					// If it doesn't exist, create it
					if (tag.getId() == null)
					{
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
					for (Images image : images)
					{
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
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.getBoolean(ServerProperty.AUTHENTICATION_ENABLED);

		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				SelectConditionStep<?> step = context.selectDistinct(TAGS.ID, TAGS.NAME, TAGS.CREATED_ON, TAGS.UPDATED_ON)
						.from(TAGS
								.leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
								.leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID))
								.leftJoin(ALBUMS).on(ALBUMS.ID.eq(IMAGES.ALBUM_ID)))
						.where(ALBUMS.ID.eq(albumId));

				// Restrict to only albums containing at least one public image
				if (auth && StringUtils.isEmpty(user.getToken()))
					step.and(IMAGES.IS_PUBLIC.eq((byte) 1));

				return step.orderBy(TAGS.NAME)
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
