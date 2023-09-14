package raubach.frickl.next.util.task;

import org.jooq.DSLContext;
import raubach.frickl.next.Database;

import java.sql.*;

import static raubach.frickl.next.codegen.tables.AccessTokens.ACCESS_TOKENS;

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
