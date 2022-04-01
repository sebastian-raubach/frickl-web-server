/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables;


import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import raubach.fricklweb.server.binding.JsonExifBinding;
import raubach.fricklweb.server.computed.Exif;
import raubach.fricklweb.server.database.Frickl;
import raubach.fricklweb.server.database.Indexes;
import raubach.fricklweb.server.database.Keys;
import raubach.fricklweb.server.database.enums.ImagesDataType;
import raubach.fricklweb.server.database.tables.records.ImagesRecord;


/**
 * This table contains images from the file system.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Images extends TableImpl<ImagesRecord> {

    private static final long serialVersionUID = 1208410387;

    /**
     * The reference instance of <code>frickl.images</code>
     */
    public static final Images IMAGES = new Images();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ImagesRecord> getRecordType() {
        return ImagesRecord.class;
    }

    /**
     * The column <code>frickl.images.id</code>. Auto incremented id of this table.
     */
    public final TableField<ImagesRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).identity(true), this, "Auto incremented id of this table.");

    /**
     * The column <code>frickl.images.path</code>. The path to the image relative to the base path of the setup.
     */
    public final TableField<ImagesRecord, String> PATH = createField("path", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "The path to the image relative to the base path of the setup.");

    /**
     * The column <code>frickl.images.name</code>. The name of the image. This will be the filename.
     */
    public final TableField<ImagesRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "The name of the image. This will be the filename.");

    /**
     * The column <code>frickl.images.is_favorite</code>. Boolean deciding if this image is one of the favorites.
     */
    public final TableField<ImagesRecord, Byte> IS_FAVORITE = createField("is_favorite", org.jooq.impl.SQLDataType.TINYINT.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.TINYINT)), this, "Boolean deciding if this image is one of the favorites.");

    /**
     * The column <code>frickl.images.exif</code>. Optional Exif information in JSON format.
     */
    public final TableField<ImagesRecord, Exif> EXIF = createField("exif", org.jooq.impl.DefaultDataType.getDefaultDataType("\"frickl\".\"images_exif\""), this, "Optional Exif information in JSON format.", new JsonExifBinding());

    /**
     * The column <code>frickl.images.album_id</code>. The album this image belongs to. This will be the containing folder.
     */
    public final TableField<ImagesRecord, Integer> ALBUM_ID = createField("album_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "The album this image belongs to. This will be the containing folder.");

    /**
     * The column <code>frickl.images.is_public</code>.
     */
    public final TableField<ImagesRecord, Byte> IS_PUBLIC = createField("is_public", org.jooq.impl.SQLDataType.TINYINT.defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.TINYINT)), this, "");

    /**
     * The column <code>frickl.images.view_count</code>.
     */
    public final TableField<ImagesRecord, Integer> VIEW_COUNT = createField("view_count", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>frickl.images.data_type</code>.
     */
    public final TableField<ImagesRecord, ImagesDataType> DATA_TYPE = createField("data_type", org.jooq.impl.SQLDataType.VARCHAR(5).nullable(false).defaultValue(org.jooq.impl.DSL.inline("image", org.jooq.impl.SQLDataType.VARCHAR)).asEnumDataType(raubach.fricklweb.server.database.enums.ImagesDataType.class), this, "");

    /**
     * The column <code>frickl.images.created_on</code>. When this record has been created.
     */
    public final TableField<ImagesRecord, Timestamp> CREATED_ON = createField("created_on", org.jooq.impl.SQLDataType.TIMESTAMP, this, "When this record has been created.");

    /**
     * The column <code>frickl.images.updated_on</code>. When this record has last been updated.
     */
    public final TableField<ImagesRecord, Timestamp> UPDATED_ON = createField("updated_on", org.jooq.impl.SQLDataType.TIMESTAMP.defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "When this record has last been updated.");

    /**
     * Create a <code>frickl.images</code> table reference
     */
    public Images() {
        this(DSL.name("images"), null);
    }

    /**
     * Create an aliased <code>frickl.images</code> table reference
     */
    public Images(String alias) {
        this(DSL.name(alias), IMAGES);
    }

    /**
     * Create an aliased <code>frickl.images</code> table reference
     */
    public Images(Name alias) {
        this(alias, IMAGES);
    }

    private Images(Name alias, Table<ImagesRecord> aliased) {
        this(alias, aliased, null);
    }

    private Images(Name alias, Table<ImagesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("This table contains images from the file system."));
    }

    public <O extends Record> Images(Table<O> child, ForeignKey<O, ImagesRecord> key) {
        super(child, key, IMAGES);
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
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.IMAGES_IMAGES_DATA_TYPE, Indexes.IMAGES_IMAGES_IBFK_1, Indexes.IMAGES_IMAGES_IS_PUBLIC, Indexes.IMAGES_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<ImagesRecord, Integer> getIdentity() {
        return Keys.IDENTITY_IMAGES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ImagesRecord> getPrimaryKey() {
        return Keys.KEY_IMAGES_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ImagesRecord>> getKeys() {
        return Arrays.<UniqueKey<ImagesRecord>>asList(Keys.KEY_IMAGES_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<ImagesRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ImagesRecord, ?>>asList(Keys.IMAGES_IBFK_1);
    }

    public Albums albums() {
        return new Albums(this, Keys.IMAGES_IBFK_1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Images as(String alias) {
        return new Images(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Images as(Name alias) {
        return new Images(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Images rename(String name) {
        return new Images(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Images rename(Name name) {
        return new Images(name, null);
    }
}
