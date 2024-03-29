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
import org.jooq.Row4;
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
import raubach.fricklweb.server.database.tables.records.AlbumTokensRecord;


/**
 * This table contains the mapping between access tokens and albums.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AlbumTokens extends TableImpl<AlbumTokensRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>frickl.album_tokens</code>
     */
    public static final AlbumTokens ALBUM_TOKENS = new AlbumTokens();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AlbumTokensRecord> getRecordType() {
        return AlbumTokensRecord.class;
    }

    /**
     * The column <code>frickl.album_tokens.album_id</code>. The album this
     * token belongs to.
     */
    public final TableField<AlbumTokensRecord, Integer> ALBUM_ID = createField(DSL.name("album_id"), SQLDataType.INTEGER.nullable(false), this, "The album this token belongs to.");

    /**
     * The column <code>frickl.album_tokens.access_token_id</code>. The access
     * token allowing access to this album.
     */
    public final TableField<AlbumTokensRecord, Integer> ACCESS_TOKEN_ID = createField(DSL.name("access_token_id"), SQLDataType.INTEGER.nullable(false), this, "The access token allowing access to this album.");

    /**
     * The column <code>frickl.album_tokens.created_on</code>. When this record
     * has been created.
     */
    public final TableField<AlbumTokensRecord, Timestamp> CREATED_ON = createField(DSL.name("created_on"), SQLDataType.TIMESTAMP(0).defaultValue(DSL.field("CURRENT_TIMESTAMP", SQLDataType.TIMESTAMP)), this, "When this record has been created.");

    /**
     * The column <code>frickl.album_tokens.updated_on</code>. When this record
     * has last been updated.
     */
    public final TableField<AlbumTokensRecord, Timestamp> UPDATED_ON = createField(DSL.name("updated_on"), SQLDataType.TIMESTAMP(0).defaultValue(DSL.field("CURRENT_TIMESTAMP", SQLDataType.TIMESTAMP)), this, "When this record has last been updated.");

    private AlbumTokens(Name alias, Table<AlbumTokensRecord> aliased) {
        this(alias, aliased, null);
    }

    private AlbumTokens(Name alias, Table<AlbumTokensRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("This table contains the mapping between access tokens and albums."), TableOptions.table());
    }

    /**
     * Create an aliased <code>frickl.album_tokens</code> table reference
     */
    public AlbumTokens(String alias) {
        this(DSL.name(alias), ALBUM_TOKENS);
    }

    /**
     * Create an aliased <code>frickl.album_tokens</code> table reference
     */
    public AlbumTokens(Name alias) {
        this(alias, ALBUM_TOKENS);
    }

    /**
     * Create a <code>frickl.album_tokens</code> table reference
     */
    public AlbumTokens() {
        this(DSL.name("album_tokens"), null);
    }

    public <O extends Record> AlbumTokens(Table<O> child, ForeignKey<O, AlbumTokensRecord> key) {
        super(child, key, ALBUM_TOKENS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Frickl.FRICKL;
    }

    @Override
    public UniqueKey<AlbumTokensRecord> getPrimaryKey() {
        return Keys.KEY_ALBUM_TOKENS_PRIMARY;
    }

    @Override
    public List<ForeignKey<AlbumTokensRecord, ?>> getReferences() {
        return Arrays.asList(Keys.ALBUM_TOKENS_IBFK_1, Keys.ALBUM_TOKENS_IBFK_2);
    }

    private transient Albums _albums;
    private transient AccessTokens _accessTokens;

    /**
     * Get the implicit join path to the <code>frickl.albums</code> table.
     */
    public Albums albums() {
        if (_albums == null)
            _albums = new Albums(this, Keys.ALBUM_TOKENS_IBFK_1);

        return _albums;
    }

    /**
     * Get the implicit join path to the <code>frickl.access_tokens</code>
     * table.
     */
    public AccessTokens accessTokens() {
        if (_accessTokens == null)
            _accessTokens = new AccessTokens(this, Keys.ALBUM_TOKENS_IBFK_2);

        return _accessTokens;
    }

    @Override
    public AlbumTokens as(String alias) {
        return new AlbumTokens(DSL.name(alias), this);
    }

    @Override
    public AlbumTokens as(Name alias) {
        return new AlbumTokens(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public AlbumTokens rename(String name) {
        return new AlbumTokens(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AlbumTokens rename(Name name) {
        return new AlbumTokens(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, Integer, Timestamp, Timestamp> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
