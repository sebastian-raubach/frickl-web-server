/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.pojos;


import java.io.*;
import java.sql.*;

import javax.annotation.*;

import raubach.fricklweb.server.computed.*;


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
public class Images implements Serializable
{

    private static final long serialVersionUID = -754146536;

    private Integer   id;
    private String    path;
    private Exif      exif;
    private Integer   albumId;
    private Timestamp createdOn;
    private Timestamp updatedOn;

	public Images()
	{
	}

	public Images(Images value)
	{
        this.id = value.id;
        this.path = value.path;
        this.exif = value.exif;
        this.albumId = value.albumId;
        this.createdOn = value.createdOn;
        this.updatedOn = value.updatedOn;
    }

    public Images(
		Integer id,
		String path,
		Exif exif,
		Integer albumId,
		Timestamp createdOn,
		Timestamp updatedOn
	)
	{
        this.id = id;
        this.path = path;
        this.exif = exif;
        this.albumId = albumId;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

	public Integer getId()
	{
        return this.id;
    }

	public void setId(Integer id)
	{
        this.id = id;
    }

	public String getPath()
	{
        return this.path;
    }

	public void setPath(String path)
	{
        this.path = path;
    }

	public Exif getExif()
	{
        return this.exif;
    }

	public void setExif(Exif exif)
	{
        this.exif = exif;
    }

	public Integer getAlbumId()
	{
        return this.albumId;
    }

	public void setAlbumId(Integer albumId)
	{
        this.albumId = albumId;
    }

	public Timestamp getCreatedOn()
	{
        return this.createdOn;
    }

	public void setCreatedOn(Timestamp createdOn)
	{
        this.createdOn = createdOn;
    }

	public Timestamp getUpdatedOn()
	{
        return this.updatedOn;
    }

	public void setUpdatedOn(Timestamp updatedOn)
	{
        this.updatedOn = updatedOn;
    }

    @Override
	public String toString()
	{
        StringBuilder sb = new StringBuilder("Images (");

        sb.append(id);
        sb.append(", ").append(path);
        sb.append(", ").append(exif);
        sb.append(", ").append(albumId);
        sb.append(", ").append(createdOn);
        sb.append(", ").append(updatedOn);

        sb.append(")");
        return sb.toString();
    }
}