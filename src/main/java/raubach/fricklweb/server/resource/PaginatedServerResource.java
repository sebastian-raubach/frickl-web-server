package raubach.fricklweb.server.resource;

import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import raubach.fricklweb.server.util.CollectionUtils;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Sebastian Raubach
 */
public class PaginatedServerResource extends ServerResource
{
	public static final String PARAM_PAGE = "page";
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

	protected String getRequestAttributeAsString(String parameter)
	{
		try
		{
			return URLDecoder.decode(getRequestAttributes().get(parameter).toString(), StandardCharsets.UTF_8.name());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	protected File getTempDir(String fileOrSubFolder) {
		List<String> segments = getReference().getSegments(true);
		String path;
		if (CollectionUtils.isEmpty(segments))
			path = "frickl";
		else
			path = segments.get(0);

		File folder = new File(System.getProperty("java.io.tmpdir"), path);
		folder.mkdirs();

		return new File(folder, fileOrSubFolder);
	}
}
