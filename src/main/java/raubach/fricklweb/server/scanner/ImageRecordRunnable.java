package raubach.fricklweb.server.scanner;

import raubach.fricklweb.server.database.tables.records.ImagesRecord;

public abstract class ImageRecordRunnable implements Runnable
{
	protected ImagesRecord image;

	public ImageRecordRunnable(ImagesRecord image)
	{
		this.image = image;
	}

	public String getId()
	{
		return Integer.toString(image.getId());
	}
}
