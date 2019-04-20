package raubach.fricklweb.server;

import org.restlet.resource.*;

import raubach.fricklweb.server.computed.Status;
import raubach.fricklweb.server.scanner.*;

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
