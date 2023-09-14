package raubach.frickl.next.pojo;

public class AlbumRequest extends PaginatedRequest
{
	private Integer parentAlbumId;

	public Integer getParentAlbumId()
	{
		return parentAlbumId;
	}

	public AlbumRequest setParentAlbumId(Integer parentAlbumId)
	{
		this.parentAlbumId = parentAlbumId;
		return this;
	}
}
