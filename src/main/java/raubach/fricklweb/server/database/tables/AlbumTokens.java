/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables;


import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import raubach.fricklweb.server.database.Frickl;
import raubach.fricklweb.server.database.Indexes;
import raubach.fricklweb.server.database.Keys;
import raubach.fricklweb.server.database.tables.records.AlbumTokensRecord;


/**
 * This table contains the mapping between access tokens and albums.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AlbumTokens extends TableImpl<AlbumTokensRecord> {

    private static final long serialVersionUID = -1313804738;

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
     * The column <code>frickl.album_tokens.album_id</code>. The album this token belongs to.
     */
    public final TableField<AlbumTokensRecord, Integer> ALBUM_ID = createField("album_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "The album this token belongs to.");

    /**
     * The column <code>frickl.album_tokens.access_token_id</code>. The access token allowing access to this album.
     */
    public final TableField<AlbumTokensRecord, Integer> ACCESS_TOKEN_ID = createField("access_token_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "The access token allowing access to this album.");

    /**
     * The column <code>frickl.album_tokens.created_on</code>. When this record has been created.
     */
    public final TableField<AlbumTokensRecord, Timestamp> CREATED_ON = createField("created_on", org.jooq.impl.SQLDataType.TIMESTAMP.defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "When this record has been created.");

    /**
     * The column <code>frickl.album_tokens.updated_on</code>. When this record has last been updated.
     */
    public final TableField<AlbumTokensRecord, Timestamp> UPDATED_ON = createField("updated_on", org.jooq.impl.SQLDataType.TIMESTAMP.defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "When this record has last been updated.");

    /**
     * Create a <code>frickl.album_tokens</code> table reference
     */
    public AlbumTokens() {
        this(DSL.name("album_tokens"), null);
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

    private AlbumTokens(Name alias, Table<AlbumTokensRecord> aliased) {
        this(alias, aliased, null);
    }

    private AlbumTokens(Name alias, Table<AlbumTokensRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("This table contains the mapping between access tokens and albums."));
    }

    public <O extends Record> AlbumTokens(Table<O> child, ForeignKey<O, AlbumTokensRecord> key) {
        super(child, key, ALBUM_TOKENS);
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
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.ALBUM_TOKENS_ALBUM_TOKENS_IBFK_1, Indexes.ALBUM_TOKENS_ALBUM_TOKENS_IBFK_2, Indexes.ALBUM_TOKENS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<AlbumTokensRecord> getPrimaryKey() {
        return Keys.KEY_ALBUM_TOKENS_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<AlbumTokensRecord>> getKeys() {
        return Arrays.<UniqueKey<AlbumTokensRecord>>asList(Keys.KEY_ALBUM_TOKENS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<AlbumTokensRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<AlbumTokensRecord, ?>>asList(Keys.ALBUM_TOKENS_IBFK_1, Keys.ALBUM_TOKENS_IBFK_2);
    }

    public Albums albums() {
        return new Albums(this, Keys.ALBUM_TOKENS_IBFK_1);
    }

    public AccessTokens accessTokens() {
        return new AccessTokens(this, Keys.ALBUM_TOKENS_IBFK_2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumTokens as(String alias) {
        return new AlbumTokens(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
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
}