package raubach.fricklweb.server.util.task;

import org.jooq.*;
import org.jooq.impl.DSL;
import raubach.fricklweb.server.Database;
import raubach.fricklweb.server.database.enums.ImagesDataType;
import raubach.fricklweb.server.database.tables.Albums;
import raubach.fricklweb.server.database.tables.records.AlbumCountsRecord;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import static raubach.fricklweb.server.database.Tables.*;

public class AlbumCountUpdateTask implements Runnable
{
	private static boolean RUNNING = false;

	@Override
	public void run()
	{
		if (RUNNING)
			return;

		RUNNING = true;
		Logger.getLogger("").info("STARTING ALBUM COUNT UPDATER");

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			Set<Integer> albumIds = new HashSet<>();
			Map<Integer, Integer> imageCount = new HashMap<>();
			Map<Integer, Integer> imageCountPublic = new HashMap<>();
			Map<Integer, Integer> imageViewCount = new HashMap<>();
			Map<Integer, Integer> albumCount = new HashMap<>();
			Map<Integer, Long> maxImageDate = new HashMap<>();
			Map<Integer, Long> minImageDate = new HashMap<>();

			Albums a = ALBUMS.as("a");

			Field<Integer> icField = DSL.selectCount().from(IMAGES).where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)).asField();
			Field<Integer> icpField = DSL.selectCount().from(IMAGES).where(IMAGES.ALBUM_ID.eq(ALBUMS.ID).and(IMAGES.IS_PUBLIC.eq((byte) 1))).asField();
			Field<Integer> acField = DSL.selectCount().from(a).where(a.PARENT_ALBUM_ID.eq(ALBUMS.ID)).asField();
			Field<BigDecimal> icvField = DSL.select(DSL.sum(DSL.coalesce(IMAGES.VIEW_COUNT, 0))).from(IMAGES).where(IMAGES.ALBUM_ID.eq(ALBUMS.ID)).asField();
			Field<Timestamp> minField = DSL.select(DSL.min(IMAGES.CREATED_ON)).from(IMAGES).where(IMAGES.ALBUM_ID.eq(ALBUMS.ID).and(IMAGES.DATA_TYPE.eq(ImagesDataType.image))).asField();
			Field<Timestamp> maxField = DSL.select(DSL.max(IMAGES.CREATED_ON)).from(IMAGES).where(IMAGES.ALBUM_ID.eq(ALBUMS.ID).and(IMAGES.DATA_TYPE.eq(ImagesDataType.image))).asField();

			context.select(
					   ALBUMS.ID,
					   ALBUMS.PARENT_ALBUM_ID,
					   icField,
					   acField,
					   icpField,
					   icvField,
					   minField,
					   maxField
				   ).from(ALBUMS)
				   .orderBy(ALBUMS.ID.desc())
				   .forEach(r -> {
					   try
					   {
						   Integer albumId = r.get(ALBUMS.ID);
						   Integer parentAlbumId = r.get(ALBUMS.PARENT_ALBUM_ID);
						   Integer ic = r.get(icField);
						   Integer ac = r.get(acField);
						   Integer icp = r.get(icpField);
						   Integer icv = 0;
						   Long min = 0l;
						   Long max = 0l;

						   try
						   {
							   icv = r.get(icvField).intValue();
						   }
						   catch (Exception e)
						   {
							   // Ignore
						   }
						   try
						   {
							   min = r.get(minField).getTime();
						   }
						   catch (Exception e)
						   {
							   // Ignore
						   }

						   try
						   {
							   max = r.get(maxField).getTime();
						   }
						   catch (Exception e)
						   {
							   // Ignore
						   }

						   if (ic == null)
							   ic = 0;
						   if (ac == null)
							   ac = 0;
						   if (icp == null)
							   icp = 0;
						   if (icv == null)
							   icp = 0;

						   albumIds.add(albumId);

						   if (!imageCount.containsKey(albumId))
							   imageCount.put(albumId, ic);
						   else
							   imageCount.put(albumId, imageCount.get(albumId) + ic);

						   if (!albumCount.containsKey(albumId))
							   albumCount.put(albumId, ac);
						   else
							   albumCount.put(albumId, albumCount.get(albumId) + ac);

						   if (!imageCountPublic.containsKey(albumId))
							   imageCountPublic.put(albumId, icp);
						   else
							   imageCountPublic.put(albumId, imageCountPublic.get(albumId) + icp);

						   if (!imageViewCount.containsKey(albumId))
							   imageViewCount.put(albumId, icv);
						   else
							   imageViewCount.put(albumId, imageViewCount.get(albumId) + icv);

						   if (!minImageDate.containsKey(albumId))
							   minImageDate.put(albumId, min);
						   else
							   minImageDate.put(albumId, Math.min(minImageDate.get(albumId), min));

						   if (!maxImageDate.containsKey(albumId))
							   maxImageDate.put(albumId, max);
						   else
							   maxImageDate.put(albumId, Math.max(maxImageDate.get(albumId), max));

						   if (parentAlbumId != null)
						   {
							   albumIds.add(parentAlbumId);

							   if (!imageCount.containsKey(parentAlbumId))
								   imageCount.put(parentAlbumId, imageCount.get(albumId));
							   else
								   imageCount.put(parentAlbumId, imageCount.get(parentAlbumId) + imageCount.get(albumId));

							   if (!imageCountPublic.containsKey(parentAlbumId))
								   imageCountPublic.put(parentAlbumId, imageCountPublic.get(albumId));
							   else
								   imageCountPublic.put(parentAlbumId, imageCountPublic.get(parentAlbumId) + imageCountPublic.get(albumId));

							   if (!imageViewCount.containsKey(parentAlbumId))
								   imageViewCount.put(parentAlbumId, imageViewCount.get(albumId));
							   else
								   imageViewCount.put(parentAlbumId, imageViewCount.get(parentAlbumId) + imageViewCount.get(albumId));

							   if (!minImageDate.containsKey(parentAlbumId))
								   minImageDate.put(parentAlbumId, minImageDate.get(albumId));
							   else
								   minImageDate.put(parentAlbumId, Math.min(minImageDate.get(parentAlbumId), minImageDate.get(albumId)));

							   if (!maxImageDate.containsKey(parentAlbumId))
								   maxImageDate.put(parentAlbumId, maxImageDate.get(albumId));
							   else
								   maxImageDate.put(parentAlbumId, Math.max(maxImageDate.get(parentAlbumId), maxImageDate.get(albumId)));
						   }
					   }
					   catch (Exception e)
					   {
						   Logger.getLogger("").severe(e.getMessage());
					   }
				   });

			context.deleteFrom(ALBUM_COUNTS).execute();

			List<AlbumCountsRecord> cache = new ArrayList<>();

			albumIds.forEach(id -> {
				try
				{
					AlbumCountsRecord rec = context.newRecord(ALBUM_COUNTS);
					rec.setAlbumId(id);
					rec.setImageCount(imageCount.get(id));
					rec.setImageCountPublic(imageCountPublic.get(id));
					rec.setImageViewCount(imageViewCount.get(id));
					rec.setAlbumCount(albumCount.get(id));
					rec.setNewestImage(maxImageDate.get(id) == null ? null : new Timestamp(maxImageDate.get(id)));
					rec.setOldestImage(minImageDate.get(id) == null ? null : new Timestamp(minImageDate.get(id)));

					cache.add(rec);

					if (cache.size() > 1000)
					{
						context.batchStore(cache)
							   .execute();
						cache.clear();
					}
				}
				catch (Exception e)
				{
					Logger.getLogger("").severe(e.getMessage());
					e.printStackTrace();
				}
			});

			if (cache.size() > 0)
			{
				context.batchStore(cache)
					   .execute();
			}
		}
		catch (SQLException e)
		{
			Logger.getLogger("").severe(e.getMessage());
			e.printStackTrace();
		}

		RUNNING = false;
	}
}
