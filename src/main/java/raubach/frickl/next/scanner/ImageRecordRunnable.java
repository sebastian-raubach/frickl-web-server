package raubach.frickl.next.scanner;

import raubach.frickl.next.codegen.tables.records.ImagesRecord;

import java.util.concurrent.Callable;

public abstract class ImageRecordRunnable<T> implements Callable<T>
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

	@Override
	public String toString()
	{
		return "ImageRecordRunnable{" +
			"image=" + image +
			'}';
	}
}
