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
public class AlbumsDao extends DAOImpl<AlbumsRecord, raubach.fricklweb.server.database.tables.pojos.Albums, Integer>
{

	/**
	 * Create a new AlbumsDao without any configuration
	 */
	public AlbumsDao()
	{
		super(Albums.ALBUMS, raubach.fricklweb.server.database.tables.pojos.Albums.class);
	}

	/**
	 * Create a new AlbumsDao with an attached configuration
	 */
	public AlbumsDao(Configuration configuration)
	{
		super(Albums.ALBUMS, raubach.fricklweb.server.database.tables.pojos.Albums.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Integer getId(raubach.fricklweb.server.database.tables.pojos.Albums object)
	{
		return object.getId();
	}

	/**
	 * Fetch records that have <code>id IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Albums> fetchById(Integer... values)
	{
		return fetch(Albums.ALBUMS.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>id = value</code>
	 */
	public raubach.fricklweb.server.database.tables.pojos.Albums fetchOneById(Integer value)
	{
		return fetchOne(Albums.ALBUMS.ID, value);
	}

	/**
	 * Fetch records that have <code>name IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Albums> fetchByName(String... values)
	{
		return fetch(Albums.ALBUMS.NAME, values);
	}

	/**
	 * Fetch records that have <code>description IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Albums> fetchByDescription(String... values)
	{
		return fetch(Albums.ALBUMS.DESCRIPTION, values);
	}

	/**
	 * Fetch records that have <code>banner_image_id IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Albums> fetchByBannerImageId(Integer... values)
	{
		return fetch(Albums.ALBUMS.BANNER_IMAGE_ID, values);
	}

	/**
	 * Fetch records that have <code>parent_album_id IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Albums> fetchByParentAlbumId(Integer... values)
	{
		return fetch(Albums.ALBUMS.PARENT_ALBUM_ID, values);
	}

	/**
	 * Fetch records that have <code>created_on IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Albums> fetchByCreatedOn(Timestamp... values)
	{
		return fetch(Albums.ALBUMS.CREATED_ON, values);
	}

	/**
	 * Fetch records that have <code>updated_on IN (values)</code>
	 */
	public List<raubach.fricklweb.server.database.tables.pojos.Albums> fetchByUpdatedOn(Timestamp... values)
	{
		return fetch(Albums.ALBUMS.UPDATED_ON, values);
	}
}
