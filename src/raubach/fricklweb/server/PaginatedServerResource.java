package raubach.fricklweb.server;

import org.restlet.resource.*;

/**
 * @author Sebastian Raubach
 */
public class PaginatedServerResource extends ServerResource
{
	public static final String PARAM_PAGE  = "page";
	public static final String PARAM_LIMIT = "limit";

	protected int currentPage;
	protected int pageSize;

	@Override
	protected void doInit()
		throws ResourceException
	{
		super.doInit();

		try
		{
			this.currentPage = Integer.parseInt(getQueryValue(PARAM_PAGE));
		}
		catch (NullPointerException | NumberFormatException e)
		{
			this.currentPage = 0;
		}
		try
		{
			this.pageSize = Integer.parseInt(getQueryValue(PARAM_LIMIT));
		}
		catch (NullPointerException | NumberFormatException e)
		{
			this.pageSize = Integer.MAX_VALUE;
		}
	}
}
