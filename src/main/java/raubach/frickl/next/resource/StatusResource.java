package raubach.frickl.next.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import raubach.frickl.next.scanner.ImageScanner;

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
