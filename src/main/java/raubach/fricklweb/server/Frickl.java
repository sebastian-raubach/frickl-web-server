package raubach.fricklweb.server;

import org.glassfish.jersey.server.ResourceConfig;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ApplicationPath;

/**
 * @author Sebastian Raubach
 */
@ApplicationPath("/api/")
public class Frickl extends ResourceConfig
{
	public static String BASE_PATH;
	public static Frickl INSTANCE;

	public Frickl()
	{
		packages("raubach.fricklweb.server");

		INSTANCE = this;
	}

	public static String getServerBase(HttpServletRequest request, boolean includeContext)
	{
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String contextPath = request.getContextPath();

		if (serverPort == 80 || serverPort == 443)
			return scheme + "://" + serverName + (includeContext ? contextPath : "");
		else
			return scheme + "://" + serverName + ":" + serverPort + (includeContext ? contextPath : "");
	}
}
