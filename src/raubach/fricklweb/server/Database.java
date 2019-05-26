package raubach.fricklweb.server;

import org.jooq.*;
import org.jooq.impl.*;

import java.sql.*;

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
			DSL.using(conn, SQLDialect.MYSQL).close();
		}
		catch (SQLException e)
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
