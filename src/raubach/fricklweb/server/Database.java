package raubach.fricklweb.server;

import org.jooq.*;
import org.jooq.impl.*;
import raubach.fricklweb.server.util.ScriptRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sebastian Raubach
 */
public class Database
{
	private static String database;
	private static String username;
	private static String password;

	public static void init(String database, String username, String password)
	{
		Database.database = database;
		Database.username = username;
		Database.password = password;

		try {
			// The newInstance() call is a work around for some
			// broken Java implementations

			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			// handle the error
		}

		// Get an initial connection to try if it works
		try (Connection conn = getConnection())
		{
			File databaseScript = new File(Database.class.getClassLoader().getResource("database.sql").toURI());
			Logger.getLogger("").log(Level.INFO, "Running database setup: " + databaseScript.getAbsolutePath());

			try(BufferedReader br = new BufferedReader(new FileReader(databaseScript))) {
				new ScriptRunner(conn, true, true)
						.runScript(br);
			}
			DSL.using(conn, SQLDialect.MYSQL).close();
			Logger.getLogger("").log(Level.INFO, "Finished running database setup: " + databaseScript.getAbsolutePath());
		}
		catch (SQLException | URISyntaxException | IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Connection getConnection()
		throws SQLException
	{
		return DriverManager.getConnection(database, username, password);
	}
}
