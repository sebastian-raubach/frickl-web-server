/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.pojos;


import java.io.Serializable;
import java.math.BigDecimal;

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
public class LatLngs implements Serializable {

    private static final long serialVersionUID = -1126761349;

    private Integer    id;
    private String     path;
    private Integer    albumId;
    private Byte       isPublic;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public LatLngs() {}

    public LatLngs(LatLngs value) {
        this.id = value.id;
        this.path = value.path;
        this.albumId = value.albumId;
        this.isPublic = value.isPublic;
        this.latitude = value.latitude;
        this.longitude = value.longitude;
    }

    public LatLngs(
        Integer    id,
        String     path,
        Integer    albumId,
        Byte       isPublic,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        this.id = id;
        this.path = path;
        this.albumId = albumId;
        this.isPublic = isPublic;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public BigDecimal getLatitude() {
        return this.latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return this.longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LatLngs (");

        sb.append(id);
        sb.append(", ").append(path);
        sb.append(", ").append(albumId);
        sb.append(", ").append(isPublic);
        sb.append(", ").append(latitude);
        sb.append(", ").append(longitude);

        sb.append(")");
        return sb.toString();
    }
}
