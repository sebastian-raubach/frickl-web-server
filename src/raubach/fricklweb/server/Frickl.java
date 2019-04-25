package raubach.fricklweb.server;

import org.restlet.*;
import org.restlet.data.*;
import org.restlet.engine.application.*;
import org.restlet.resource.*;
import org.restlet.routing.*;
import org.restlet.service.*;
import org.restlet.util.*;

import java.util.*;

/**
 * @author Sebastian Raubach
 */
public class Frickl extends Application
{
	public static String BASE_PATH;

	public Frickl()
	{
		// Set information about API
		setName("Frickl Server");
		setDescription("This is the server implementation of Frickl");
		setOwner("Sebastian Raubach");
		setAuthor("Sebastian Raubach <sebastian@raubach.co.uk>");
	}

	private void attachToRouter(Router router, String url, Class<? extends ServerResource> clazz)
	{
		router.attach(url, clazz);
		router.attach(url + "/", clazz);
	}

	@Override
	public Restlet createInboundRoot()
	{
		Context context = getContext();

		// Set the encoder
		Filter encoder = new Encoder(context, false, true, new EncoderService(true));

		// Set the Cors filter
		CorsFilter corsFilter = new CorsFilter(context, encoder)
		{
			@Override
			protected int beforeHandle(Request request, Response response)
			{
				if (getCorsResponseHelper().isCorsRequest(request))
				{
					Series<Header> headers = request.getHeaders();

					for (Header header : headers)
					{
						if (header.getName().equalsIgnoreCase("origin"))
						{
							response.setAccessControlAllowOrigin(header.getValue());
						}
					}
				}
				return super.beforeHandle(request, response);
			}
		};
		corsFilter.setAllowedOrigins(new HashSet<>(Collections.singletonList("*")));
		corsFilter.setSkippingResourceForCorsOptions(true);
		corsFilter.setAllowingAllRequestedHeaders(true);
		corsFilter.setDefaultAllowedMethods(new HashSet<>(Arrays.asList(Method.POST, Method.GET, Method.PUT, Method.PATCH, Method.DELETE, Method.OPTIONS)));
		corsFilter.setAllowedCredentials(true);

		// Create new router
		Router router = new Router(context);
		// Attach the url handlers
		attachToRouter(router, "/album", AlbumResource.class);
		attachToRouter(router, "/album/count", AlbumCountResource.class);
		attachToRouter(router, "/album/{albumId}", AlbumResource.class);
		attachToRouter(router, "/album/{albumId}/count", AlbumCountResource.class);
		attachToRouter(router, "/album/{albumId}/location", AlbumLocationResource.class);
		attachToRouter(router, "/album/{albumId}/image", ImageResource.class);
		attachToRouter(router, "/album/{albumId}/image/count", ImageCountResource.class);
		attachToRouter(router, "/album/{albumId}/tag", AlbumTagResource.class);
		attachToRouter(router, "/calendar", CalendarResource.class);
		attachToRouter(router, "/calendar/year", CalendarYearResource.class);
		attachToRouter(router, "/image", ImageResource.class);
		attachToRouter(router, "/image/count", ImageCountResource.class);
		attachToRouter(router, "/image/fav/random", ImageRandomResource.class);
		attachToRouter(router, "/image/{imageId}", ImageResource.class);
		attachToRouter(router, "/image/{imageId}/fav", ImageFavResource.class);
		attachToRouter(router, "/image/{imageId}/tag", ImageTagResource.class);
		attachToRouter(router, "/image/{imageId}/img", ImageImageResource.class);
		attachToRouter(router, "/stats/camera", StatsCameraResource.class);
		attachToRouter(router, "/tag", TagResource.class);
		attachToRouter(router, "/tag/{tagId}", TagResource.class);
		attachToRouter(router, "/tag/{tagId}/image", TagImageResource.class);
		attachToRouter(router, "/tag/{tagId}/image/count", TagImageCountResource.class);

		attachToRouter(router, "/status", StatusResource.class);

		// CORS first, then encoder
		corsFilter.setNext(encoder);
		// After that the unauthorized paths
		encoder.setNext(router);

		return corsFilter;
	}
}
