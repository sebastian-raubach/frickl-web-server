package raubach.fricklweb.server.resource;

import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import jakarta.servlet.http.*;
import jakarta.ws.rs.core.*;
import java.io.File;

public class ContextResource
{
	@Context
	protected SecurityContext     securityContext;
	@Context
	protected HttpServletRequest  req;
	@Context
	protected HttpServletResponse resp;

	protected File getTempFolder()
	{
		String version = PropertyWatcher.get(ServerProperty.API_VERSION);
		return new File(System.getProperty("java.io.tmpdir"), "frickl-thumbnails" + "-" + version);
	}
}
