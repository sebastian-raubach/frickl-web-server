/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.records;


import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;

import raubach.fricklweb.server.database.tables.AlbumTokens;


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
public class AlbumTokensRecord extends UpdatableRecordImpl<AlbumTokensRecord> implements Record4<Integer, Integer, Timestamp, Timestamp> {

    private static final long serialVersionUID = 89118807;

    /**
     * Create a detached AlbumTokensRecord
     */
    public AlbumTokensRecord() {
        super(AlbumTokens.ALBUM_TOKENS);
    }

    /**
     * Create a detached, initialised AlbumTokensRecord
     */
    public AlbumTokensRecord(Integer albumId, Integer accessTokenId, Timestamp createdOn, Timestamp updatedOn) {
        super(AlbumTokens.ALBUM_TOKENS);

        set(0, albumId);
        set(1, accessTokenId);
        set(2, createdOn);
        set(3, updatedOn);
    }

    /**
     * Getter for <code>frickl.album_tokens.album_id</code>. The album this token belongs to.
     */
    public Integer getAlbumId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>frickl.album_tokens.album_id</code>. The album this token belongs to.
     */
    public void setAlbumId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>frickl.album_tokens.access_token_id</code>. The access token allowing access to this album.
     */
    public Integer getAccessTokenId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>frickl.album_tokens.access_token_id</code>. The access token allowing access to this album.
     */
    public void setAccessTokenId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>frickl.album_tokens.created_on</code>. When this record has been created.
     */
    public Timestamp getCreatedOn() {
        return (Timestamp) get(2);
    }

    /**
     * Setter for <code>frickl.album_tokens.created_on</code>. When this record has been created.
     */
    public void setCreatedOn(Timestamp value) {
        set(2, value);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * Getter for <code>frickl.album_tokens.updated_on</code>. When this record has last been updated.
     */
    public Timestamp getUpdatedOn() {
        return (Timestamp) get(3);
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * Setter for <code>frickl.album_tokens.updated_on</code>. When this record has last been updated.
     */
    public void setUpdatedOn(Timestamp value) {
        set(3, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Record2<Integer, Integer> key() {
        return (Record2) super.key();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Integer, Integer, Timestamp, Timestamp> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Integer, Integer, Timestamp, Timestamp> valuesRow() {
        return (Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return AlbumTokens.ALBUM_TOKENS.ALBUM_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return AlbumTokens.ALBUM_TOKENS.ACCESS_TOKEN_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field3() {
        return AlbumTokens.ALBUM_TOKENS.CREATED_ON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field4() {
        return AlbumTokens.ALBUM_TOKENS.UPDATED_ON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component1() {
        return getAlbumId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component2() {
        return getAccessTokenId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component3() {
        return getCreatedOn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component4() {
        return getUpdatedOn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getAlbumId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value2() {
        return getAccessTokenId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value3() {
        return getCreatedOn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value4() {
        return getUpdatedOn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumTokensRecord value1(Integer value) {
        setAlbumId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumTokensRecord value2(Integer value) {
        setAccessTokenId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumTokensRecord value3(Timestamp value) {
        setCreatedOn(value);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumTokensRecord value4(Timestamp value) {
        setUpdatedOn(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumTokensRecord values(Integer value1, Integer value2, Timestamp value3, Timestamp value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }
}
