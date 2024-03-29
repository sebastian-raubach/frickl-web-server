package raubach.fricklweb.server.resource;

import jakarta.ws.rs.core.*;
import raubach.fricklweb.server.computed.Settings;
import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import jakarta.ws.rs.*;

@Path("settings")
public class SettingsResource extends ContextResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSettings()
	{
		Settings settings = new Settings();
		settings.setAuthEnabled(PropertyWatcher.authEnabled());
		settings.setGoogleAnalyticsKey(PropertyWatcher.get(ServerProperty.GOOGLE_ANALYTICS_KEY));
		settings.setPlausibleApiHost(PropertyWatcher.get(ServerProperty.PLAUSIBLE_API_HOST));
		settings.setPlausibleHashMode(PropertyWatcher.getBoolean(ServerProperty.PLAUSIBLE_HASH_MODE));
		settings.setPlausibleDomain(PropertyWatcher.get(ServerProperty.PLAUSIBLE_DOMAIN));

		return Response.ok(settings).build();
	}
}
