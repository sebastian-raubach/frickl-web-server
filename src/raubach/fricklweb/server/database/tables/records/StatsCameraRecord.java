/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.records;


import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.TableRecordImpl;

import raubach.fricklweb.server.database.tables.StatsCamera;


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
public class StatsCameraRecord extends TableRecordImpl<StatsCameraRecord> implements Record2<String, Long> {

    private static final long serialVersionUID = -1252538212;

    /**
     * Setter for <code>frickl.stats_camera.camera</code>.
     */
    public void setCamera(String value) {
        set(0, value);
    }

    /**
     * Create a detached StatsCameraRecord
     */
    public StatsCameraRecord() {
        super(StatsCamera.STATS_CAMERA);
    }

    /**
     * Setter for <code>frickl.stats_camera.count</code>.
     */
    public void setCount(Long value) {
        set(1, value);
    }

    /**
     * Create a detached, initialised StatsCameraRecord
     */
    public StatsCameraRecord(String camera, Long count) {
        super(StatsCamera.STATS_CAMERA);

        set(0, camera);
        set(1, count);
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<String, Long> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row2<String, Long> valuesRow() {
        return (Row2) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return StatsCamera.STATS_CAMERA.CAMERA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return StatsCamera.STATS_CAMERA.COUNT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component1() {
        return getCamera();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component2() {
        return getCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getCamera();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value2() {
        return getCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StatsCameraRecord value1(String value) {
        setCamera(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StatsCameraRecord value2(Long value) {
        setCount(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StatsCameraRecord values(String value1, Long value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Getter for <code>frickl.stats_camera.camera</code>.
     */
    public String getCamera() {
        return (String) get(0);
    }

    /**
     * Getter for <code>frickl.stats_camera.count</code>.
     */
    public Long getCount() {
        return (Long) get(1);
    }
}
