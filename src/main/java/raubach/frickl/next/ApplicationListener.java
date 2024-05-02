package raubach.frickl.next;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import jhi.oddjob.*;
import raubach.frickl.next.pojo.*;
import raubach.frickl.next.scanner.ImageScanner;
import raubach.frickl.next.util.*;
import raubach.frickl.next.util.task.AlbumDownloadFolderCleanupTask;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.File;
import java.util.concurrent.*;

@WebListener
public class ApplicationListener implements ServletContextListener
{
	private static      ScheduledExecutorService                     backgroundScheduler;
	public static final IScheduler                                   SCHEDULER     = new ProcessScheduler();
	public static final ConcurrentHashMap<String, AsyncExportResult> SCHEDULER_IDS = new ConcurrentHashMap<>();

	public static void startImageScanner(File file)
	{
		if (ImageScanner.SCANRESULT.getStatus() == Status.IDLE)
		{
			// Spin off a thread to run the initial data import/update
			backgroundScheduler.schedule(new ImageScanner(new File(Frickl.BASE_PATH), file), 0, TimeUnit.SECONDS);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		System.setProperty("org.jooq.no-logo", "true");
		System.setProperty("org.jooq.no-tips", "true");

		PropertyWatcher.initialize();

		UserAlbumAccessStore.initialize();

		try
		{
			SCHEDULER.initialize();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Frickl.BASE_PATH = PropertyWatcher.get(ServerProperty.BASE_PATH);

		backgroundScheduler = Executors.newSingleThreadScheduledExecutor();
		backgroundScheduler.scheduleAtFixedRate(new AlbumDownloadFolderCleanupTask(), 0, 1, TimeUnit.DAYS);

		startImageScanner(new File(Frickl.BASE_PATH));
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent)
	{
		PropertyWatcher.stopFileWatcher();
		ThumbnailUtils.shutdownExecutor();

		try
		{
			SCHEDULER.destroy();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			// Stop the scheduler
			if (backgroundScheduler != null)
				backgroundScheduler.shutdownNow();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
