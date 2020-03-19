/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

import raubach.fricklweb.server.database.tables.AlbumStats;
import raubach.fricklweb.server.database.tables.Albums;
import raubach.fricklweb.server.database.tables.CalendarData;
import raubach.fricklweb.server.database.tables.ImageTags;
import raubach.fricklweb.server.database.tables.ImageTimeline;
import raubach.fricklweb.server.database.tables.Images;
import raubach.fricklweb.server.database.tables.LatLngs;
import raubach.fricklweb.server.database.tables.SchemaVersion;
import raubach.fricklweb.server.database.tables.StatsCamera;
import raubach.fricklweb.server.database.tables.Tags;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Frickl extends SchemaImpl {

    /**
     * The reference instance of <code>frickl</code>
     */
    public static final Frickl FRICKL = new Frickl();
    private static final long serialVersionUID = 1143223609;
    /**
     * VIEW
     */
    public final AlbumStats ALBUM_STATS = raubach.fricklweb.server.database.tables.AlbumStats.ALBUM_STATS;

    /**
     * This table contains all albums in Frickl. Albums correspond to image folders on the file system.
     */
    public final Albums ALBUMS = raubach.fricklweb.server.database.tables.Albums.ALBUMS;

    /**
     * VIEW
     */
    public final CalendarData CALENDAR_DATA = raubach.fricklweb.server.database.tables.CalendarData.CALENDAR_DATA;

    /**
     * This table joins `images` and `tags` and therefore defines which tags an image is tagged with.
     */
    public final ImageTags IMAGE_TAGS = raubach.fricklweb.server.database.tables.ImageTags.IMAGE_TAGS;

    /**
     * VIEW
     */
    public final ImageTimeline IMAGE_TIMELINE = raubach.fricklweb.server.database.tables.ImageTimeline.IMAGE_TIMELINE;

    /**
     * This table contains images from the file system.
     */
    public final Images IMAGES = raubach.fricklweb.server.database.tables.Images.IMAGES;

    /**
     * VIEW
     */
    public final LatLngs LAT_LNGS = raubach.fricklweb.server.database.tables.LatLngs.LAT_LNGS;

    /**
     * The table <code>frickl.schema_version</code>.
     */
    public final SchemaVersion SCHEMA_VERSION = raubach.fricklweb.server.database.tables.SchemaVersion.SCHEMA_VERSION;

    /**
     * VIEW
     */
    public final StatsCamera STATS_CAMERA = raubach.fricklweb.server.database.tables.StatsCamera.STATS_CAMERA;

    /**
     * This table contains all tags/keywords that have been defined and assigned to images.
     */
    public final Tags TAGS = raubach.fricklweb.server.database.tables.Tags.TAGS;

    /**
     * No further instances allowed
     */
    private Frickl() {
        super("frickl", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            AlbumStats.ALBUM_STATS,
            Albums.ALBUMS,
            CalendarData.CALENDAR_DATA,
            ImageTags.IMAGE_TAGS,
            ImageTimeline.IMAGE_TIMELINE,
            Images.IMAGES,
            LatLngs.LAT_LNGS,
            SchemaVersion.SCHEMA_VERSION,
            StatsCamera.STATS_CAMERA,
            Tags.TAGS);
    }
}
