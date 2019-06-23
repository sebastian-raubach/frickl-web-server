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

import raubach.fricklweb.server.computed.Exif;
import raubach.fricklweb.server.database.tables.Images;


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
public class ImagesRecord extends UpdatableRecordImpl<ImagesRecord> implements Record8<Integer, String, String, Byte, Exif, Integer, Timestamp, Timestamp> {

    private static final long serialVersionUID = -821978832;

    /**
     * Create a detached ImagesRecord
     */
    public ImagesRecord() {
        super(Images.IMAGES);
    }

    /**
     * Getter for <code>frickl.images.id</code>. Auto incremented id of this table.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Create a detached, initialised ImagesRecord
     */
    public ImagesRecord(Integer id, String path, String name, Byte isFavorite, Exif exif, Integer albumId, Timestamp createdOn, Timestamp updatedOn) {
        super(Images.IMAGES);

        set(0, id);
        set(1, path);
        set(2, name);
        set(3, isFavorite);
        set(4, exif);
        set(5, albumId);
        set(6, createdOn);
        set(7, updatedOn);
    }

    /**
     * Getter for <code>frickl.images.path</code>. The path to the image relative to the base path of the setup.
     */
    public String getPath() {
        return (String) get(1);
    }

    /**
     * Setter for <code>frickl.images.name</code>. The name of the image. This will be the filename.
     */
    public void setName(String value) {
        set(2, value);
    }

    /**
     * Setter for <code>frickl.images.id</code>. Auto incremented id of this table.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Setter for <code>frickl.images.is_favorite</code>. Boolean deciding if this image is one of the favorites.
     */
    public void setIsFavorite(Byte value) {
        set(3, value);
    }

    /**
     * Setter for <code>frickl.images.path</code>. The path to the image relative to the base path of the setup.
     */
    public void setPath(String value) {
        set(1, value);
    }

    /**
     * Setter for <code>frickl.images.exif</code>. Optional Exif information in JSON format.
     */
    public void setExif(Exif value) {
        set(4, value);
    }

    /**
     * Getter for <code>frickl.images.name</code>. The name of the image. This will be the filename.
     */
    public String getName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>frickl.images.album_id</code>. The album this image belongs to. This will be the containing folder.
     */
    public void setAlbumId(Integer value) {
        set(5, value);
    }

    /**
     * Getter for <code>frickl.images.is_favorite</code>. Boolean deciding if this image is one of the favorites.
     */
    public Byte getIsFavorite() {
        return (Byte) get(3);
    }

    /**
     * Getter for <code>frickl.images.exif</code>. Optional Exif information in JSON format.
     */
    public Exif getExif() {
        return (Exif) get(4);
    }

    /**
     * Getter for <code>frickl.images.created_on</code>. When this record has been created.
     */
    public Timestamp getCreatedOn() {
        return (Timestamp) get(6);
    }

    /**
     * Getter for <code>frickl.images.album_id</code>. The album this image belongs to. This will be the containing folder.
     */
    public Integer getAlbumId() {
        return (Integer) get(5);
    }

    /**
     * Getter for <code>frickl.images.updated_on</code>. When this record has last been updated.
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
    public Row8<Integer, String, String, Byte, Exif, Integer, Timestamp, Timestamp> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<Integer, String, String, Byte, Exif, Integer, Timestamp, Timestamp> valuesRow() {
        return (Row8) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return Images.IMAGES.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Images.IMAGES.PATH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Images.IMAGES.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Byte> field4() {
        return Images.IMAGES.IS_FAVORITE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Exif> field5() {
        return Images.IMAGES.EXIF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field6() {
        return Images.IMAGES.ALBUM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field7() {
        return Images.IMAGES.CREATED_ON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field8() {
        return Images.IMAGES.UPDATED_ON;
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
        return getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte component4() {
        return getIsFavorite();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Exif component5() {
        return getExif();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component6() {
        return getAlbumId();
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
        return getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte value4() {
        return getIsFavorite();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Exif value5() {
        return getExif();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value6() {
        return getAlbumId();
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
    public ImagesRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImagesRecord value2(String value) {
        setPath(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImagesRecord value3(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImagesRecord value4(Byte value) {
        setIsFavorite(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImagesRecord value5(Exif value) {
        setExif(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImagesRecord value6(Integer value) {
        setAlbumId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImagesRecord value7(Timestamp value) {
        setCreatedOn(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImagesRecord value8(Timestamp value) {
        setUpdatedOn(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImagesRecord values(Integer value1, String value2, String value3, Byte value4, Exif value5, Integer value6, Timestamp value7, Timestamp value8) {
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
     * Setter for <code>frickl.images.created_on</code>. When this record has been created.
     */
    public void setCreatedOn(Timestamp value) {
        set(6, value);
    }

    /**
     * Setter for <code>frickl.images.updated_on</code>. When this record has last been updated.
     */
    public void setUpdatedOn(Timestamp value) {
        set(7, value);
    }
}
