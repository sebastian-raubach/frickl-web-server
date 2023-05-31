/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables;


import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row16;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import raubach.fricklweb.server.database.Frickl;
import raubach.fricklweb.server.database.tables.records.AlbumStatsRecord;


/**
 * VIEW
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AlbumStats extends TableImpl<AlbumStatsRecord> {

    private static final long serialVersionUID = 1L;

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
     * The column <code>frickl.album_stats.id</code>. Auto incremented id of
     * this table.
     */
    public final TableField<AlbumStatsRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "Auto incremented id of this table.");

    /**
     * The column <code>frickl.album_stats.name</code>. The name of the album.
     * Should ideally be relatively short.
     */
    public final TableField<AlbumStatsRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(255).nullable(false), this, "The name of the album. Should ideally be relatively short.");

    /**
     * The column <code>frickl.album_stats.description</code>. Optional
     * description of the album.
     */
    public final TableField<AlbumStatsRecord, String> DESCRIPTION = createField(DSL.name("description"), SQLDataType.CLOB, this, "Optional description of the album.");

    /**
     * The column <code>frickl.album_stats.path</code>. The path to the album
     * relative to the base path of the setup.
     */
    public final TableField<AlbumStatsRecord, String> PATH = createField(DSL.name("path"), SQLDataType.CLOB.nullable(false), this, "The path to the album relative to the base path of the setup.");

    /**
     * The column <code>frickl.album_stats.banner_image_id</code>. Optional
     * banner image id. This image will be shown to visually represent this
     * album.
     */
    public final TableField<AlbumStatsRecord, Integer> BANNER_IMAGE_ID = createField(DSL.name("banner_image_id"), SQLDataType.INTEGER, this, "Optional banner image id. This image will be shown to visually represent this album.");

    /**
     * The column <code>frickl.album_stats.banner_image_public_id</code>.
     */
    public final TableField<AlbumStatsRecord, Long> BANNER_IMAGE_PUBLIC_ID = createField(DSL.name("banner_image_public_id"), SQLDataType.BIGINT, this, "");

    /**
     * The column <code>frickl.album_stats.parent_album_id</code>. Optional
     * parent album id. If this album is a sub-album of another album, this
     * parent album can be defined here.
     */
    public final TableField<AlbumStatsRecord, Integer> PARENT_ALBUM_ID = createField(DSL.name("parent_album_id"), SQLDataType.INTEGER, this, "Optional parent album id. If this album is a sub-album of another album, this parent album can be defined here.");

    /**
     * The column <code>frickl.album_stats.created_on</code>. When this record
     * has been created.
     */
    public final TableField<AlbumStatsRecord, Timestamp> CREATED_ON = createField(DSL.name("created_on"), SQLDataType.TIMESTAMP(0), this, "When this record has been created.");

    /**
     * The column <code>frickl.album_stats.updated_on</code>. When this record
     * has last been updated.
     */
    public final TableField<AlbumStatsRecord, Timestamp> UPDATED_ON = createField(DSL.name("updated_on"), SQLDataType.TIMESTAMP(0), this, "When this record has last been updated.");

    /**
     * The column <code>frickl.album_stats.album_id</code>.
     */
    public final TableField<AlbumStatsRecord, Integer> ALBUM_ID = createField(DSL.name("album_id"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>frickl.album_stats.image_count</code>.
     */
    public final TableField<AlbumStatsRecord, Integer> IMAGE_COUNT = createField(DSL.name("image_count"), SQLDataType.INTEGER.defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>frickl.album_stats.image_count_public</code>.
     */
    public final TableField<AlbumStatsRecord, Integer> IMAGE_COUNT_PUBLIC = createField(DSL.name("image_count_public"), SQLDataType.INTEGER.defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>frickl.album_stats.album_count</code>.
     */
    public final TableField<AlbumStatsRecord, Integer> ALBUM_COUNT = createField(DSL.name("album_count"), SQLDataType.INTEGER.defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>frickl.album_stats.image_view_count</code>.
     */
    public final TableField<AlbumStatsRecord, Integer> IMAGE_VIEW_COUNT = createField(DSL.name("image_view_count"), SQLDataType.INTEGER.defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>frickl.album_stats.newest_image</code>.
     */
    public final TableField<AlbumStatsRecord, Timestamp> NEWEST_IMAGE = createField(DSL.name("newest_image"), SQLDataType.TIMESTAMP(0), this, "");

    /**
     * The column <code>frickl.album_stats.oldest_image</code>.
     */
    public final TableField<AlbumStatsRecord, Timestamp> OLDEST_IMAGE = createField(DSL.name("oldest_image"), SQLDataType.TIMESTAMP(0), this, "");

    private AlbumStats(Name alias, Table<AlbumStatsRecord> aliased) {
        this(alias, aliased, null);
    }

    private AlbumStats(Name alias, Table<AlbumStatsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("VIEW"), TableOptions.view("create view `album_stats` as select `frickl`.`albums`.`id` AS `id`,`frickl`.`albums`.`name` AS `name`,`frickl`.`albums`.`description` AS `description`,`frickl`.`albums`.`path` AS `path`,`frickl`.`albums`.`banner_image_id` AS `banner_image_id`,(select `frickl`.`images`.`id` from `frickl`.`images` where ((`frickl`.`images`.`album_id` = `frickl`.`albums`.`id`) and (`frickl`.`images`.`is_public` = 1)) limit 1) AS `banner_image_public_id`,`frickl`.`albums`.`parent_album_id` AS `parent_album_id`,`frickl`.`albums`.`created_on` AS `created_on`,`frickl`.`albums`.`updated_on` AS `updated_on`,`frickl`.`album_counts`.`album_id` AS `album_id`,`frickl`.`album_counts`.`image_count` AS `image_count`,`frickl`.`album_counts`.`image_count_public` AS `image_count_public`,`frickl`.`album_counts`.`album_count` AS `album_count`,`frickl`.`album_counts`.`image_view_count` AS `image_view_count`,`frickl`.`album_counts`.`newest_image` AS `newest_image`,`frickl`.`album_counts`.`oldest_image` AS `oldest_image` from (`frickl`.`albums` left join `frickl`.`album_counts` on((`frickl`.`albums`.`id` = `frickl`.`album_counts`.`album_id`)))"));
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

    /**
     * Create a <code>frickl.album_stats</code> table reference
     */
    public AlbumStats() {
        this(DSL.name("album_stats"), null);
    }

    public <O extends Record> AlbumStats(Table<O> child, ForeignKey<O, AlbumStatsRecord> key) {
        super(child, key, ALBUM_STATS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Frickl.FRICKL;
    }

    @Override
    public AlbumStats as(String alias) {
        return new AlbumStats(DSL.name(alias), this);
    }

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

    // -------------------------------------------------------------------------
    // Row16 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row16<Integer, String, String, String, Integer, Long, Integer, Timestamp, Timestamp, Integer, Integer, Integer, Integer, Integer, Timestamp, Timestamp> fieldsRow() {
        return (Row16) super.fieldsRow();
    }
}
