package raubach.frickl.next.resource.image;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.tools.StringUtils;
import raubach.frickl.next.*;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.tables.records.ImagesRecord;
import raubach.frickl.next.resource.AbstractAccessTokenResource;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.*;
import java.io.File;
import java.nio.channels.*;
import java.sql.*;
import java.util.Date;
import java.util.logging.*;

import static raubach.frickl.next.codegen.tables.AccessTokens.ACCESS_TOKENS;
import static raubach.frickl.next.codegen.tables.AlbumTokens.ALBUM_TOKENS;
import static raubach.frickl.next.codegen.tables.Images.IMAGES;

@Path("image/{imageId:\\d+}/video")
@Secured
public class VideoResource extends AbstractAccessTokenResource
{
	private final int CHUNK_SIZE = 1024 * 1024 * 2; // 2 MB chunks

	@PathParam("imageId")
	Integer imageId;

	@HEAD
	@Path("/{filename}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"video/quicktime", "video/mp4", "video/x-msvideo", "video/x-ms-wmv", "video/webm", "video/mkv"})
	@PermitAll
	public Response getVideoByNameHead(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @HeaderParam("Range") String range)
			throws IOException, SQLException
	{
		return this.getVideo(imageId, token, range, true);
	}

	@GET
	@Path("/{filename}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"video/quicktime", "video/mp4", "video/x-msvideo", "video/x-ms-wmv", "video/webm", "video/mkv"})
	@PermitAll
	public Response getVideoByName(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @HeaderParam("Range") String range)
			throws IOException, SQLException
	{
		return this.getVideo(imageId, token, range, false);
	}

	@HEAD
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"video/quicktime", "video/mp4", "video/x-msvideo", "video/x-ms-wmv", "video/webm", "video/mkv"})
	@PermitAll
	public Response getVideoHead(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @HeaderParam("Range") String range)
			throws IOException, SQLException
	{
		return this.getVideo(imageId, token, range, true);
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({"video/quicktime", "video/mp4", "video/x-msvideo", "video/x-ms-wmv", "video/webm", "video/mkv"})
	@PermitAll
	public Response getVideo(@PathParam("imageId") Integer imageId, @QueryParam("token") String token, @HeaderParam("Range") String range)
			throws IOException, SQLException
	{
		return this.getVideo(imageId, token, range, false);
	}

	private Response getVideo(Integer imageId, String token, String range, boolean isHead)
			throws IOException, SQLException
	{
		boolean auth = PropertyWatcher.authEnabled();

		if (imageId != null)
		{
			try (Connection conn = Database.getConnection())
			{
				DSLContext context = Database.getContext(conn);
				SelectConditionStep<Record> step = context.select().from(IMAGES)
														  .where(IMAGES.ID.eq(imageId));

				if (auth)
				{
					if (!StringUtils.isEmpty(accessToken))
					{
						step.and(DSL.exists(DSL.selectOne()
												 .from(ALBUM_TOKENS)
												 .leftJoin(ACCESS_TOKENS).on(ACCESS_TOKENS.ID.eq(ALBUM_TOKENS.ACCESS_TOKEN_ID))
												 .where(ACCESS_TOKENS.TOKEN.eq(accessToken)
																		   .and(ALBUM_TOKENS.ALBUM_ID.eq(IMAGES.ALBUM_ID)))));
					}
					else if (!AuthenticationFilter.isValidImageToken(token))
					{
						step.and(IMAGES.IS_PUBLIC.eq((byte) 1));
					}
				}

				ImagesRecord image = step.fetchAnyInto(ImagesRecord.class);

				if (image != null)
				{
					java.io.File file = new java.io.File(Frickl.BASE_PATH, image.getPath());

					// Check if the image exists
					if (file.exists() && file.isFile())
					{
						if (isHead)
						{
							image.setViewCount(image.getViewCount() + 1);
							image.store(IMAGES.VIEW_COUNT);

							return Response.ok()
										   .status(Response.Status.PARTIAL_CONTENT)
										   .header(HttpHeaders.CONTENT_LENGTH, file.length())
										   .header("Accept-Ranges", "bytes")
										   .build();
						}
						else
						{
							return this.buildStream(file, range);
						}
					}
					else
					{
						Logger.getLogger("").log(Level.WARNING, "File not found: " + file.getAbsolutePath());
						return Response.status(Response.Status.NOT_FOUND).build();
					}
				}
			}
		}
		else
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return Response.noContent().build();
	}

	private Response buildStream(final File asset, final String range)
			throws IOException
	{
		// range not requested: firefox does not send range headers
		if (range == null)
		{
			StreamingOutput streamer = output -> {
				try (FileInputStream fis = new FileInputStream(asset);
					 FileChannel inputChannel = fis.getChannel();
					 WritableByteChannel outputChannel = Channels.newChannel(output))
				{

					inputChannel.transferTo(0, inputChannel.size(), outputChannel);
				}
				catch (IOException io)
				{
					Logger.getLogger("").info(io.getMessage());
					io.printStackTrace();
				}
			};

			return Response.ok(streamer)
						   .status(Response.Status.OK)
						   .header(HttpHeaders.CONTENT_LENGTH, asset.length())
						   .build();
		}

		String[] ranges = range.split("=")[1].split("-");

		int from = Integer.parseInt(ranges[0]);

		// Chunk media if the range upper bound is unspecified
		int to = CHUNK_SIZE + from;

		if (to >= asset.length())
		{
			to = (int) (asset.length() - 1);
		}

		// uncomment to let the client decide the upper bound
		// we want to send 2 MB chunks all the time
		//if ( ranges.length == 2 ) {
		//    to = Integer.parseInt( ranges[1] );
		//}

		final String responseRange = String.format("bytes %d-%d/%d", from, to, asset.length());

		final RandomAccessFile raf = new RandomAccessFile(asset, "r");
		raf.seek(from);
		final int len = to - from + 1;
		final MediaStreamer mediaStreamer = new MediaStreamer(len, raf);
		return Response.ok(mediaStreamer)
					   .status(Response.Status.PARTIAL_CONTENT)
					   .header("Accept-Ranges", "bytes")
					   .header("Content-Range", responseRange)
					   .header(HttpHeaders.CONTENT_LENGTH, mediaStreamer.getLength())
					   .header(HttpHeaders.LAST_MODIFIED, new Date(asset.lastModified()))
					   .build();
	}
}
