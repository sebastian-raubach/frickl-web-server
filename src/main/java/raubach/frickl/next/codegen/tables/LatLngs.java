/*
 * This file is generated by jOOQ.
 */
package raubach.frickl.next.codegen.tables;


import java.math.BigDecimal;
import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.JSON;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row13;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import raubach.frickl.next.codegen.Frickl;
import raubach.frickl.next.codegen.enums.LatLngsDataType;
import raubach.frickl.next.codegen.tables.records.LatLngsRecord;


/**
 * VIEW
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class LatLngs extends TableImpl<LatLngsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>frickl.lat_lngs</code>
     */
    public static final LatLngs LAT_LNGS = new LatLngs();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<LatLngsRecord> getRecordType() {
        return LatLngsRecord.class;
    }

    /**
     * The column <code>frickl.lat_lngs.id</code>. Auto incremented id of this
     * table.
     */
    public final TableField<LatLngsRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "Auto incremented id of this table.");

    /**
     * The column <code>frickl.lat_lngs.path</code>. The path to the image
     * relative to the base path of the setup.
     */
    public final TableField<LatLngsRecord, String> PATH = createField(DSL.name("path"), SQLDataType.CLOB.nullable(false), this, "The path to the image relative to the base path of the setup.");

    /**
     * The column <code>frickl.lat_lngs.name</code>. The name of the image. This
     * will be the filename.
     */
    public final TableField<LatLngsRecord, String> NAME = createField(DSL.name("name"), SQLDataType.CLOB.nullable(false), this, "The name of the image. This will be the filename.");

    /**
     * The column <code>frickl.lat_lngs.is_favorite</code>. Boolean deciding if
     * this image is one of the favorites.
     */
    public final TableField<LatLngsRecord, Byte> IS_FAVORITE = createField(DSL.name("is_favorite"), SQLDataType.TINYINT.nullable(false).defaultValue(DSL.inline("0", SQLDataType.TINYINT)), this, "Boolean deciding if this image is one of the favorites.");

    /**
     * The column <code>frickl.lat_lngs.exif</code>. Optional Exif information
     * in JSON format.
     */
    public final TableField<LatLngsRecord, JSON> EXIF = createField(DSL.name("exif"), SQLDataType.JSON, this, "Optional Exif information in JSON format.");

    /**
     * The column <code>frickl.lat_lngs.album_id</code>. The album this image
     * belongs to. This will be the containing folder.
     */
    public final TableField<LatLngsRecord, Integer> ALBUM_ID = createField(DSL.name("album_id"), SQLDataType.INTEGER.nullable(false), this, "The album this image belongs to. This will be the containing folder.");

    /**
     * The column <code>frickl.lat_lngs.is_public</code>.
     */
    public final TableField<LatLngsRecord, Byte> IS_PUBLIC = createField(DSL.name("is_public"), SQLDataType.TINYINT.defaultValue(DSL.inline("0", SQLDataType.TINYINT)), this, "");

    /**
     * The column <code>frickl.lat_lngs.view_count</code>.
     */
    public final TableField<LatLngsRecord, Integer> VIEW_COUNT = createField(DSL.name("view_count"), SQLDataType.INTEGER.nullable(false).defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>frickl.lat_lngs.data_type</code>.
     */
    public final TableField<LatLngsRecord, LatLngsDataType> DATA_TYPE = createField(DSL.name("data_type"), SQLDataType.VARCHAR(5).nullable(false).defaultValue(DSL.inline("image", SQLDataType.VARCHAR)).asEnumDataType(raubach.frickl.next.codegen.enums.LatLngsDataType.class), this, "");

    /**
     * The column <code>frickl.lat_lngs.created_on</code>. When this record has
     * been created.
     */
    public final TableField<LatLngsRecord, Timestamp> CREATED_ON = createField(DSL.name("created_on"), SQLDataType.TIMESTAMP(0), this, "When this record has been created.");

    /**
     * The column <code>frickl.lat_lngs.updated_on</code>. When this record has
     * last been updated.
     */
    public final TableField<LatLngsRecord, Timestamp> UPDATED_ON = createField(DSL.name("updated_on"), SQLDataType.TIMESTAMP(0), this, "When this record has last been updated.");

    /**
     * The column <code>frickl.lat_lngs.latitude</code>.
     */
    public final TableField<LatLngsRecord, BigDecimal> LATITUDE = createField(DSL.name("latitude"), SQLDataType.DECIMAL(64, 10), this, "");

    /**
     * The column <code>frickl.lat_lngs.longitude</code>.
     */
    public final TableField<LatLngsRecord, BigDecimal> LONGITUDE = createField(DSL.name("longitude"), SQLDataType.DECIMAL(64, 10), this, "");

    private LatLngs(Name alias, Table<LatLngsRecord> aliased) {
        this(alias, aliased, null);
    }

    private LatLngs(Name alias, Table<LatLngsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("VIEW"), TableOptions.view("create view `lat_lngs` as select `frickl`.`images`.`id` AS `id`,`frickl`.`images`.`path` AS `path`,`frickl`.`images`.`name` AS `name`,`frickl`.`images`.`is_favorite` AS `is_favorite`,`frickl`.`images`.`exif` AS `exif`,`frickl`.`images`.`album_id` AS `album_id`,`frickl`.`images`.`is_public` AS `is_public`,`frickl`.`images`.`view_count` AS `view_count`,`frickl`.`images`.`data_type` AS `data_type`,`frickl`.`images`.`created_on` AS `created_on`,`frickl`.`images`.`updated_on` AS `updated_on`,cast(json_unquote(json_extract(`frickl`.`images`.`exif`,'$.gpsLatitude')) as decimal(64,10)) AS `latitude`,cast(json_unquote(json_extract(`frickl`.`images`.`exif`,'$.gpsLongitude')) as decimal(64,10)) AS `longitude` from `frickl`.`images` where ((json_unquote(json_extract(`frickl`.`images`.`exif`,'$.gpsLatitude')) is not null) and (json_unquote(json_extract(`frickl`.`images`.`exif`,'$.gpsLongitude')) is not null))"));
    }

    /**
     * Create an aliased <code>frickl.lat_lngs</code> table reference
     */
    public LatLngs(String alias) {
        this(DSL.name(alias), LAT_LNGS);
    }

    /**
     * Create an aliased <code>frickl.lat_lngs</code> table reference
     */
    public LatLngs(Name alias) {
        this(alias, LAT_LNGS);
    }

    /**
     * Create a <code>frickl.lat_lngs</code> table reference
     */
    public LatLngs() {
        this(DSL.name("lat_lngs"), null);
    }

    public <O extends Record> LatLngs(Table<O> child, ForeignKey<O, LatLngsRecord> key) {
        super(child, key, LAT_LNGS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Frickl.FRICKL;
    }

    @Override
    public LatLngs as(String alias) {
        return new LatLngs(DSL.name(alias), this);
    }

    @Override
    public LatLngs as(Name alias) {
        return new LatLngs(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public LatLngs rename(String name) {
        return new LatLngs(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public LatLngs rename(Name name) {
        return new LatLngs(name, null);
    }

    // -------------------------------------------------------------------------
    // Row13 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row13<Integer, String, String, Byte, JSON, Integer, Byte, Integer, LatLngsDataType, Timestamp, Timestamp, BigDecimal, BigDecimal> fieldsRow() {
        return (Row13) super.fieldsRow();
    }
}