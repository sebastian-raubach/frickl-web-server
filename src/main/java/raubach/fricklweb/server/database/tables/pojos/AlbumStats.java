/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.pojos;


import java.io.Serializable;
import java.sql.Timestamp;

import javax.annotation.Generated;


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
public class AlbumStats implements Serializable {

    private static final long serialVersionUID = 902564300;

    private Integer   id;
    private String    name;
    private String    description;
    private String    path;
    private Integer   bannerImageId;
    private Long      bannerImagePublicId;
    private Integer   parentAlbumId;
    private Timestamp createdOn;
    private Timestamp updatedOn;
    private Long      count;
    private Long      countPublic;

    public AlbumStats() {}

    public AlbumStats(AlbumStats value) {
        this.id = value.id;
        this.name = value.name;
        this.description = value.description;
        this.path = value.path;
        this.bannerImageId = value.bannerImageId;
        this.bannerImagePublicId = value.bannerImagePublicId;
        this.parentAlbumId = value.parentAlbumId;
        this.createdOn = value.createdOn;
        this.updatedOn = value.updatedOn;
        this.count = value.count;
        this.countPublic = value.countPublic;
    }

    public AlbumStats(
        Integer   id,
        String    name,
        String    description,
        String    path,
        Integer   bannerImageId,
        Long      bannerImagePublicId,
        Integer   parentAlbumId,
        Timestamp createdOn,
        Timestamp updatedOn,
        Long      count,
        Long      countPublic
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.path = path;
        this.bannerImageId = bannerImageId;
        this.bannerImagePublicId = bannerImagePublicId;
        this.parentAlbumId = parentAlbumId;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.count = count;
        this.countPublic = countPublic;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getBannerImageId() {
        return this.bannerImageId;
    }

    public void setBannerImageId(Integer bannerImageId) {
        this.bannerImageId = bannerImageId;
    }

    public Long getBannerImagePublicId() {
        return this.bannerImagePublicId;
    }

    public void setBannerImagePublicId(Long bannerImagePublicId) {
        this.bannerImagePublicId = bannerImagePublicId;
    }

    public Integer getParentAlbumId() {
        return this.parentAlbumId;
    }

    public void setParentAlbumId(Integer parentAlbumId) {
        this.parentAlbumId = parentAlbumId;
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

    public Long getCount() {
        return this.count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getCountPublic() {
        return this.countPublic;
    }

    public void setCountPublic(Long countPublic) {
        this.countPublic = countPublic;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AlbumStats (");

        sb.append(id);
        sb.append(", ").append(name);
        sb.append(", ").append(description);
        sb.append(", ").append(path);
        sb.append(", ").append(bannerImageId);
        sb.append(", ").append(bannerImagePublicId);
        sb.append(", ").append(parentAlbumId);
        sb.append(", ").append(createdOn);
        sb.append(", ").append(updatedOn);
        sb.append(", ").append(count);
        sb.append(", ").append(countPublic);

        sb.append(")");
        return sb.toString();
    }
}
