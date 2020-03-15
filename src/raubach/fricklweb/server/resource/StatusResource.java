package raubach.fricklweb.server.resource;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import raubach.fricklweb.server.computed.Status;
import raubach.fricklweb.server.scanner.ImageScanner;

/**
 * @author Sebastian Raubach
 */
public class StatusResource extends ServerResource
{
	@Get("json")
	public Status getJson()
	{
		return ImageScanner.STATUS;
	}
}
