package raubach.fricklweb.server;

import java.io.*;

import javax.servlet.*;

import raubach.fricklweb.server.scanner.*;

public class ApplicationListener implements ServletContextListener
{
	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		ServletContext ctx = sce.getServletContext();
		String database = ctx.getInitParameter("database");
		String username = ctx.getInitParameter("username");
		String password = ctx.getInitParameter("password");
		Database.init(database, username, password);

		Frickl.BASE_PATH = ctx.getInitParameter("basePath");

		// Spin off a thread to run the initial data import/update
		new Thread(() -> {
			File file = new File(Frickl.BASE_PATH);
			try
			{
				new ImageScanner(ctx, file, file)
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
	}
}
