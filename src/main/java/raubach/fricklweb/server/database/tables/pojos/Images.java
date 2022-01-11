/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.pojos;


import java.io.Serializable;
import java.sql.Timestamp;

import javax.annotation.Generated;

import raubach.fricklweb.server.computed.Exif;
import raubach.fricklweb.server.database.enums.ImagesDataType;


/**
 * This table contains images from the file system.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Images implements Serializable {

    private static final long serialVersionUID = -1408304963;

    private Integer        id;
    private String         path;
    private String         name;
    private Byte           isFavorite;
    private Exif           exif;
    private Integer        albumId;
    private Byte           isPublic;
    private ImagesDataType dataType;
    private Timestamp      createdOn;
    private Timestamp      updatedOn;

    public Images() {}

    public Images(Images value) {
        this.id = value.id;
        this.path = value.path;
        this.name = value.name;
        this.isFavorite = value.isFavorite;
        this.exif = value.exif;
        this.albumId = value.albumId;
        this.isPublic = value.isPublic;
        this.dataType = value.dataType;
        this.createdOn = value.createdOn;
        this.updatedOn = value.updatedOn;
    }

    public Images(
        Integer        id,
        String         path,
        String         name,
        Byte           isFavorite,
        Exif           exif,
        Integer        albumId,
        Byte           isPublic,
        ImagesDataType dataType,
        Timestamp      createdOn,
        Timestamp      updatedOn
    ) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.isFavorite = isFavorite;
        this.exif = exif;
        this.albumId = albumId;
        this.isPublic = isPublic;
        this.dataType = dataType;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getIsFavorite() {
        return this.isFavorite;
    }

    public void setIsFavorite(Byte isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Exif getExif() {
        return this.exif;
    }

    public void setExif(Exif exif) {
        this.exif = exif;
    }

    public Integer getAlbumId() {
        return this.albumId;
    }

    public void setAlbumId(Integer albumId) {
        this.albumId = albumId;
    }

    public Byte getIsPublic() {
        return this.isPublic;
    }

    public void setIsPublic(Byte isPublic) {
        this.isPublic = isPublic;
    }

    public ImagesDataType getDataType() {
        return this.dataType;
    }

    public void setDataType(ImagesDataType dataType) {
        this.dataType = dataType;
    }

    public Timestamp getCreatedOn() {
        return this.createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    public Timestamp getUpdatedOn() {
        return this.updatedOn;
    }

    public void setUpdatedOn(Timestamp updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Images (");

        sb.append(id);
        sb.append(", ").append(path);
        sb.append(", ").append(name);
        sb.append(", ").append(isFavorite);
        sb.append(", ").append(exif);
        sb.append(", ").append(albumId);
        sb.append(", ").append(isPublic);
        sb.append(", ").append(dataType);
        sb.append(", ").append(createdOn);
        sb.append(", ").append(updatedOn);

        sb.append(")");
        return sb.toString();
    }
}