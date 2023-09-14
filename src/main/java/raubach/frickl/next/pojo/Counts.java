package raubach.frickl.next.pojo;

public class Counts
{
	private int images = 0;
	private int albums = 0;
	private int favorites = 0;
	private int tags = 0;

	public int getImages()
	{
		return images;
	}

	public Counts setImages(int images)
	{
		this.images = images;
		return this;
	}

	public int getAlbums()
	{
		return albums;
	}

	public Counts setAlbums(int albums)
	{
		this.albums = albums;
		return this;
	}

	public int getFavorites()
	{
		return favorites;
	}

	public Counts setFavorites(int favorites)
	{
		this.favorites = favorites;
		return this;
	}

	public int getTags()
	{
		return tags;
	}

	public Counts setTags(int tags)
	{
		this.tags = tags;
		return this;
	}
}
