package raubach.fricklweb.server.scanner;

import org.restlet.data.*;

import java.io.*;

import javax.servlet.*;

import raubach.fricklweb.server.*;
import raubach.fricklweb.server.database.tables.records.*;
import raubach.fricklweb.server.util.*;

/**
 * @author Sebastian Raubach
 */
public class ImageScaler implements Runnable
{
	private ServletContext context;
	private ImagesRecord   image;

	public ImageScaler(ServletContext context, ImagesRecord image)
	{
		this.context = context;
		this.image = image;
	}

	@Override
	public void run()
	{
		File file = new File(Frickl.BASE_PATH, image.getPath());

		// Check if the image exists
		try
		{
			MediaType type;

			if (file.getName().toLowerCase().endsWith(".jpg"))
				type = MediaType.IMAGE_JPEG;
			else if (file.getName().toLowerCase().endsWith(".png"))
				type = MediaType.IMAGE_PNG;
			else
				type = MediaType.IMAGE_ALL;

			ThumbnailUtils.getOrCreateThumbnail(context, type, image.getId(), file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
