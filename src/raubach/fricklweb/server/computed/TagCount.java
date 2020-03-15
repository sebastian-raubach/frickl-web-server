package raubach.fricklweb.server.computed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import raubach.fricklweb.server.database.tables.pojos.Tags;

/**
 * @author Sebastian Raubach
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagCount
{
	private Tags tag;
	private Integer count;

	public TagCount()
	{
	}

	public TagCount(Tags tag, Integer count)
	{
		this.tag = tag;
		this.count = count;
	}

	public Tags getTag()
	{
		return tag;
	}

	public TagCount setTag(Tags tag)
	{
		this.tag = tag;
		return this;
	}

	public Integer getCount()
	{
		return count;
	}

	public TagCount setCount(Integer count)
	{
		this.count = count;
		return this;
	}
}
