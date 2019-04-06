/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.daos;


import org.jooq.*;
import org.jooq.impl.*;

import java.sql.*;
import java.util.*;

import javax.annotation.*;

import raubach.fricklweb.server.database.tables.*;
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
public class ImagesDao extends DAOImpl<ImagesRecord, raubach.fricklweb.server.database.tables.pojos.Images, Integer>
{

	/**
	 * Create a new ImagesDao without any configuration
	 */
	public ImagesDao()
	{
		super(Images.IMAGES, raubach.fricklweb.server.database.tables.pojos.Images.class);
	}

	/**
	 * Create a new ImagesDao with an attached configuration
	 */
	public ImagesDao(Configuration configuration)
	{
		super(Images.IMAGES, raubach.fricklweb.server.database.tables.pojos.Images.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Integer getId(raubach.fricklweb.server.database.tables.pojos.Images object)
	{
		return object.getId();
	}

	/**
	 * Fetch records that have <code>id IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Images> fetchById(Integer... values)
	{
		return fetch(Images.IMAGES.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>id = value</code>
	 */
	public raubach.fricklweb.server.database.tables.pojos.Images fetchOneById(Integer value)
	{
		return fetchOne(Images.IMAGES.ID, value);
	}

	/**
	 * Fetch records that have <code>path IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Images> fetchByPath(String... values)
	{
		return fetch(Images.IMAGES.PATH, values);
	}

	/**
	 * @deprecated Unknown data type. Please define an explicit {@link org.jooq.Binding} to specify how this type should be handled. Deprecation can be turned off using {@literal <deprecationOnUnknownTypes/>} in your code generator configuration.
	 */
	@java.lang.Deprecated
	public List<raubach.fricklweb.server.database.tables.pojos.Images> fetchByExif(Object... values)
	{
		return fetch(Images.IMAGES.EXIF, values);
	}

	/**
	 * Fetch records that have <code>album_id IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Images> fetchByAlbumId(Integer... values)
	{
		return fetch(Images.IMAGES.ALBUM_ID, values);
	}

	/**
	 * Fetch records that have <code>created_on IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Images> fetchByCreatedOn(Timestamp... values)
	{
		return fetch(Images.IMAGES.CREATED_ON, values);
	}

	/**
	 * Fetch records that have <code>updated_on IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Images> fetchByUpdatedOn(Timestamp... values)
	{
		return fetch(Images.IMAGES.UPDATED_ON, values);
	}
}
