package raubach.fricklweb.server.resource.image;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.*;
import raubach.fricklweb.server.*;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.database.enums.ImagesDataType;
import raubach.fricklweb.server.database.tables.pojos.Images;
import raubach.fricklweb.server.database.tables.records.*;
import raubach.fricklweb.server.resource.AbstractAccessTokenResource;
import raubach.fricklweb.server.util.FileUploadHandler;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static raubach.fricklweb.server.database.tables.AccessTokens.*;
import static raubach.fricklweb.server.database.tables.AlbumTokens.*;
import static raubach.fricklweb.server.database.tables.Albums.*;
import static raubach.fricklweb.server.database.tables.Images.*;

/**
 * @author Sebastian Raubach
 */
public class ImageResource extends AbstractAccessTokenResource
{
	public static final String PARAM_DATE = "date";
	public static final String PARAM_FAV  = "fav";

	private SimpleDateFormat sdf     = new SimpleDateFormat("yyyy-MM-dd");
	private Integer          albumId = null;
	private Integer          imageId = null;

	private String  date;
	private Boolean isFav;

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
		try
		{
			imageId = Integer.parseInt(getRequestAttributes().get("imageId").toString());
		}
		catch (Exception e)
		{
		}
		try
		{
			date = getQueryValue(PARAM_DATE);
		}
		catch (Exception e)
		{
		}
		try
		{
			isFav = Boolean.parseBoolean(getQueryValue(PARAM_FAV));
		}
		catch (Exception e)
		{
		}
	}

	private synchronized Date getDate(String date)
	{
		try
		{
			return new Date(sdf.parse(date).getTime());
		}
		catch (Exception e)
		{
			return null;
		}
	}

	@Patch("json")
	public void patchJson(Images image)
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (auth && StringUtils.isEmpty(user.getToken()))
			throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);

		if (imageId != null && image != null && Objects.equals(image.getId(), imageId))
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				context.update(IMAGES)
					   .set(IMAGES.IS_FAVORITE, image.getIsFavorite())
					   .set(IMAGES.IS_PUBLIC, image.getIsPublic())
					   .where(IMAGES.ID.eq(imageId))
					   .execute();
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

	@Post
	public boolean postImages(Representation entity)
	{
		if (albumId == null)
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			AlbumsRecord album = context.selectFrom(ALBUMS).where(ALBUMS.ID.eq(albumId)).fetchAny();

			if (album == null)
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

			File basePath = new File(Frickl.BASE_PATH);
			File folder = new File(basePath, album.getPath());
			List<String> finalFilenames = FileUploadHandler.handleMultiple(entity, "imageFiles", folder);

			boolean needsBannerImage = album.getBannerImageId() == null;

			int counter = 0;
			for (String file : finalFilenames)
			{
				Timestamp ts = new Timestamp(System.currentTimeMillis());
				File path = new File(folder, file);
				try
				{
					BasicFileAttributes attr = Files.readAttributes(path.toPath(), BasicFileAttributes.class);
					ts = new Timestamp(attr.creationTime().toMillis());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				String relativePath = basePath.toURI().relativize(path.toURI()).getPath();

				ImagesRecord image = context.newRecord(IMAGES);
				image.setAlbumId(albumId);
				image.setName(file);
				image.setPath(relativePath);
				image.setDataType(ImagesDataType.image);
				image.setCreatedOn(ts);
				counter += image.store() > 0 ? 1 : 0;

				if (needsBannerImage) {
					album.setBannerImageId(image.getId());
					album.store(ALBUMS.BANNER_IMAGE_ID);
					needsBannerImage = false;
				}
			}
			return counter > 0;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Get("json")
	public List<Images> getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		if (albumId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectConditionStep<Record> step = context.select().from(IMAGES)
														  .where(IMAGES.ALBUM_ID.eq(albumId));

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
					else if (StringUtils.isEmpty(user.getToken()))
					{
						step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				return step.orderBy(IMAGES.CREATED_ON.desc(), IMAGES.ID.desc())
						   .offset(pageSize * currentPage)
						   .limit(pageSize)
						   .fetch()
						   .into(Images.class);
			}
			catch (SQLException e)
			{
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else if (imageId != null)
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectConditionStep<Record> step = context.select().from(IMAGES)
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
					else if (StringUtils.isEmpty(user.getToken()))
					{
						step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				return step.fetch()
						   .into(Images.class);
			}
			catch (SQLException e)
			{
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
		else
		{
			try (Connection conn = Database.getConnection();
				 DSLContext context = Database.getContext(conn))
			{
				SelectJoinStep<Record> step = context.select().from(IMAGES);

				if (isFav != null && isFav)
					step.where(IMAGES.IS_FAVORITE.eq((byte) 1));
				if (date != null)
					step.where(DSL.date(IMAGES.CREATED_ON).eq(getDate(date)));

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
					else if (StringUtils.isEmpty(user.getToken()))
					{
						step.where(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				return step.orderBy(IMAGES.CREATED_ON.desc(), IMAGES.ID.desc())
						   .offset(pageSize * currentPage)
						   .limit(pageSize)
						   .fetch()
						   .into(Images.class);
			}
			catch (SQLException e)
			{
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
			}
		}
	}
}
