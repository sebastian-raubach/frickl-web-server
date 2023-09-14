package raubach.frickl.next.pojo;

/**
 * @author Sebastian Raubach
 */
public class PaginatedRequest
{
	private String  orderBy;
	private Integer ascending;
	private int     limit     = Integer.MAX_VALUE;
	private int     page      = 0;
	private long    prevCount = -1;
	private String  searchTerm;

	public PaginatedRequest()
	{
	}

	public String getOrderBy()
	{
		return orderBy;
	}

	public PaginatedRequest setOrderBy(String orderBy)
	{
		this.orderBy = orderBy;
		return this;
	}

	public Integer getAscending()
	{
		return ascending;
	}

	public PaginatedRequest setAscending(Integer ascending)
	{
		this.ascending = ascending;
		return this;
	}

	public int getLimit()
	{
		return limit;
	}

	public PaginatedRequest setLimit(int limit)
	{
		this.limit = limit;
		return this;
	}

	public int getPage()
	{
		return page;
	}

	public PaginatedRequest setPage(int page)
	{
		this.page = page;
		return this;
	}

	public long getPrevCount()
	{
		return prevCount;
	}

	public PaginatedRequest setPrevCount(long prevCount)
	{
		this.prevCount = prevCount;
		return this;
	}

	public String getSearchTerm()
	{
		return searchTerm;
	}

	public PaginatedRequest setSearchTerm(String searchTerm)
	{
		this.searchTerm = searchTerm;
		return this;
	}

	@Override
	public String toString()
	{
		return "PaginatedRequest{" +
				"orderBy='" + orderBy + '\'' +
				", ascending=" + ascending +
				", limit=" + limit +
				", page=" + page +
				", prevCount=" + prevCount +
				", searchTerm=" + searchTerm +
				'}';
	}
}
