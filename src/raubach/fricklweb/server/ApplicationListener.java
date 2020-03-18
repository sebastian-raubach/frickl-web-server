package raubach.fricklweb.server;

import raubach.fricklweb.server.scanner.ImageScanner;
import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;

public class ApplicationListener implements ServletContextListener
{
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
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent)
	{
		PropertyWatcher.stopFileWatcher();
	}
}
