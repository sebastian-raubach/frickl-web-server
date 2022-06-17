package raubach.fricklweb.server.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import raubach.fricklweb.server.scanner.ImageScanner;

/**
 * @author Sebastian Raubach
 */
@Path("status")
public class StatusResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJson()
	{
		return Response.ok(ImageScanner.SCANRESULT).build();
	}
}
