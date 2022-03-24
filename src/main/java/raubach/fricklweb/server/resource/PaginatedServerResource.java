package raubach.fricklweb.server.resource;

import jakarta.ws.rs.*;

/**
 * @author Sebastian Raubach
 */
public class PaginatedServerResource extends ContextResource
{
	@DefaultValue("0")
	@QueryParam("page")
	protected int currentPage;

	@DefaultValue("2147483647")
	@QueryParam("limit")
	protected int pageSize;

//	protected String getRequestAttributeAsString(String parameter)
//	{
//		try
//		{
//			return URLDecoder.decode(getRequestAttributes().get(parameter).toString(), StandardCharsets.UTF_8.name());
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//			return null;
//		}
//	}
}
