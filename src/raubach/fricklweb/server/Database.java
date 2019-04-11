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
	}

	public static Connection getConnection()
		throws SQLException
	{
		return DriverManager.getConnection(database, username, password);
	}

	public static DSLContext context()
		throws SQLException
	{
		return DSL.using(getConnection(), SQLDialect.MYSQL);
	}
}
