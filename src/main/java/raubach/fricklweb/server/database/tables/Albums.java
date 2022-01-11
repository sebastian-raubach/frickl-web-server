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

import raubach.fricklweb.server.database.Frickl;
import raubach.fricklweb.server.database.Indexes;
import raubach.fricklweb.server.database.Keys;
import raubach.fricklweb.server.database.tables.records.AlbumsRecord;


/**
 * This table contains all albums in Frickl. Albums correspond to image folders 
 * on the file system.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Albums extends TableImpl<AlbumsRecord> {

    private static final long serialVersionUID = 81644035;

    /**
     * The reference instance of <code>frickl.albums</code>
     */
    public static final Albums ALBUMS = new Albums();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AlbumsRecord> getRecordType() {
        return AlbumsRecord.class;
    }

    /**
     * The column <code>frickl.albums.id</code>. Auto incremented id of this table.
     */
    public final TableField<AlbumsRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).identity(true), this, "Auto incremented id of this table.");

    /**
     * The column <code>frickl.albums.name</code>. The name of the album. Should ideally be relatively short.
     */
    public final TableField<AlbumsRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "The name of the album. Should ideally be relatively short.");

    /**
     * The column <code>frickl.albums.description</code>. Optional description of the album.
     */
    public final TableField<AlbumsRecord, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.CLOB, this, "Optional description of the album.");

    /**
     * The column <code>frickl.albums.path</code>. The path to the album relative to the base path of the setup.
     */
    public final TableField<AlbumsRecord, String> PATH = createField("path", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "The path to the album relative to the base path of the setup.");

    /**
     * The column <code>frickl.albums.banner_image_id</code>. Optional banner image id. This image will be shown to visually represent this album.
     */
    public final TableField<AlbumsRecord, Integer> BANNER_IMAGE_ID = createField("banner_image_id", org.jooq.impl.SQLDataType.INTEGER, this, "Optional banner image id. This image will be shown to visually represent this album.");

    /**
     * The column <code>frickl.albums.parent_album_id</code>. Optional parent album id. If this album is a sub-album of another album, this parent album can be defined here.
     */
    public final TableField<AlbumsRecord, Integer> PARENT_ALBUM_ID = createField("parent_album_id", org.jooq.impl.SQLDataType.INTEGER, this, "Optional parent album id. If this album is a sub-album of another album, this parent album can be defined here.");

    /**
     * The column <code>frickl.albums.created_on</code>. When this record has been created.
     */
    public final TableField<AlbumsRecord, Timestamp> CREATED_ON = createField("created_on", org.jooq.impl.SQLDataType.TIMESTAMP, this, "When this record has been created.");

    /**
     * The column <code>frickl.albums.updated_on</code>. When this record has last been updated.
     */
    public final TableField<AlbumsRecord, Timestamp> UPDATED_ON = createField("updated_on", org.jooq.impl.SQLDataType.TIMESTAMP.defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "When this record has last been updated.");

    /**
     * Create a <code>frickl.albums</code> table reference
     */
    public Albums() {
        this(DSL.name("albums"), null);
    }

    /**
     * Create an aliased <code>frickl.albums</code> table reference
     */
    public Albums(String alias) {
        this(DSL.name(alias), ALBUMS);
    }

    /**
     * Create an aliased <code>frickl.albums</code> table reference
     */
    public Albums(Name alias) {
        this(alias, ALBUMS);
    }

    private Albums(Name alias, Table<AlbumsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Albums(Name alias, Table<AlbumsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("This table contains all albums in Frickl. Albums correspond to image folders on the file system."));
    }

    public <O extends Record> Albums(Table<O> child, ForeignKey<O, AlbumsRecord> key) {
        super(child, key, ALBUMS);
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
        return Arrays.<Index>asList(Indexes.ALBUMS_BANNER_IMAGE_ID, Indexes.ALBUMS_PARENT_ALBUM_ID, Indexes.ALBUMS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<AlbumsRecord, Integer> getIdentity() {
        return Keys.IDENTITY_ALBUMS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<AlbumsRecord> getPrimaryKey() {
        return Keys.KEY_ALBUMS_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<AlbumsRecord>> getKeys() {
        return Arrays.<UniqueKey<AlbumsRecord>>asList(Keys.KEY_ALBUMS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<AlbumsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<AlbumsRecord, ?>>asList(Keys.ALBUMS_IBFK_1, Keys.ALBUMS_IBFK_2);
    }

    public Images images() {
        return new Images(this, Keys.ALBUMS_IBFK_1);
    }

    public raubach.fricklweb.server.database.tables.Albums albums() {
        return new raubach.fricklweb.server.database.tables.Albums(this, Keys.ALBUMS_IBFK_2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Albums as(String alias) {
        return new Albums(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Albums as(Name alias) {
        return new Albums(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Albums rename(String name) {
        return new Albums(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Albums rename(Name name) {
        return new Albums(name, null);
    }
}