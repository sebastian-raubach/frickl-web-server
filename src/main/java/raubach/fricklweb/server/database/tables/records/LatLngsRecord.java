/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.records;


import java.math.BigDecimal;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.TableRecordImpl;

import raubach.fricklweb.server.database.tables.LatLngs;


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
public class LatLngsRecord extends TableRecordImpl<LatLngsRecord> implements Record6<Integer, String, Integer, Byte, BigDecimal, BigDecimal> {

    private static final long serialVersionUID = 1515248477;

    /**
     * Setter for <code>frickl.lat_lngs.id</code>. Auto incremented id of this table.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>frickl.lat_lngs.id</code>. Auto incremented id of this table.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>frickl.lat_lngs.path</code>. The path to the image relative to the base path of the setup.
     */
    public void setPath(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>frickl.lat_lngs.path</code>. The path to the image relative to the base path of the setup.
     */
    public String getPath() {
        return (String) get(1);
    }

    /**
     * Setter for <code>frickl.lat_lngs.album_id</code>. The album this image belongs to. This will be the containing folder.
     */
    public void setAlbumId(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>frickl.lat_lngs.album_id</code>. The album this image belongs to. This will be the containing folder.
     */
    public Integer getAlbumId() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>frickl.lat_lngs.is_public</code>.
     */
    public void setIsPublic(Byte value) {
        set(3, value);
    }

    /**
     * Getter for <code>frickl.lat_lngs.is_public</code>.
     */
    public Byte getIsPublic() {
        return (Byte) get(3);
    }

    /**
     * Setter for <code>frickl.lat_lngs.latitude</code>.
     */
    public void setLatitude(BigDecimal value) {
        set(4, value);
    }

    /**
     * Getter for <code>frickl.lat_lngs.latitude</code>.
     */
    public BigDecimal getLatitude() {
        return (BigDecimal) get(4);
    }

    /**
     * Setter for <code>frickl.lat_lngs.longitude</code>.
     */
    public void setLongitude(BigDecimal value) {
        set(5, value);
    }

    /**
     * Getter for <code>frickl.lat_lngs.longitude</code>.
     */
    public BigDecimal getLongitude() {
        return (BigDecimal) get(5);
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row6<Integer, String, Integer, Byte, BigDecimal, BigDecimal> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row6<Integer, String, Integer, Byte, BigDecimal, BigDecimal> valuesRow() {
        return (Row6) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return LatLngs.LAT_LNGS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return LatLngs.LAT_LNGS.PATH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return LatLngs.LAT_LNGS.ALBUM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Byte> field4() {
        return LatLngs.LAT_LNGS.IS_PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<BigDecimal> field5() {
        return LatLngs.LAT_LNGS.LATITUDE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<BigDecimal> field6() {
        return LatLngs.LAT_LNGS.LONGITUDE;
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
    public Integer component3() {
        return getAlbumId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte component4() {
        return getIsPublic();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal component5() {
        return getLatitude();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal component6() {
        return getLongitude();
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
    public Integer value3() {
        return getAlbumId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte value4() {
        return getIsPublic();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal value5() {
        return getLatitude();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal value6() {
        return getLongitude();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LatLngsRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LatLngsRecord value2(String value) {
        setPath(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LatLngsRecord value3(Integer value) {
        setAlbumId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LatLngsRecord value4(Byte value) {
        setIsPublic(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LatLngsRecord value5(BigDecimal value) {
        setLatitude(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LatLngsRecord value6(BigDecimal value) {
        setLongitude(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LatLngsRecord values(Integer value1, String value2, Integer value3, Byte value4, BigDecimal value5, BigDecimal value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached LatLngsRecord
     */
    public LatLngsRecord() {
        super(LatLngs.LAT_LNGS);
    }

    /**
     * Create a detached, initialised LatLngsRecord
     */
    public LatLngsRecord(Integer id, String path, Integer albumId, Byte isPublic, BigDecimal latitude, BigDecimal longitude) {
        super(LatLngs.LAT_LNGS);

        set(0, id);
        set(1, path);
        set(2, albumId);
        set(3, isPublic);
        set(4, latitude);
        set(5, longitude);
    }
}
