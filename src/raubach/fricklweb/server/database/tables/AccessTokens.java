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
import org.jooq.Identity;
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
import raubach.fricklweb.server.database.tables.records.AccessTokensRecord;


/**
 * This table contains all tags that can be used to access folders that aren't 
 * public.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AccessTokens extends TableImpl<AccessTokensRecord> {

    private static final long serialVersionUID = 892232480;

    /**
     * The reference instance of <code>frickl.access_tokens</code>
     */
    public static final AccessTokens ACCESS_TOKENS = new AccessTokens();
    /**
     * The column <code>frickl.access_tokens.expires_on</code>. When this token expires.
     */
    public final TableField<AccessTokensRecord, Timestamp> EXPIRES_ON = createField("expires_on", org.jooq.impl.SQLDataType.TIMESTAMP, this, "When this token expires.");

    /**
     * The column <code>frickl.access_tokens.id</code>. Auto incremented id of this table.
     */
    public final TableField<AccessTokensRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).identity(true), this, "Auto incremented id of this table.");

    /**
     * The column <code>frickl.access_tokens.token</code>. The access token.
     */
    public final TableField<AccessTokensRecord, String> TOKEN = createField("token", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "The access token.");

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AccessTokensRecord> getRecordType() {
        return AccessTokensRecord.class;
    }

    /**
     * The column <code>frickl.access_tokens.created_on</code>. When this record has been created.
     */
    public final TableField<AccessTokensRecord, Timestamp> CREATED_ON = createField("created_on", org.jooq.impl.SQLDataType.TIMESTAMP.defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "When this record has been created.");

    /**
     * The column <code>frickl.access_tokens.updated_on</code>. When this record has last been updated.
     */
    public final TableField<AccessTokensRecord, Timestamp> UPDATED_ON = createField("updated_on", org.jooq.impl.SQLDataType.TIMESTAMP.defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "When this record has last been updated.");

    /**
     * Create a <code>frickl.access_tokens</code> table reference
     */
    public AccessTokens() {
        this(DSL.name("access_tokens"), null);
    }

    /**
     * Create an aliased <code>frickl.access_tokens</code> table reference
     */
    public AccessTokens(String alias) {
        this(DSL.name(alias), ACCESS_TOKENS);
    }

    /**
     * Create an aliased <code>frickl.access_tokens</code> table reference
     */
    public AccessTokens(Name alias) {
        this(alias, ACCESS_TOKENS);
    }

    private AccessTokens(Name alias, Table<AccessTokensRecord> aliased) {
        this(alias, aliased, null);
    }

    private AccessTokens(Name alias, Table<AccessTokensRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("This table contains all tags that can be used to access folders that aren't public."));
    }

    public <O extends Record> AccessTokens(Table<O> child, ForeignKey<O, AccessTokensRecord> key) {
        super(child, key, ACCESS_TOKENS);
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
        return Arrays.<Index>asList(Indexes.ACCESS_TOKENS_ACCESS_TOKENS_TOKEN, Indexes.ACCESS_TOKENS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<AccessTokensRecord, Integer> getIdentity() {
        return Keys.IDENTITY_ACCESS_TOKENS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<AccessTokensRecord> getPrimaryKey() {
        return Keys.KEY_ACCESS_TOKENS_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<AccessTokensRecord>> getKeys() {
        return Arrays.<UniqueKey<AccessTokensRecord>>asList(Keys.KEY_ACCESS_TOKENS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessTokens as(String alias) {
        return new AccessTokens(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessTokens as(Name alias) {
        return new AccessTokens(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public AccessTokens rename(String name) {
        return new AccessTokens(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AccessTokens rename(Name name) {
        return new AccessTokens(name, null);
    }
}
