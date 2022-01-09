package raubach.fricklweb.server;

import org.restlet.*;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.engine.application.CorsFilter;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MethodAuthorizer;
import org.restlet.util.Series;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.resource.SettingsResource;
import raubach.fricklweb.server.resource.StatusResource;
import raubach.fricklweb.server.resource.TokenResource;
import raubach.fricklweb.server.resource.accesstoken.AccessTokenCountResource;
import raubach.fricklweb.server.resource.accesstoken.AccessTokenResource;
import raubach.fricklweb.server.resource.album.*;
import raubach.fricklweb.server.resource.calendar.CalendarResource;
import raubach.fricklweb.server.resource.calendar.CalendarYearResource;
import raubach.fricklweb.server.resource.image.*;
import raubach.fricklweb.server.resource.location.LocationResource;
import raubach.fricklweb.server.resource.search.SearchAlbumCountResource;
import raubach.fricklweb.server.resource.search.SearchAlbumResource;
import raubach.fricklweb.server.resource.search.SearchImageCountResource;
import raubach.fricklweb.server.resource.search.SearchImageResource;
import raubach.fricklweb.server.resource.stats.StatsCameraResource;
import raubach.fricklweb.server.resource.tag.TagImageCountResource;
import raubach.fricklweb.server.resource.tag.TagImageResource;
import raubach.fricklweb.server.resource.tag.TagResource;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Sebastian Raubach
 */
public class Frickl extends Application
{
	public static String BASE_PATH;
	public static Frickl INSTANCE;
	private static CustomVerifier verifier = new CustomVerifier();
	private ChallengeAuthenticator authenticator;
	private MethodAuthorizer authorizer;
	private Router routerAuth;
	private Router routerUnauth;

	public Frickl()
	{
		// Set information about API
		setName("Frickl Server");
		setDescription("This is the server implementation of Frickl");
		setOwner("Sebastian Raubach");
		setAuthor("Sebastian Raubach <sebastian@raubach.co.uk>");

		INSTANCE = this;
	}

	private void setUpAuthentication(Context context)
	{
		authorizer = new MethodAuthorizer();
		authorizer.getAuthenticatedMethods().add(Method.GET);
		authorizer.getAuthenticatedMethods().add(Method.OPTIONS);
		authorizer.getAuthenticatedMethods().add(Method.PATCH);
		authorizer.getAuthenticatedMethods().add(Method.POST);
		authorizer.getAuthenticatedMethods().add(Method.PUT);
		authorizer.getAuthenticatedMethods().add(Method.DELETE);

		authenticator = new ChallengeAuthenticator(context, true, ChallengeScheme.HTTP_OAUTH_BEARER, "Frickl", verifier);
	}


	private void attachToRouter(Router router, String url, Class<? extends ServerResource> clazz)
	{
		router.attach(url, clazz);
		router.attach(url + "/", clazz);
	}

	public static String getServerBase(HttpServletRequest request, boolean includeContext) {
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String contextPath = request.getContextPath();

		if (serverPort == 80 || serverPort == 443)
			return scheme + "://" + serverName + (includeContext ? contextPath : "");
		else
			return scheme + "://" + serverName + ":" + serverPort + (includeContext ? contextPath : "");
	}

