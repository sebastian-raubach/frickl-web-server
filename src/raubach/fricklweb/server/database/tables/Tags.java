/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables;


import org.jooq.*;
import org.jooq.impl.*;

import java.sql.*;
import java.util.*;

import javax.annotation.*;

import raubach.fricklweb.server.database.*;
import raubach.fricklweb.server.database.tables.records.*;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.11.9"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Tags extends TableImpl<TagsRecord>
{

	/**
	 * The reference instance of <code>frickl.tags</code>
	 */
	public static final  Tags                              TAGS             = new Tags();
	private static final long                              serialVersionUID = -634546269;
	/**
	 * The column <code>frickl.tags.id</code>.
	 */
	public final         TableField<TagsRecord, Integer>   ID               = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).identity(true), this, "");
	/**
	 * The column <code>frickl.tags.name</code>.
	 */
	public final         TableField<TagsRecord, String>    NAME             = createField("name", org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");
	/**
	 * The column <code>frickl.tags.created_on</code>.
	 */
	public final         TableField<TagsRecord, Timestamp> CREATED_ON       = createField("created_on", org.jooq.impl.SQLDataType.TIMESTAMP.defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");
	/**
	 * The column <code>frickl.tags.updated_on</code>.
	 */
	public final         TableField<TagsRecord, Timestamp> UPDATED_ON       = createField("updated_on", org.jooq.impl.SQLDataType.TIMESTAMP.defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

	/**
	 * Create a <code>frickl.tags</code> table reference
	 */
	public Tags()
	{
		this(DSL.name("tags"), null);
	}

	/**
	 * Create an aliased <code>frickl.tags</code> table reference
	 */
	public Tags(String alias)
	{
		this(DSL.name(alias), TAGS);
	}

	/**
	 * Create an aliased <code>frickl.tags</code> table reference
	 */
	public Tags(Name alias)
	{
		this(alias, TAGS);
	}

	private Tags(Name alias, Table<TagsRecord> aliased)
	{
		this(alias, aliased, null);
	}

	private Tags(Name alias, Table<TagsRecord> aliased, Field<?>[] parameters)
	{
		super(alias, null, aliased, parameters, DSL.comment(""));
	}

	public <O extends Record> Tags(Table<O> child, ForeignKey<O, TagsRecord> key)
	{
		super(child, key, TAGS);
	}

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<TagsRecord> getRecordType()
	{
		return TagsRecord.class;
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
	public List<Index> getIndexes()
	{
		return Arrays.<Index>asList(Indexes.TAGS_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<TagsRecord, Integer> getIdentity()
	{
		return Keys.IDENTITY_TAGS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<TagsRecord> getPrimaryKey()
	{
		return Keys.KEY_TAGS_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<TagsRecord>> getKeys()
	{
		return Arrays.<UniqueKey<TagsRecord>>asList(Keys.KEY_TAGS_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tags as(String alias)
	{
		return new Tags(DSL.name(alias), this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tags as(Name alias)
	{
		return new Tags(alias, this);
	}

	/**
	 * Rename this table
	 */
	@Override
	public Tags rename(String name)
	{
		return new Tags(DSL.name(name), null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public Tags rename(Name name)
	{
		return new Tags(name, null);
	}
}
