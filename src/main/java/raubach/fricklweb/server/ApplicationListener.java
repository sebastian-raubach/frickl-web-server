package raubach.fricklweb.server;

import raubach.fricklweb.server.scanner.ImageScanner;
import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.task.AccessTokenDeleteTask;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ApplicationListener implements ServletContextListener
{
	private static ScheduledExecutorService backgroundScheduler;

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		PropertyWatcher.initialize();

		Frickl.BASE_PATH = PropertyWatcher.get(ServerProperty.BASE_PATH);

		// Spin off a thread to run the initial data import/update
		new Thread(() -> {
			File file = new File(Frickl.BASE_PATH);
			try
			{
				new ImageScanner(file, file)
						.run();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}).start();

		backgroundScheduler = Executors.newSingleThreadScheduledExecutor();
		// Run it now
		backgroundScheduler.schedule(new AccessTokenDeleteTask(), 0, TimeUnit.SECONDS);
		// Then at midnight each day
		long midnight = LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.MINUTES);
		backgroundScheduler.scheduleAtFixedRate(new AccessTokenDeleteTask(), midnight, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent)
	{
		PropertyWatcher.stopFileWatcher();

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