	@Override
	public Restlet createInboundRoot()
	{
		Context context = getContext();

		setUpAuthentication(context);

		// Set the encoder
//		Filter encoder = new Encoder(context, false, true, new EncoderService(true));

		// Create new router
		routerAuth = new Router(context);
		routerUnauth = new Router(context);

		// Set the Cors filter
		CorsFilter corsFilter = new CorsFilter(context, routerUnauth)
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
		corsFilter.setExposedHeaders(Collections.singleton("Content-Disposition"));

		// Attach the url handlers
		attachToRouter(routerAuth, "/accesstoken", AccessTokenResource.class);
		attachToRouter(routerAuth, "/accesstoken/count", AccessTokenCountResource.class);
		attachToRouter(routerAuth, "/accesstoken/{tokenId}", AccessTokenResource.class);

		attachToRouter(routerAuth, "/album", AlbumResource.class);
		attachToRouter(routerAuth, "/album/count", AlbumCountResource.class);
		attachToRouter(routerAuth, "/album/{albumId}", AlbumResource.class);
		attachToRouter(routerAuth, "/album/{albumId}/count", AlbumCountResource.class);
		attachToRouter(routerAuth, "/album/{albumId}/download", AlbumDownloadResource.class);
		attachToRouter(routerAuth, "/album/{albumId}/location", AlbumLocationResource.class);
		attachToRouter(routerAuth, "/album/{albumId}/image", ImageResource.class);
		attachToRouter(routerAuth, "/album/{albumId}/image/count", ImageCountResource.class);
		attachToRouter(routerAuth, "/album/{albumId}/tag", AlbumTagResource.class);
		attachToRouter(routerAuth, "/album/{albumId}/public", AlbumPublicResource.class);
		attachToRouter(routerAuth, "/album/{albumId}/accesstoken", AlbumAccessTokenResource.class);

		attachToRouter(routerAuth, "/calendar", CalendarResource.class);
		attachToRouter(routerAuth, "/calendar/year", CalendarYearResource.class);

		attachToRouter(routerAuth, "/image", ImageResource.class);
		attachToRouter(routerAuth, "/image/count", ImageCountResource.class);
		attachToRouter(routerAuth, "/image/fav/random", ImageRandomResource.class);
		attachToRouter(routerAuth, "/image/xago", ImageXAgoResource.class);
		attachToRouter(routerAuth, "/image/{imageId}", ImageResource.class);
		attachToRouter(routerAuth, "/image/{imageId}/tag", ImageTagResource.class);
		attachToRouter(routerUnauth, "/image/{imageId}/img", ImageImageResource.class);
		attachToRouter(routerUnauth, "/image/{imageId}/video", ImageVideoResource.class);
		attachToRouter(routerUnauth, "/image/{imageId}/video/{filename}", ImageVideoResource.class);
		// For social media sharing. They don't seem to like image URLs without extension.
		attachToRouter(routerUnauth, "/image/{imageId}/share", ImageShareResource.class);

		attachToRouter(routerAuth, "/location", LocationResource.class);

		attachToRouter(routerAuth, "/search/{searchTerm}/album", SearchAlbumResource.class);
		attachToRouter(routerAuth, "/search/{searchTerm}/album/count", SearchAlbumCountResource.class);
		attachToRouter(routerAuth, "/search/{searchTerm}/image", SearchImageResource.class);
		attachToRouter(routerAuth, "/search/{searchTerm}/image/count", SearchImageCountResource.class);

		attachToRouter(routerAuth, "/stats/camera", StatsCameraResource.class);

		attachToRouter(routerAuth, "/tag", TagResource.class);
		attachToRouter(routerAuth, "/tag/{tagId}", TagResource.class);
		attachToRouter(routerAuth, "/tag/{tagId}/image", TagImageResource.class);
		attachToRouter(routerAuth, "/tag/{tagId}/image/count", TagImageCountResource.class);

		attachToRouter(routerAuth, "/status", StatusResource.class);

		attachToRouter(routerUnauth, "/token", TokenResource.class);
		attachToRouter(routerUnauth, "/settings", SettingsResource.class);

		// CORS first, then encoder
		corsFilter.setNext(routerUnauth);
		// After that the unauthorized paths
//		encoder.setNext(routerUnauth);
		// Set everything that isn't covered to go through the authenticator
		routerUnauth.attachDefault(authenticator);
		authenticator.setNext(authorizer);
		// And finally it ends up at the authenticated router
		authorizer.setNext(routerAuth);

		return corsFilter;
	}
}
