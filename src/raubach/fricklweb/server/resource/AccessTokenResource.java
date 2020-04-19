package raubach.fricklweb.server.resource;

import org.restlet.resource.ResourceException;

public class AccessTokenResource extends PaginatedServerResource
{
	public static final String PARAM_ACCESS_TOKEN = "accesstoken";

	protected String accessToken = null;

	@Override
	protected void doInit() throws ResourceException
	{
		super.doInit();

		this.accessToken = getQueryValue(PARAM_ACCESS_TOKEN);

	}
}
