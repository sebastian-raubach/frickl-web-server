package raubach.fricklweb.server.resource.download;


import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.resource.ContextResource;
import raubach.fricklweb.server.util.ResourceUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.nio.file.Files;

@Path("download/{filename}")
public class DownloadResource extends ContextResource
{
	@PathParam("filename")
	String filename;

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFile()
		throws IOException
	{
		if (StringUtils.isEmpty(filename))
		{
			resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
			return null;
		}

		File file = ResourceUtils.getTempFile("frickl-download", filename);

		java.nio.file.Path zipFilePath = file.toPath();
		return Response.ok((StreamingOutput) output -> {
						   Files.copy(zipFilePath, output);
						   Files.deleteIfExists(zipFilePath);
					   })
					   .type("application/zip")
					   .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename= \"" + file.getName() + "\"")
					   .header(HttpHeaders.CONTENT_LENGTH, file.length())
					   .build();
	}
}
