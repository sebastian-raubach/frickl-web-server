package raubach.frickl.next.pojo;

import raubach.frickl.next.codegen.tables.pojos.AlbumStats;

import java.sql.Timestamp;

public class Album
{
	private Integer   id;
	private String    name;
	private String    description;
	private String    path;
	private Long      bannerImageId;
	private Integer   parentAlbumId;
	private Timestamp createdOn;
	private Timestamp updatedOn;
	private Integer   albumId;
	private Integer   imageCount;
	private Integer   albumCount;
	private Integer   imageViewCount;
	private Timestamp newestImage;
	private Timestamp oldestImage;

	public Album(AlbumStats original, boolean onlyPublic)
	{
		this.id = original.getId();
		this.name = original.getName();
		this.description = original.getDescription();
		this.path = original.getPath();
		this.bannerImageId = onlyPublic ? original.getBannerImagePublicId() : (original.getBannerImageId() != null ? Long.valueOf(original.getBannerImageId()) : null);
		this.parentAlbumId = original.getParentAlbumId();
		this.createdOn = original.getCreatedOn();
		this.updatedOn = original.getUpdatedOn();
		this.albumId = original.getAlbumId();
		this.imageCount = onlyPublic ? original.getImageCountPublic() : original.getImageCount();
		this.albumCount = original.getAlbumCount();
		this.imageViewCount = original.getImageViewCount();
		this.newestImage = original.getNewestImage();
		this.oldestImage = original.getOldestImage();
	}

	public Integer getId()
	{
		return id;
	}

	public Album setId(Integer id)
	{
		this.id = id;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Album setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public Album setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public String getPath()
	{
		return path;
	}

	public Album setPath(String path)
	{
		this.path = path;
		return this;
	}

	public Long getBannerImageId()
	{
		return bannerImageId;
	}

	public Album setBannerImageId(Long bannerImageId)
	{
		this.bannerImageId = bannerImageId;
		return this;
	}

	public Integer getParentAlbumId()
	{
		return parentAlbumId;
	}

	public Album setParentAlbumId(Integer parentAlbumId)
	{
		this.parentAlbumId = parentAlbumId;
		return this;
	}

	public Timestamp getCreatedOn()
	{
		return createdOn;
	}

	public Album setCreatedOn(Timestamp createdOn)
	{
		this.createdOn = createdOn;
		return this;
	}

	public Timestamp getUpdatedOn()
	{
		return updatedOn;
	}

	public Album setUpdatedOn(Timestamp updatedOn)
	{
		this.updatedOn = updatedOn;
		return this;
	}

	public Integer getAlbumId()
	{
		return albumId;
	}

	public Album setAlbumId(Integer albumId)
	{
		this.albumId = albumId;
		return this;
	}

	public Integer getImageCount()
	{
		return imageCount;
	}

	public Album setImageCount(Integer imageCount)
	{
		this.imageCount = imageCount;
		return this;
	}

	public Integer getAlbumCount()
	{
		return albumCount;
	}

	public Album setAlbumCount(Integer albumCount)
	{
		this.albumCount = albumCount;
		return this;
	}

	public Integer getImageViewCount()
	{
		return imageViewCount;
	}

	public Album setImageViewCount(Integer imageViewCount)
	{
		this.imageViewCount = imageViewCount;
		return this;
	}

	public Timestamp getNewestImage()
	{
		return newestImage;
	}

	public Album setNewestImage(Timestamp newestImage)
	{
		this.newestImage = newestImage;
		return this;
	}

	public Timestamp getOldestImage()
	{
		return oldestImage;
	}

	public Album setOldestImage(Timestamp oldestImage)
	{
		this.oldestImage = oldestImage;
		return this;
	}
}
