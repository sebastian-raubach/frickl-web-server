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
import raubach.fricklweb.server.database.tables.records.TagsRecord;


/**
 * This table contains all tags/keywords that have been defined and assigned 
 * to images.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tags extends TableImpl<TagsRecord> {

    private static final long serialVersionUID = -144341892;

    /**
     * The reference instance of <code>frickl.tags</code>
     */
    public static final Tags TAGS = new Tags();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TagsRecord> getRecordType() {
        return TagsRecord.class;
    }

    /**
     * The column <code>frickl.tags.id</code>. Auto incremented id of this table.
     */
    public final TableField<TagsRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).identity(true), this, "Auto incremented id of this table.");

    /**
     * The column <code>frickl.tags.name</code>. The name of this tag.
     */
    public final TableField<TagsRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "The name of this tag.");

    /**
     * The column <code>frickl.tags.created_on</code>. When this record has been created.
     */
    public final TableField<TagsRecord, Timestamp> CREATED_ON = createField("created_on", org.jooq.impl.SQLDataType.TIMESTAMP.defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "When this record has been created.");

    /**
     * The column <code>frickl.tags.updated_on</code>. When this record has last been updated.
     */
    public final TableField<TagsRecord, Timestamp> UPDATED_ON = createField("updated_on", org.jooq.impl.SQLDataType.TIMESTAMP.defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "When this record has last been updated.");

    /**
     * Create a <code>frickl.tags</code> table reference
     */
    public Tags() {
        this(DSL.name("tags"), null);
    }

    /**
     * Create an aliased <code>frickl.tags</code> table reference
     */
    public Tags(String alias) {
        this(DSL.name(alias), TAGS);
    }

    /**
     * Create an aliased <code>frickl.tags</code> table reference
     */
    public Tags(Name alias) {
        this(alias, TAGS);
    }

    private Tags(Name alias, Table<TagsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Tags(Name alias, Table<TagsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("This table contains all tags/keywords that have been defined and assigned to images."));
    }

    public <O extends Record> Tags(Table<O> child, ForeignKey<O, TagsRecord> key) {
        super(child, key, TAGS);
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
        return Arrays.<Index>asList(Indexes.TAGS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<TagsRecord, Integer> getIdentity() {
        return Keys.IDENTITY_TAGS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<TagsRecord> getPrimaryKey() {
        return Keys.KEY_TAGS_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<TagsRecord>> getKeys() {
        return Arrays.<UniqueKey<TagsRecord>>asList(Keys.KEY_TAGS_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tags as(String alias) {
        return new Tags(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tags as(Name alias) {
        return new Tags(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Tags rename(String name) {
        return new Tags(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Tags rename(Name name) {
        return new Tags(name, null);
    }
}
