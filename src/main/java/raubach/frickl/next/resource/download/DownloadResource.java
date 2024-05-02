package raubach.frickl.next.resource.download;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.apache.commons.io.FileUtils;
import raubach.frickl.next.ApplicationListener;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.pojo.*;
import raubach.frickl.next.resource.ContextResource;
import raubach.frickl.next.util.*;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Path("/download")
public class DownloadResource extends ContextResource
{
	@POST
	@Path("/status")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response checkDownloadStatus(List<String> uuids)
			throws SQLException
	{
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		if (CollectionUtils.isEmpty(uuids))
			return Response.ok(new ArrayList<AsyncAlbumExportResult>()).build();

		return Response.ok(uuids.stream()
								.map(ApplicationListener.SCHEDULER_IDS::get)
								.filter(j -> j != null && Objects.equals(j.getUserToken(), userDetails.getToken()))
								.map(j -> {
									try
									{
										if (ApplicationListener.SCHEDULER.isJobFinished(j.getJobId()))
										{
											// The job is "finished", so check if the result exists
											String version = PropertyWatcher.get(ServerProperty.API_VERSION);
											File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-exports" + "-" + version);
											File targetFolder = new File(folder, j.getToken());
											// Get zip result files (there'll only be one per folder)
											File[] zipFiles = targetFolder.listFiles((dir, name) -> name.endsWith(".zip"));

											if (!CollectionUtils.isEmpty(zipFiles))
												j.setStatus(ExportStatus.FINISHED);
											else
												j.setStatus(ExportStatus.EXPIRED);
										}
										else
										{
											j.setStatus(ExportStatus.RUNNING);
										}
									}
									catch (Exception e)
									{
										// Do nothing here
									}
									return j;
								})
								.collect(Collectors.toList()))
					   .build();
	}

	@GET
	@Path("/{uuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("application/zip")
	public Response downloadAlbumByToken(@PathParam("uuid") String uuid)
			throws SQLException
	{
		String version = PropertyWatcher.get(ServerProperty.API_VERSION);
		File folder = new File(System.getProperty("java.io.tmpdir"), "frickl-exports" + "-" + version);
		File targetFolder = new File(folder, uuid);

		AsyncExportResult info = ApplicationListener.SCHEDULER_IDS.get(uuid);

		if (info == null)
		{
			// Job isn't in the active map anymore, but may still exist locally in a file. We cannot check user tokens for that.
			// Get zip result files (there'll only be one per folder)
			File[] zipFiles = targetFolder.listFiles((dir, name) -> name.endsWith(".zip"));
			if (!CollectionUtils.isEmpty(zipFiles))
			{
				java.nio.file.Path zipFilePath = zipFiles[0].toPath();
				return Response.ok((StreamingOutput) output -> {
								   java.nio.file.Files.copy(zipFilePath, output);
								   // Delete the whole folder once we're done
								   FileUtils.deleteDirectory(targetFolder);
							   })
							   .type("application/zip")
							   .header("content-disposition", "attachment;filename= \"" + zipFiles[0].getName() + "\"")
							   .header("content-length", zipFiles[0].length())
							   .build();
			}
			else
			{
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		}

		try
		{
			if (ApplicationListener.SCHEDULER.isJobFinished(info.getJobId()))
			{
				// Get zip result files (there'll only be one per folder)
				File[] zipFiles = targetFolder.listFiles((dir, name) -> name.endsWith(".zip"));

				if (!CollectionUtils.isEmpty(zipFiles))
				{
					java.nio.file.Path zipFilePath = zipFiles[0].toPath();
					return Response.ok((StreamingOutput) output -> {
									   java.nio.file.Files.copy(zipFilePath, output);
									   // Delete the whole folder once we're done
									   FileUtils.deleteDirectory(targetFolder);
								   })
								   .type("application/zip")
								   .header("content-disposition", "attachment;filename= \"" + zipFiles[0].getName() + "\"")
								   .header("content-length", zipFiles[0].length())
								   .build();
				}
				else
				{
					return Response.status(Response.Status.NOT_FOUND).build();
				}
			}
			else
			{
				return Response.status(Response.Status.ACCEPTED).build();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
