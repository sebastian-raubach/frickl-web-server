package raubach.fricklweb.server;

import com.drew.imaging.*;
import com.drew.lang.*;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.*;

import org.restlet.*;
import org.restlet.data.*;
import org.restlet.engine.application.*;
import org.restlet.resource.*;
import org.restlet.routing.*;
import org.restlet.service.*;
import org.restlet.util.*;

import java.io.*;
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

	public static void main(String[] args)
		throws ImageProcessingException, IOException
	{
		Metadata metadata = ImageMetadataReader.readMetadata(new File("C:\\Users\\sr41756\\Downloads\\32409897731_99f4091bbc_o.jpg"));
		// See whether it has GPS data
		Collection<GpsDirectory> gpsDirectories = metadata.getDirectoriesOfType(GpsDirectory.class);
		for (GpsDirectory gpsDirectory : gpsDirectories)
		{
			// Try to read out the location, making sure it's non-zero
			GeoLocation geoLocation = gpsDirectory.getGeoLocation();
			if (geoLocation != null && !geoLocation.isZero())
			{
				// Add to our collection for use below
				System.out.println(geoLocation);
				break;
			}
		}

		Iterable<Directory> directories = metadata.getDirectories();
		Iterator<Directory> iterator = directories.iterator();
		while (iterator.hasNext())
		{
			Directory dir = iterator.next();
			Collection<Tag> tags = dir.getTags();
			for (Tag tag : tags)
			{
				System.out.println(tag.getTagName() + "  " + tag.getDescription() + " " + tag.getTagTypeHex());
			}
		}
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
		attachToRouter(router, "/album/{albumId}", AlbumResource.class);
		attachToRouter(router, "/album/{albumId}/image", ImageResource.class);
		attachToRouter(router, "/image/{imageId}", ImageResource.class);
		attachToRouter(router, "/image/{imageId}/tag", ImageTagResource.class);
		attachToRouter(router, "/image/{imageId}/img", ImageImageResource.class);
		attachToRouter(router, "/tag", TagResource.class);
		attachToRouter(router, "/tag/{tagId}", TagResource.class);
		attachToRouter(router, "/tag/{tagId}/image", TagImageResource.class);
		attachToRouter(router, "/tag/{tagId}/image/count", TagImageCountResource.class);

		// CORS first, then encoder
		corsFilter.setNext(encoder);
		// After that the unauthorized paths
		encoder.setNext(router);

		return corsFilter;
	}
}
