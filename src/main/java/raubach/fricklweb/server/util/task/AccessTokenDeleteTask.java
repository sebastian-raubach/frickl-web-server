package raubach.fricklweb.server.util.task;

import org.jooq.DSLContext;
import raubach.fricklweb.server.Database;

import java.sql.*;

import static raubach.fricklweb.server.database.tables.AccessTokens.*;

public class AccessTokenDeleteTask implements Runnable
{
	@Override
	public void run()
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			context.deleteFrom(ACCESS_TOKENS)
				   .where(ACCESS_TOKENS.EXPIRES_ON.isNotNull()
												  .and(ACCESS_TOKENS.EXPIRES_ON.le(new Timestamp(System.currentTimeMillis()))))
				   .execute();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
