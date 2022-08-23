package raubach.fricklweb.server;

import raubach.fricklweb.server.computed.Status;
import raubach.fricklweb.server.scanner.ImageScanner;
import raubach.fricklweb.server.util.*;
import raubach.fricklweb.server.util.task.AccessTokenDeleteTask;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import java.io.File;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Logger;

@WebListener
public class ApplicationListener implements ServletContextListener
{
	private static ScheduledExecutorService backgroundScheduler;

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
		Logger.getLogger("").info("APPLICATION LISTENER");
		PropertyWatcher.initialize();

		Frickl.BASE_PATH = PropertyWatcher.get(ServerProperty.BASE_PATH);

		backgroundScheduler = Executors.newSingleThreadScheduledExecutor();
		// Run it now
		backgroundScheduler.schedule(new AccessTokenDeleteTask(), 0, TimeUnit.SECONDS);
		// Then at midnight each day
		long midnight = LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.MINUTES);
		backgroundScheduler.scheduleAtFixedRate(new AccessTokenDeleteTask(), midnight, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);

		startImageScanner(new File(Frickl.BASE_PATH));
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent)
	{
		PropertyWatcher.stopFileWatcher();
		ThumbnailUtils.shutdownExecutor();

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
