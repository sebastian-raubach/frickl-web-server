package raubach.fricklweb.server.resource.image;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.conf.ParamType;
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
import raubach.fricklweb.server.database.tables.records.TagsRecord;
import raubach.fricklweb.server.resource.AccessTokenResource;
import raubach.fricklweb.server.resource.PaginatedServerResource;
import raubach.fricklweb.server.util.TagUtils;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static raubach.fricklweb.server.database.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.fricklweb.server.database.tables.AlbumTokens.ALBUM_TOKENS;
import static raubach.fricklweb.server.database.tables.ImageTags.IMAGE_TAGS;
import static raubach.fricklweb.server.database.tables.Images.IMAGES;
import static raubach.fricklweb.server.database.tables.Tags.TAGS;

/**
 * @author Sebastian Raubach
 */
public class ImageTagResource extends AccessTokenResource
{
	private Integer imageId = null;

	@Override
	protected void doInit()
			throws ResourceException
	{
		super.doInit();

		try
		{
			imageId = Integer.parseInt(getRequestAttributes().get("imageId").toString());
		}
		catch (Exception e)
		{
		}
	}

	@Post("json")
	public boolean addTagJson(Tags[] tags)
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(user.getToken()))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

		boolean result = false;
		if (tags != null && tags.length > 0 && imageId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				Images image = context.selectFrom(IMAGES)
						.where(IMAGES.ID.eq(imageId))
						.fetchAnyInto(Images.class);

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

					if (!existingIds.contains(image.getId()))
					{

						result = context.insertInto(IMAGE_TAGS, IMAGE_TAGS.IMAGE_ID, IMAGE_TAGS.TAG_ID)
								.values(image.getId(), tag.getId())
								.execute() == 1;
					}
				}

				// Run this in a separate thread, we don't need to wait for it to finish
				new Thread(() -> {
					File file = new File(Frickl.BASE_PATH, image.getPath());
					try
					{
						TagUtils.addTagToImage(file, tagStrings);
					}
					catch (IOException e)
					{
						e.printStackTrace();
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

		return result;
	}

	@Delete("json")
	public boolean removeTagJson(Tags tag)
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(user.getToken()))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

		if (imageId != null && tag != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				Images image = context.selectFrom(IMAGES)
						.where(IMAGES.ID.eq(imageId))
						.fetchAnyInto(Images.class);

				Tags t = context.selectFrom(TAGS)
						.where(TAGS.ID.eq(tag.getId()))
						.fetchAnyInto(Tags.class);

				if (t == null || image == null)
					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

				int numberOfDeletedItems = context.deleteFrom(IMAGE_TAGS)
						.where(IMAGE_TAGS.IMAGE_ID.eq(image.getId()))
						.and(IMAGE_TAGS.TAG_ID.eq(tag.getId()))
						.execute();

				File file = new File(Frickl.BASE_PATH, image.getPath());
				try
				{
					TagUtils.deleteTagFromImage(file, Collections.singletonList(tag.getName()));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				// TODO: Delete all tags that have no more images associated with them

				return numberOfDeletedItems == 1;
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
		boolean auth = PropertyWatcher.authEnabled();

		if (imageId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = DSL.using(conn, SQLDialect.MYSQL))
			{
				SelectConditionStep<?> step = context.select(TAGS.ID, TAGS.NAME, TAGS.CREATED_ON, TAGS.UPDATED_ON)
						.from(TAGS
								.leftJoin(IMAGE_TAGS).on(TAGS.ID.eq(IMAGE_TAGS.TAG_ID))
								.leftJoin(IMAGES).on(IMAGES.ID.eq(IMAGE_TAGS.IMAGE_ID)))
						.where(IMAGES.ID.eq(imageId));

				if (auth)
				{
					if (!StringUtils.isEmpty(accessToken))
					{
						Logger.getLogger("").log(Level.INFO, "ACCESS TOKEN: " + accessToken);

						step.and(DSL.exists(DSL.selectOne()
								.from(ALBUM_TOKENS)
								.leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
								.where(ACCESS_TOKENS.TOKEN.eq(accessToken)
										.and(ALBUM_TOKENS.ALBUM_ID.eq(IMAGES.ALBUM_ID)))));
					}
					else if (StringUtils.isEmpty(user.getToken()))
					{
						step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				Logger.getLogger("").log(Level.INFO, step.getSQL(ParamType.INLINED));

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
