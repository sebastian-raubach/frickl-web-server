package raubach.fricklweb.server.resource;

import raubach.fricklweb.server.computed.DataScanResult;
import raubach.fricklweb.server.scanner.ImageScanner;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * @author Sebastian Raubach
 */
@Path("status")
public class StatusResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public DataScanResult getJson()
	{
		return ImageScanner.SCANRESULT;
	}
}
