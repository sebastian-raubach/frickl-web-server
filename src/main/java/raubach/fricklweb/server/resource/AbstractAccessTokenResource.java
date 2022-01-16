package raubach.fricklweb.server.resource;

import javax.ws.rs.QueryParam;

public abstract class AbstractAccessTokenResource extends PaginatedServerResource
{
	@QueryParam("accesstoken")
	protected String accessToken = null;
}
