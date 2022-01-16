package raubach.fricklweb.server.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.*;
import javax.ws.rs.core.Response;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileUploadHandler
{
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	public static synchronized List<String> handleMultiple(HttpServletRequest req, HttpServletResponse resp, String formIdentifier, File folder)
		throws IOException
	{
		//checks whether there is a file upload request or not
		if (ServletFileUpload.isMultipartContent(req))
		{
			final ServletFileUpload fileUpload = new ServletFileUpload(new DiskFileItemFactory());
			try
			{
				List<String> filenames = new ArrayList<>();

				final List<FileItem> items = fileUpload.parseRequest(req);

				if (!CollectionUtils.isEmpty(items))
				{
					for (FileItem item : items)
					{
						final String itemName = item.getName();

						if (!item.isFormField() && Objects.equals(item.getFieldName(), formIdentifier))
						{
							// consume the stream immediately, otherwise the stream
							// will be closed.
							String filename = item.getName();
							String name = filename.substring(0, filename.lastIndexOf("."));
							// Convert extension to lower case to prevent issues with uppercase videos and the player library
							String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase(Locale.ENGLISH);

							File target = new File(folder, name + "." + extension);

							// Make sure it's unique
							while (target.exists())
							{
								target = new File(folder, name + "-" + SDF.format(new Date()) + "." + extension);
							}

							item.write(target);
							filenames.add(target.getName());
						}
					}
				}

				return filenames;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				resp.sendError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
				return null;
			}
		}
		else
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}
	}
}
