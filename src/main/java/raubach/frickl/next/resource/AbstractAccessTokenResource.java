package raubach.frickl.next.resource;

import jakarta.ws.rs.QueryParam;

public abstract class AbstractAccessTokenResource extends PaginatedServerResource
{
	@QueryParam("accesstoken")
	protected String accessToken = null;
}
