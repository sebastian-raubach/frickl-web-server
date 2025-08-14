package raubach.frickl.next.pojo;

public class ImageRequest extends PaginatedRequest
{
	public Integer imageId;
	public Integer albumId;
	public Boolean isFav;
	public String  date;
	public Integer tagId;

	public Integer getImageId()
	{
		return imageId;
	}

	public ImageRequest setImageId(Integer imageId)
	{
		this.imageId = imageId;
		return this;
	}

	public Integer getAlbumId()
	{
		return albumId;
	}

	public ImageRequest setAlbumId(Integer albumId)
	{
		this.albumId = albumId;
		return this;
	}

	public Boolean getIsFav()
	{
		return isFav;
	}

	public ImageRequest setIsFav(Boolean isFav)
	{
		this.isFav = isFav;
		return this;
	}

	public String getDate()
	{
		return date;
	}

	public ImageRequest setDate(String date)
	{
		this.date = date;
		return this;
	}

	public Boolean getFav()
	{
		return isFav;
	}

	public ImageRequest setFav(Boolean fav)
	{
		isFav = fav;
		return this;
	}

	public Integer getTagId()
	{
		return tagId;
	}

	public ImageRequest setTagId(Integer tagId)
	{
		this.tagId = tagId;
		return this;
	}
}
