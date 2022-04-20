package raubach.fricklweb.server.util;

/**
 * @author Sebastian Raubach
 */
public enum ServerProperty
{
	CONFIG_PATH("config.file", null, false),
	DATABASE_SERVER("database.server", null, true),
	DATABASE_NAME("database.name", null, true),
	DATABASE_USERNAME("database.username", null, true),
	DATABASE_PASSWORD("database.password", null, false),
	DATABASE_PORT("database.port", null, false),
	ADMIN_USERNAME("admin.username", null, false),
	ADMIN_PASSWORD("admin.password", null, false),
	GOOGLE_ANALYTICS_KEY("google.analytics.key", null, false),
	BASE_PATH("base.path", null, true),
	PUBLIC_URL("public.url", null, true),
	API_VERSION("api.version", "1", false);

	String  key;
	String  defaultValue;
	boolean required;

	ServerProperty(String key, String defaultValue, boolean required)
	{
		this.key = key;
		this.defaultValue = defaultValue;
		this.required = required;
	}

	public String getKey()
	{
		return key;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public boolean isRequired()
	{
		return required;
	}
}