/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables;


import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import raubach.fricklweb.server.database.Frickl;
import raubach.fricklweb.server.database.tables.records.AlbumStatsRecord;


/**
 * VIEW
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AlbumStats extends TableImpl<AlbumStatsRecord> {

    private static final long serialVersionUID = -1633900757;

    /**
     * The reference instance of <code>frickl.album_stats</code>
     */
    public static final AlbumStats ALBUM_STATS = new AlbumStats();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AlbumStatsRecord> getRecordType() {
        return AlbumStatsRecord.class;
    }

    /**
     * The column <code>frickl.album_stats.id</code>. Auto incremented id of this table.
     */
    public final TableField<AlbumStatsRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.INTEGER)), this, "Auto incremented id of this table.");

    /**
     * The column <code>frickl.album_stats.name</code>. The name of the album. Should ideally be relatively short.
     */
    public final TableField<AlbumStatsRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "The name of the album. Should ideally be relatively short.");

    /**
     * The column <code>frickl.album_stats.description</code>. Optional description of the album.
     */
    public final TableField<AlbumStatsRecord, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.CLOB, this, "Optional description of the album.");

    /**
     * The column <code>frickl.album_stats.path</code>. The path to the album relative to the base path of the setup.
     */
    public final TableField<AlbumStatsRecord, String> PATH = createField("path", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "The path to the album relative to the base path of the setup.");

    /**
     * The column <code>frickl.album_stats.banner_image_id</code>. Optional banner image id. This image will be shown to visually represent this album.
     */
    public final TableField<AlbumStatsRecord, Integer> BANNER_IMAGE_ID = createField("banner_image_id", org.jooq.impl.SQLDataType.INTEGER, this, "Optional banner image id. This image will be shown to visually represent this album.");

    /**
     * The column <code>frickl.album_stats.banner_image_public_id</code>.
     */
    public final TableField<AlbumStatsRecord, Long> BANNER_IMAGE_PUBLIC_ID = createField("banner_image_public_id", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>frickl.album_stats.parent_album_id</code>. Optional parent album id. If this album is a sub-album of another album, this parent album can be defined here.
     */
    public final TableField<AlbumStatsRecord, Integer> PARENT_ALBUM_ID = createField("parent_album_id", org.jooq.impl.SQLDataType.INTEGER, this, "Optional parent album id. If this album is a sub-album of another album, this parent album can be defined here.");

    /**
     * The column <code>frickl.album_stats.created_on</code>. When this record has been created.
     */
    public final TableField<AlbumStatsRecord, Timestamp> CREATED_ON = createField("created_on", org.jooq.impl.SQLDataType.TIMESTAMP, this, "When this record has been created.");

    /**
     * The column <code>frickl.album_stats.updated_on</code>. When this record has last been updated.
     */
    public final TableField<AlbumStatsRecord, Timestamp> UPDATED_ON = createField("updated_on", org.jooq.impl.SQLDataType.TIMESTAMP.defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "When this record has last been updated.");

    /**
     * The column <code>frickl.album_stats.count</code>.
     */
    public final TableField<AlbumStatsRecord, Long> COUNT = createField("count", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>frickl.album_stats.count_public</code>.
     */
    public final TableField<AlbumStatsRecord, Long> COUNT_PUBLIC = createField("count_public", org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * Create a <code>frickl.album_stats</code> table reference
     */
    public AlbumStats() {
        this(DSL.name("album_stats"), null);
    }

    /**
     * Create an aliased <code>frickl.album_stats</code> table reference
     */
    public AlbumStats(String alias) {
        this(DSL.name(alias), ALBUM_STATS);
    }

    /**
     * Create an aliased <code>frickl.album_stats</code> table reference
     */
    public AlbumStats(Name alias) {
        this(alias, ALBUM_STATS);
    }

    private AlbumStats(Name alias, Table<AlbumStatsRecord> aliased) {
        this(alias, aliased, null);
    }

    private AlbumStats(Name alias, Table<AlbumStatsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("VIEW"));
    }

    public <O extends Record> AlbumStats(Table<O> child, ForeignKey<O, AlbumStatsRecord> key) {
        super(child, key, ALBUM_STATS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Frickl.FRICKL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumStats as(String alias) {
        return new AlbumStats(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumStats as(Name alias) {
        return new AlbumStats(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public AlbumStats rename(String name) {
        return new AlbumStats(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AlbumStats rename(Name name) {
        return new AlbumStats(name, null);
    }
}
