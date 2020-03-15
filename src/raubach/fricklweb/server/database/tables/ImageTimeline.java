/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables;


import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;
import raubach.fricklweb.server.database.Frickl;
import raubach.fricklweb.server.database.tables.records.ImageTimelineRecord;

import javax.annotation.Generated;


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
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ImageTimeline extends TableImpl<ImageTimelineRecord>
{

	/**
	 * The reference instance of <code>frickl.image_timeline</code>
	 */
	public static final ImageTimeline IMAGE_TIMELINE = new ImageTimeline();
	private static final long serialVersionUID = -291521238;
	/**
	 * The column <code>frickl.image_timeline.year</code>.
	 */
	public final TableField<ImageTimelineRecord, UInteger> YEAR = createField("year", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, this, "");
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
	public ImageTimeline()
	{
		this(DSL.name("image_timeline"), null);
	}

	/**
	 * Create an aliased <code>frickl.image_timeline</code> table reference
	 */
	public ImageTimeline(String alias)
	{
		this(DSL.name(alias), IMAGE_TIMELINE);
	}

	/**
	 * Create an aliased <code>frickl.image_timeline</code> table reference
	 */
	public ImageTimeline(Name alias)
	{
		this(alias, IMAGE_TIMELINE);
	}

	private ImageTimeline(Name alias, Table<ImageTimelineRecord> aliased)
	{
		this(alias, aliased, null);
	}

	private ImageTimeline(Name alias, Table<ImageTimelineRecord> aliased, Field<?>[] parameters)
	{
		super(alias, null, aliased, parameters, DSL.comment("VIEW"));
	}

	public <O extends Record> ImageTimeline(Table<O> child, ForeignKey<O, ImageTimelineRecord> key)
	{
		super(child, key, IMAGE_TIMELINE);
	}

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<ImageTimelineRecord> getRecordType()
	{
		return ImageTimelineRecord.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Schema getSchema()
	{
		return Frickl.FRICKL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImageTimeline as(String alias)
	{
		return new ImageTimeline(DSL.name(alias), this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImageTimeline as(Name alias)
	{
		return new ImageTimeline(alias, this);
	}

	/**
	 * Rename this table
	 */
	@Override
	public ImageTimeline rename(String name)
	{
		return new ImageTimeline(DSL.name(name), null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public ImageTimeline rename(Name name)
	{
		return new ImageTimeline(name, null);
	}
}
