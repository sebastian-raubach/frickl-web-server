package raubach.fricklweb.server.resource;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import raubach.fricklweb.server.computed.Settings;
import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

public class SettingsResource extends ServerResource
{
	@Get("json")
	public Settings getJson()
	{
		Settings settings = new Settings();
		settings.setAuthEnabled(PropertyWatcher.authEnabled());
		settings.setGoogleAnalyticsKey(PropertyWatcher.get(ServerProperty.GOOGLE_ANALYTICS_KEY));
		return settings;
	}
}
