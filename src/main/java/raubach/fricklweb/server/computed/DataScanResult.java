package raubach.fricklweb.server.computed;

public class DataScanResult
{
	private Status status      = Status.IDLE;
	private int    totalImages = 0;
	private int    queueSize   = 0;

	public void reset()
	{
		status = Status.IDLE;
		totalImages = 0;
		queueSize = 0;
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public Integer getTotalImages()
	{
		return totalImages;
	}

	public void setTotalImages(int totalImages)
	{
		this.totalImages = totalImages;
	}

	public void incrementTotalImages()
	{
		this.totalImages++;
	}

	public Integer getQueueSize()
	{
		return queueSize;
	}

	public void setQueueSize(int queueSize)
	{
		this.queueSize = queueSize;
	}
}
