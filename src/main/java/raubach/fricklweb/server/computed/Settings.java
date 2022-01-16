package raubach.fricklweb.server.computed;

public class Settings
{
	private boolean authEnabled;
	private String  googleAnalyticsKey;

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
}
