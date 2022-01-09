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
public class ImageXAgoResource extends AbstractAccessTokenResource
{
	public static final String PARAM_YEAR  = "year";

	private Integer year;

	@Override
	protected void doInit()
		throws ResourceException
	{
		super.doInit();

		try
		{
			year = Integer.parseInt(getQueryValue(PARAM_YEAR));
		}
		catch (Exception e)
		{
		}
	}

	@Get("json")
	public List<Images> getJson()
	{
		CustomVerifier.UserDetails user = CustomVerifier.getFromSession(getRequest(), getResponse());
		boolean auth = PropertyWatcher.authEnabled();

		currentPage = 0;
		pageSize = Integer.MAX_VALUE;

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			SelectJoinStep<Record> step = context.select().from(IMAGES);

			Field<Integer> woyI = DSL.field("weekofyear(?)", Integer.class, IMAGES.CREATED_ON);
			Field<Integer> woyN = DSL.field("weekofyear(?)", Integer.class, DSL.now());

			step.where(woyI.eq(woyN)).and(DSL.year(IMAGES.CREATED_ON).eq(DSL.year(DSL.now()).minus(year)));

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
					   .fetch()
					   .into(Images.class);
		}
		catch (SQLException e)
		{
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
