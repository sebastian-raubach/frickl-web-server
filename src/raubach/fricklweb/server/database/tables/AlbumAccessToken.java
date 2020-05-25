/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables;


import java.sql.Timestamp;

import javax.annotation.Generated;

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
import raubach.fricklweb.server.database.tables.records.AlbumAccessTokenRecord;


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
public class AlbumAccessToken extends TableImpl<AlbumAccessTokenRecord> {

    private static final long serialVersionUID = -1370202886;

    /**
     * The reference instance of <code>frickl.album_access_token</code>
     */
    public static final AlbumAccessToken ALBUM_ACCESS_TOKEN = new AlbumAccessToken();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AlbumAccessTokenRecord> getRecordType() {
        return AlbumAccessTokenRecord.class;
    }

    /**
     * The column <code>frickl.album_access_token.album_id</code>. Auto incremented id of this table.
     */
    public final TableField<AlbumAccessTokenRecord, Integer> ALBUM_ID = createField("album_id", org.jooq.impl.SQLDataType.INTEGER.defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.INTEGER)), this, "Auto incremented id of this table.");

    /**
     * The column <code>frickl.album_access_token.album_name</code>. The name of the album. Should ideally be relatively short.
     */
    public final TableField<AlbumAccessTokenRecord, String> ALBUM_NAME = createField("album_name", org.jooq.impl.SQLDataType.VARCHAR(255), this, "The name of the album. Should ideally be relatively short.");

    /**
     * The column <code>frickl.album_access_token.album_description</code>. Optional description of the album.
     */
    public final TableField<AlbumAccessTokenRecord, String> ALBUM_DESCRIPTION = createField("album_description", org.jooq.impl.SQLDataType.CLOB, this, "Optional description of the album.");

    /**
     * The column <code>frickl.album_access_token.token_id</code>. Auto incremented id of this table.
     */
    public final TableField<AlbumAccessTokenRecord, Integer> TOKEN_ID = createField("token_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaultValue(org.jooq.impl.DSL.inline("0", org.jooq.impl.SQLDataType.INTEGER)), this, "Auto incremented id of this table.");

    /**
     * The column <code>frickl.album_access_token.token_token</code>. The access token.
     */
    public final TableField<AlbumAccessTokenRecord, String> TOKEN_TOKEN = createField("token_token", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "The access token.");

    /**
     * The column <code>frickl.album_access_token.token_expires_on</code>. When this token expires.
     */
    public final TableField<AlbumAccessTokenRecord, Timestamp> TOKEN_EXPIRES_ON = createField("token_expires_on", org.jooq.impl.SQLDataType.TIMESTAMP, this, "When this token expires.");

    /**
     * Create a <code>frickl.album_access_token</code> table reference
     */
    public AlbumAccessToken() {
        this(DSL.name("album_access_token"), null);
    }

    /**
     * Create an aliased <code>frickl.album_access_token</code> table reference
     */
    public AlbumAccessToken(String alias) {
        this(DSL.name(alias), ALBUM_ACCESS_TOKEN);
    }

    /**
     * Create an aliased <code>frickl.album_access_token</code> table reference
     */
    public AlbumAccessToken(Name alias) {
        this(alias, ALBUM_ACCESS_TOKEN);
    }

    private AlbumAccessToken(Name alias, Table<AlbumAccessTokenRecord> aliased) {
        this(alias, aliased, null);
    }

    private AlbumAccessToken(Name alias, Table<AlbumAccessTokenRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("VIEW"));
    }

    public <O extends Record> AlbumAccessToken(Table<O> child, ForeignKey<O, AlbumAccessTokenRecord> key) {
        super(child, key, ALBUM_ACCESS_TOKEN);
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
    public AlbumAccessToken as(String alias) {
        return new AlbumAccessToken(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumAccessToken as(Name alias) {
        return new AlbumAccessToken(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public AlbumAccessToken rename(String name) {
        return new AlbumAccessToken(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AlbumAccessToken rename(Name name) {
        return new AlbumAccessToken(name, null);
    }
}
