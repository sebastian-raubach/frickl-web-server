/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.records;


import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;

import raubach.fricklweb.server.database.tables.Albums;


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
public class AlbumsRecord extends UpdatableRecordImpl<AlbumsRecord> implements Record8<Integer, String, String, String, Integer, Integer, Timestamp, Timestamp> {

    private static final long serialVersionUID = 659399391;

    /**
     * Setter for <code>frickl.albums.id</code>. Auto incremented id of this table.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>frickl.albums.id</code>. Auto incremented id of this table.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>frickl.albums.name</code>. The name of the album. Should ideally be relatively short.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>frickl.albums.name</code>. The name of the album. Should ideally be relatively short.
     */
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>frickl.albums.description</code>. Optional description of the album.
     */
    public void setDescription(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>frickl.albums.description</code>. Optional description of the album.
     */
    public String getDescription() {
        return (String) get(2);
    }

    /**
     * Setter for <code>frickl.albums.path</code>. The path to the album relative to the base path of the setup.
     */
    public void setPath(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>frickl.albums.path</code>. The path to the album relative to the base path of the setup.
     */
    public String getPath() {
        return (String) get(3);
    }

    /**
     * Setter for <code>frickl.albums.banner_image_id</code>. Optional banner image id. This image will be shown to visually represent this album.
     */
    public void setBannerImageId(Integer value) {
        set(4, value);
    }

    /**
     * Getter for <code>frickl.albums.banner_image_id</code>. Optional banner image id. This image will be shown to visually represent this album.
     */
    public Integer getBannerImageId() {
        return (Integer) get(4);
    }

    /**
     * Setter for <code>frickl.albums.parent_album_id</code>. Optional parent album id. If this album is a sub-album of another album, this parent album can be defined here.
     */
    public void setParentAlbumId(Integer value) {
        set(5, value);
    }

    /**
     * Getter for <code>frickl.albums.parent_album_id</code>. Optional parent album id. If this album is a sub-album of another album, this parent album can be defined here.
     */
    public Integer getParentAlbumId() {
        return (Integer) get(5);
    }

    /**
     * Setter for <code>frickl.albums.created_on</code>. When this record has been created.
     */
    public void setCreatedOn(Timestamp value) {
        set(6, value);
    }

    /**
     * Getter for <code>frickl.albums.created_on</code>. When this record has been created.
     */
    public Timestamp getCreatedOn() {
        return (Timestamp) get(6);
    }

    /**
     * Setter for <code>frickl.albums.updated_on</code>. When this record has last been updated.
     */
    public void setUpdatedOn(Timestamp value) {
        set(7, value);
    }

    /**
     * Getter for <code>frickl.albums.updated_on</code>. When this record has last been updated.
     */
    public Timestamp getUpdatedOn() {
        return (Timestamp) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<Integer, String, String, String, Integer, Integer, Timestamp, Timestamp> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<Integer, String, String, String, Integer, Integer, Timestamp, Timestamp> valuesRow() {
        return (Row8) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return Albums.ALBUMS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Albums.ALBUMS.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Albums.ALBUMS.DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Albums.ALBUMS.PATH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field5() {
        return Albums.ALBUMS.BANNER_IMAGE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field6() {
        return Albums.ALBUMS.PARENT_ALBUM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field7() {
        return Albums.ALBUMS.CREATED_ON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field8() {
        return Albums.ALBUMS.UPDATED_ON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component5() {
        return getBannerImageId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component6() {
        return getParentAlbumId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component7() {
        return getCreatedOn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component8() {
        return getUpdatedOn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value5() {
        return getBannerImageId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value6() {
        return getParentAlbumId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value7() {
        return getCreatedOn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value8() {
        return getUpdatedOn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumsRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumsRecord value2(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumsRecord value3(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumsRecord value4(String value) {
        setPath(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumsRecord value5(Integer value) {
        setBannerImageId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumsRecord value6(Integer value) {
        setParentAlbumId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumsRecord value7(Timestamp value) {
        setCreatedOn(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumsRecord value8(Timestamp value) {
        setUpdatedOn(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumsRecord values(Integer value1, String value2, String value3, String value4, Integer value5, Integer value6, Timestamp value7, Timestamp value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached AlbumsRecord
     */
    public AlbumsRecord() {
        super(Albums.ALBUMS);
    }

    /**
     * Create a detached, initialised AlbumsRecord
     */
    public AlbumsRecord(Integer id, String name, String description, String path, Integer bannerImageId, Integer parentAlbumId, Timestamp createdOn, Timestamp updatedOn) {
        super(Albums.ALBUMS);

        set(0, id);
        set(1, name);
        set(2, description);
        set(3, path);
        set(4, bannerImageId);
        set(5, parentAlbumId);
        set(6, createdOn);
        set(7, updatedOn);
    }
}
