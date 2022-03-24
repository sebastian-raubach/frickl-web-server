/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables;


import jakarta.annotation.Generated;

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
import raubach.fricklweb.server.database.tables.records.ImageTimelineRecord;


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
public class ImageTimeline extends TableImpl<ImageTimelineRecord> {

    private static final long serialVersionUID = 1523171147;

    /**
     * The reference instance of <code>frickl.image_timeline</code>
     */
    public static final ImageTimeline IMAGE_TIMELINE = new ImageTimeline();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ImageTimelineRecord> getRecordType() {
        return ImageTimelineRecord.class;
    }

    /**
     * The column <code>frickl.image_timeline.year</code>.
     */
    public final TableField<ImageTimelineRecord, Integer> YEAR = createField("year", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>frickl.image_timeline.month</code>.
     */
    public final TableField<ImageTimelineRecord, Integer> MONTH = createField("month", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * @deprecated Unknown data type. Please define an explicit {@link org.jooq.Binding} to specify how this type should be handled. Deprecation can be turned off using {@literal <deprecationOnUnknownTypes/>} in your code generator configuration.
     */
    @java.lang.Deprecated
    public final TableField<ImageTimelineRecord, Object> IDS = createField("ids", org.jooq.impl.DefaultDataType.getDefaultDataType("\"frickl\".\"image_timeline_ids\""), this, "");

    /**
     * Create a <code>frickl.image_timeline</code> table reference
     */
    public ImageTimeline() {
        this(DSL.name("image_timeline"), null);
    }

    /**
     * Create an aliased <code>frickl.image_timeline</code> table reference
     */
    public ImageTimeline(String alias) {
        this(DSL.name(alias), IMAGE_TIMELINE);
    }

    /**
     * Create an aliased <code>frickl.image_timeline</code> table reference
     */
    public ImageTimeline(Name alias) {
        this(alias, IMAGE_TIMELINE);
    }

    private ImageTimeline(Name alias, Table<ImageTimelineRecord> aliased) {
        this(alias, aliased, null);
    }

    private ImageTimeline(Name alias, Table<ImageTimelineRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("VIEW"));
    }

    public <O extends Record> ImageTimeline(Table<O> child, ForeignKey<O, ImageTimelineRecord> key) {
        super(child, key, IMAGE_TIMELINE);
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
    public ImageTimeline as(String alias) {
        return new ImageTimeline(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageTimeline as(Name alias) {
        return new ImageTimeline(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ImageTimeline rename(String name) {
        return new ImageTimeline(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ImageTimeline rename(Name name) {
        return new ImageTimeline(name, null);
    }
}
