package raubach.fricklweb.server.computed;

public class StatsDayHour
{
	private short day;
	private short hour;
	private long count;

	public short getDay()
	{
		return day;
	}

	public StatsDayHour setDay(short day)
	{
		this.day = day;
		return this;
	}

	public short getHour()
	{
		return hour;
	}

	public StatsDayHour setHour(short hour)
	{
		this.hour = hour;
		return this;
	}

	public long getCount()
	{
		return count;
	}

	public StatsDayHour setCount(long count)
	{
		this.count = count;
		return this;
	}
}