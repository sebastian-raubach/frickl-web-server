/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables;


import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row7;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import raubach.fricklweb.server.database.Frickl;
import raubach.fricklweb.server.database.Keys;
import raubach.fricklweb.server.database.tables.records.AlbumCountsRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AlbumCounts extends TableImpl<AlbumCountsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>frickl.album_counts</code>
     */
    public static final AlbumCounts ALBUM_COUNTS = new AlbumCounts();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AlbumCountsRecord> getRecordType() {
        return AlbumCountsRecord.class;
    }

    /**
     * The column <code>frickl.album_counts.album_id</code>.
     */
    public final TableField<AlbumCountsRecord, Integer> ALBUM_ID = createField(DSL.name("album_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>frickl.album_counts.image_count</code>.
     */
    public final TableField<AlbumCountsRecord, Integer> IMAGE_COUNT = createField(DSL.name("image_count"), SQLDataType.INTEGER.nullable(false).defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>frickl.album_counts.image_count_public</code>.
     */
    public final TableField<AlbumCountsRecord, Integer> IMAGE_COUNT_PUBLIC = createField(DSL.name("image_count_public"), SQLDataType.INTEGER.nullable(false).defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>frickl.album_counts.album_count</code>.
     */
    public final TableField<AlbumCountsRecord, Integer> ALBUM_COUNT = createField(DSL.name("album_count"), SQLDataType.INTEGER.nullable(false).defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>frickl.album_counts.image_view_count</code>.
     */
    public final TableField<AlbumCountsRecord, Integer> IMAGE_VIEW_COUNT = createField(DSL.name("image_view_count"), SQLDataType.INTEGER.nullable(false).defaultValue(DSL.inline("0", SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>frickl.album_counts.newest_image</code>.
     */
    public final TableField<AlbumCountsRecord, Timestamp> NEWEST_IMAGE = createField(DSL.name("newest_image"), SQLDataType.TIMESTAMP(0), this, "");

    /**
     * The column <code>frickl.album_counts.oldest_image</code>.
     */
    public final TableField<AlbumCountsRecord, Timestamp> OLDEST_IMAGE = createField(DSL.name("oldest_image"), SQLDataType.TIMESTAMP(0), this, "");

    private AlbumCounts(Name alias, Table<AlbumCountsRecord> aliased) {
        this(alias, aliased, null);
    }

    private AlbumCounts(Name alias, Table<AlbumCountsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>frickl.album_counts</code> table reference
     */
    public AlbumCounts(String alias) {
        this(DSL.name(alias), ALBUM_COUNTS);
    }

    /**
     * Create an aliased <code>frickl.album_counts</code> table reference
     */
    public AlbumCounts(Name alias) {
        this(alias, ALBUM_COUNTS);
    }

    /**
     * Create a <code>frickl.album_counts</code> table reference
     */
    public AlbumCounts() {
        this(DSL.name("album_counts"), null);
    }

    public <O extends Record> AlbumCounts(Table<O> child, ForeignKey<O, AlbumCountsRecord> key) {
        super(child, key, ALBUM_COUNTS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Frickl.FRICKL;
    }

    @Override
    public UniqueKey<AlbumCountsRecord> getPrimaryKey() {
        return Keys.KEY_ALBUM_COUNTS_PRIMARY;
    }

    @Override
    public List<ForeignKey<AlbumCountsRecord, ?>> getReferences() {
        return Arrays.asList(Keys.ALBUM_COUNTS_IBFK_1);
    }

    private transient Albums _albums;

    /**
     * Get the implicit join path to the <code>frickl.albums</code> table.
     */
    public Albums albums() {
        if (_albums == null)
            _albums = new Albums(this, Keys.ALBUM_COUNTS_IBFK_1);

        return _albums;
    }

    @Override
    public AlbumCounts as(String alias) {
        return new AlbumCounts(DSL.name(alias), this);
    }

    @Override
    public AlbumCounts as(Name alias) {
        return new AlbumCounts(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public AlbumCounts rename(String name) {
        return new AlbumCounts(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AlbumCounts rename(Name name) {
        return new AlbumCounts(name, null);
    }

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row7<Integer, Integer, Integer, Integer, Integer, Timestamp, Timestamp> fieldsRow() {
        return (Row7) super.fieldsRow();
    }
}
