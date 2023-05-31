/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.pojos;


import java.io.Serializable;
import java.sql.Timestamp;


/**
 * VIEW
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AlbumStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer   id;
    private String    name;
    private String    description;
    private String    path;
    private Integer   bannerImageId;
    private Long      bannerImagePublicId;
    private Integer   parentAlbumId;
    private Timestamp createdOn;
    private Timestamp updatedOn;
    private Integer   albumId;
    private Integer   imageCount;
    private Integer   imageCountPublic;
    private Integer   albumCount;
    private Integer   imageViewCount;
    private Timestamp newestImage;
    private Timestamp oldestImage;

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
        this.albumId = value.albumId;
        this.imageCount = value.imageCount;
        this.imageCountPublic = value.imageCountPublic;
        this.albumCount = value.albumCount;
        this.imageViewCount = value.imageViewCount;
        this.newestImage = value.newestImage;
        this.oldestImage = value.oldestImage;
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
        Integer   albumId,
        Integer   imageCount,
        Integer   imageCountPublic,
        Integer   albumCount,
        Integer   imageViewCount,
        Timestamp newestImage,
        Timestamp oldestImage
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
        this.albumId = albumId;
        this.imageCount = imageCount;
        this.imageCountPublic = imageCountPublic;
        this.albumCount = albumCount;
        this.imageViewCount = imageViewCount;
        this.newestImage = newestImage;
        this.oldestImage = oldestImage;
    }

    /**
     * Getter for <code>frickl.album_stats.id</code>. Auto incremented id of
     * this table.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Setter for <code>frickl.album_stats.id</code>. Auto incremented id of
     * this table.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Getter for <code>frickl.album_stats.name</code>. The name of the album.
     * Should ideally be relatively short.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>frickl.album_stats.name</code>. The name of the album.
     * Should ideally be relatively short.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for <code>frickl.album_stats.description</code>. Optional
     * description of the album.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for <code>frickl.album_stats.description</code>. Optional
     * description of the album.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for <code>frickl.album_stats.path</code>. The path to the album
     * relative to the base path of the setup.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Setter for <code>frickl.album_stats.path</code>. The path to the album
     * relative to the base path of the setup.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Getter for <code>frickl.album_stats.banner_image_id</code>. Optional
     * banner image id. This image will be shown to visually represent this
     * album.
     */
    public Integer getBannerImageId() {
        return this.bannerImageId;
    }

    /**
     * Setter for <code>frickl.album_stats.banner_image_id</code>. Optional
     * banner image id. This image will be shown to visually represent this
     * album.
     */
    public void setBannerImageId(Integer bannerImageId) {
        this.bannerImageId = bannerImageId;
    }

    /**
     * Getter for <code>frickl.album_stats.banner_image_public_id</code>.
     */
    public Long getBannerImagePublicId() {
        return this.bannerImagePublicId;
    }

    /**
     * Setter for <code>frickl.album_stats.banner_image_public_id</code>.
     */
    public void setBannerImagePublicId(Long bannerImagePublicId) {
        this.bannerImagePublicId = bannerImagePublicId;
    }

    /**
     * Getter for <code>frickl.album_stats.parent_album_id</code>. Optional
     * parent album id. If this album is a sub-album of another album, this
     * parent album can be defined here.
     */
    public Integer getParentAlbumId() {
        return this.parentAlbumId;
    }

    /**
     * Setter for <code>frickl.album_stats.parent_album_id</code>. Optional
     * parent album id. If this album is a sub-album of another album, this
     * parent album can be defined here.
     */
    public void setParentAlbumId(Integer parentAlbumId) {
        this.parentAlbumId = parentAlbumId;
    }

    /**
     * Getter for <code>frickl.album_stats.created_on</code>. When this record
     * has been created.
     */
    public Timestamp getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Setter for <code>frickl.album_stats.created_on</code>. When this record
     * has been created.
     */
    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Getter for <code>frickl.album_stats.updated_on</code>. When this record
     * has last been updated.
     */
    public Timestamp getUpdatedOn() {
        return this.updatedOn;
    }

    /**
     * Setter for <code>frickl.album_stats.updated_on</code>. When this record
     * has last been updated.
     */
    public void setUpdatedOn(Timestamp updatedOn) {
        this.updatedOn = updatedOn;
    }

    /**
     * Getter for <code>frickl.album_stats.album_id</code>.
     */
    public Integer getAlbumId() {
        return this.albumId;
    }

    /**
     * Setter for <code>frickl.album_stats.album_id</code>.
     */
    public void setAlbumId(Integer albumId) {
        this.albumId = albumId;
    }

    /**
     * Getter for <code>frickl.album_stats.image_count</code>.
     */
    public Integer getImageCount() {
        return this.imageCount;
    }

    /**
     * Setter for <code>frickl.album_stats.image_count</code>.
     */
    public void setImageCount(Integer imageCount) {
        this.imageCount = imageCount;
    }

    /**
     * Getter for <code>frickl.album_stats.image_count_public</code>.
     */
    public Integer getImageCountPublic() {
        return this.imageCountPublic;
    }

    /**
     * Setter for <code>frickl.album_stats.image_count_public</code>.
     */
    public void setImageCountPublic(Integer imageCountPublic) {
        this.imageCountPublic = imageCountPublic;
    }

    /**
     * Getter for <code>frickl.album_stats.album_count</code>.
     */
    public Integer getAlbumCount() {
        return this.albumCount;
    }

    /**
     * Setter for <code>frickl.album_stats.album_count</code>.
     */
    public void setAlbumCount(Integer albumCount) {
        this.albumCount = albumCount;
    }

    /**
     * Getter for <code>frickl.album_stats.image_view_count</code>.
     */
    public Integer getImageViewCount() {
        return this.imageViewCount;
    }

    /**
     * Setter for <code>frickl.album_stats.image_view_count</code>.
     */
    public void setImageViewCount(Integer imageViewCount) {
        this.imageViewCount = imageViewCount;
    }

    /**
     * Getter for <code>frickl.album_stats.newest_image</code>.
     */
    public Timestamp getNewestImage() {
        return this.newestImage;
    }

    /**
     * Setter for <code>frickl.album_stats.newest_image</code>.
     */
    public void setNewestImage(Timestamp newestImage) {
        this.newestImage = newestImage;
    }

    /**
     * Getter for <code>frickl.album_stats.oldest_image</code>.
     */
    public Timestamp getOldestImage() {
        return this.oldestImage;
    }

    /**
     * Setter for <code>frickl.album_stats.oldest_image</code>.
     */
    public void setOldestImage(Timestamp oldestImage) {
        this.oldestImage = oldestImage;
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
        sb.append(", ").append(albumId);
        sb.append(", ").append(imageCount);
        sb.append(", ").append(imageCountPublic);
        sb.append(", ").append(albumCount);
        sb.append(", ").append(imageViewCount);
        sb.append(", ").append(newestImage);
        sb.append(", ").append(oldestImage);

        sb.append(")");
        return sb.toString();
    }
}
