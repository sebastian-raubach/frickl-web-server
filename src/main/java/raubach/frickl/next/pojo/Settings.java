package raubach.frickl.next.pojo;

public class Settings
{
	private boolean authEnabled;
	private String  googleAnalyticsKey;
	private String             plausibleDomain;
	private Boolean            plausibleHashMode;
	private String             plausibleApiHost;

	public Settings()
	{
	}

	public boolean isAuthEnabled()
	{
		return authEnabled;
	}

	public void setAuthEnabled(boolean authEnabled)
	{
		this.authEnabled = authEnabled;
	}

	public String getGoogleAnalyticsKey()
	{
		return googleAnalyticsKey;
	}

	public void setGoogleAnalyticsKey(String googleAnalyticsKey)
	{
		this.googleAnalyticsKey = googleAnalyticsKey;
	}

	public String getPlausibleDomain()
	{
		return plausibleDomain;
	}

	public Settings setPlausibleDomain(String plausibleDomain)
	{
		this.plausibleDomain = plausibleDomain;
		return this;
	}

	public Boolean getPlausibleHashMode()
	{
		return plausibleHashMode;
	}

	public Settings setPlausibleHashMode(Boolean plausibleHashMode)
	{
		this.plausibleHashMode = plausibleHashMode;
		return this;
	}

	public String getPlausibleApiHost()
	{
		return plausibleApiHost;
	}

	public Settings setPlausibleApiHost(String plausibleApiHost)
	{
		this.plausibleApiHost = plausibleApiHost;
		return this;
	}
}
