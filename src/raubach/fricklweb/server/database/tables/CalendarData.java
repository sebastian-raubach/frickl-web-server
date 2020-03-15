/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables;


import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
import raubach.fricklweb.server.database.Frickl;
import raubach.fricklweb.server.database.tables.records.CalendarDataRecord;

import javax.annotation.Generated;
import java.sql.Date;


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
public class CalendarData extends TableImpl<CalendarDataRecord>
{

	/**
	 * The reference instance of <code>frickl.calendar_data</code>
	 */
	public static final CalendarData CALENDAR_DATA = new CalendarData();
	private static final long serialVersionUID = 1140774171;
	/**
	 * The column <code>frickl.calendar_data.date</code>.
	 */
	public final TableField<CalendarDataRecord, Date> DATE = createField("date", org.jooq.impl.SQLDataType.DATE, this, "");
	/**
	 * The column <code>frickl.calendar_data.count</code>.
	 */
	public final TableField<CalendarDataRecord, Long> COUNT = createField("count", org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.BIGINT)), this, "");

	/**
	 * Create a <code>frickl.calendar_data</code> table reference
	 */
	public CalendarData()
	{
		this(DSL.name("calendar_data"), null);
	}

	/**
	 * Create an aliased <code>frickl.calendar_data</code> table reference
	 */
	public CalendarData(String alias)
	{
		this(DSL.name(alias), CALENDAR_DATA);
	}

	/**
	 * Create an aliased <code>frickl.calendar_data</code> table reference
	 */
	public CalendarData(Name alias)
	{
		this(alias, CALENDAR_DATA);
	}

	private CalendarData(Name alias, Table<CalendarDataRecord> aliased)
	{
		this(alias, aliased, null);
	}

	private CalendarData(Name alias, Table<CalendarDataRecord> aliased, Field<?>[] parameters)
	{
		super(alias, null, aliased, parameters, DSL.comment("VIEW"));
	}

	public <O extends Record> CalendarData(Table<O> child, ForeignKey<O, CalendarDataRecord> key)
	{
		super(child, key, CALENDAR_DATA);
	}

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<CalendarDataRecord> getRecordType()
	{
		return CalendarDataRecord.class;
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
	public CalendarData as(String alias)
	{
		return new CalendarData(DSL.name(alias), this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CalendarData as(Name alias)
	{
		return new CalendarData(alias, this);
	}

	/**
	 * Rename this table
	 */
	@Override
	public CalendarData rename(String name)
	{
		return new CalendarData(DSL.name(name), null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public CalendarData rename(Name name)
	{
		return new CalendarData(name, null);
	}
}
