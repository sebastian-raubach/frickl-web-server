package raubach.fricklweb.server.computed;

import java.sql.Timestamp;

public class AccessToken
{
	private String    token;
	private Timestamp expiresOn;

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	public Timestamp getExpiresOn()
	{
		return expiresOn;
	}

	public void setExpiresOn(Timestamp expiresOn)
	{
		this.expiresOn = expiresOn;
	}

	@Override
	public String toString()
	{
		return "AccessToken{" +
			"token='" + token + '\'' +
			", expiresOn=" + expiresOn +
			'}';
	}
}
