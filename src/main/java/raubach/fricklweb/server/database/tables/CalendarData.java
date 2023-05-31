/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables;


import java.sql.Date;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row2;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import raubach.fricklweb.server.database.Frickl;
import raubach.fricklweb.server.database.tables.records.CalendarDataRecord;


/**
 * VIEW
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CalendarData extends TableImpl<CalendarDataRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>frickl.calendar_data</code>
     */
    public static final CalendarData CALENDAR_DATA = new CalendarData();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<CalendarDataRecord> getRecordType() {
        return CalendarDataRecord.class;
    }

    /**
     * The column <code>frickl.calendar_data.date</code>.
     */
    public final TableField<CalendarDataRecord, Date> DATE = createField(DSL.name("date"), SQLDataType.DATE, this, "");

    /**
     * The column <code>frickl.calendar_data.count</code>.
     */
    public final TableField<CalendarDataRecord, Long> COUNT = createField(DSL.name("count"), SQLDataType.BIGINT.nullable(false).defaultValue(DSL.inline("0", SQLDataType.BIGINT)), this, "");

    private CalendarData(Name alias, Table<CalendarDataRecord> aliased) {
        this(alias, aliased, null);
    }

    private CalendarData(Name alias, Table<CalendarDataRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("VIEW"), TableOptions.view("create view `calendar_data` as select cast(`frickl`.`images`.`created_on` as date) AS `date`,count(1) AS `count` from `frickl`.`images` where (`frickl`.`images`.`created_on` is not null) group by `date` order by `date` desc"));
    }

    /**
     * Create an aliased <code>frickl.calendar_data</code> table reference
     */
    public CalendarData(String alias) {
        this(DSL.name(alias), CALENDAR_DATA);
    }

    /**
     * Create an aliased <code>frickl.calendar_data</code> table reference
     */
    public CalendarData(Name alias) {
        this(alias, CALENDAR_DATA);
    }

    /**
     * Create a <code>frickl.calendar_data</code> table reference
     */
    public CalendarData() {
        this(DSL.name("calendar_data"), null);
    }

    public <O extends Record> CalendarData(Table<O> child, ForeignKey<O, CalendarDataRecord> key) {
        super(child, key, CALENDAR_DATA);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Frickl.FRICKL;
    }

    @Override
    public CalendarData as(String alias) {
        return new CalendarData(DSL.name(alias), this);
    }

    @Override
    public CalendarData as(Name alias) {
        return new CalendarData(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public CalendarData rename(String name) {
        return new CalendarData(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public CalendarData rename(Name name) {
        return new CalendarData(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<Date, Long> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}
