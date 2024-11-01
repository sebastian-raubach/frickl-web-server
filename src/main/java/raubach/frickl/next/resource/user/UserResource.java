package raubach.frickl.next.resource.user;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.Record;
import raubach.frickl.next.Database;
import raubach.frickl.next.auth.Secured;
import raubach.frickl.next.codegen.tables.pojos.Users;
import raubach.frickl.next.codegen.tables.records.UsersRecord;
import raubach.frickl.next.pojo.*;
import raubach.frickl.next.resource.*;
import raubach.frickl.next.util.*;

import java.sql.*;
import java.util.*;

import static raubach.frickl.next.codegen.tables.Users.USERS;

@Path("/user")
@Secured(Permission.IS_ADMIN)
public class UserResource extends PaginatedServerResource
{
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putUser(Users newUser)
			throws SQLException
	{
		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			newUser.setPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt(TokenResource.SALT)));
			return Response.ok(context.newRecord(USERS, newUser).store() > 0).build();
		}
	}

	@PATCH
	@Path("/{userId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putUser(@PathParam("userId") Integer userId, Users changedUser)
			throws SQLException
	{
		if (userId == null || changedUser == null || !Objects.equals(userId, changedUser.getId()))
			return Response.status(Response.Status.BAD_REQUEST).build();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			UsersRecord existingUser = context.selectFrom(USERS).where(USERS.ID.eq(userId)).fetchAny();

			if (!StringUtils.isEmpty(changedUser.getUsername()))
				existingUser.setUsername(changedUser.getUsername());
			if (!StringUtils.isEmpty(changedUser.getPassword()))
				existingUser.setPassword(BCrypt.hashpw(changedUser.getPassword(), BCrypt.gensalt(TokenResource.SALT)));
			if (changedUser.getPermissions() != null)
				existingUser.setPermissions(changedUser.getPermissions());
			if (changedUser.getViewType() != null)
				existingUser.setViewType(changedUser.getViewType());

			return Response.ok(existingUser.store() > 0).build();
		}
	}

	@DELETE
	@Path("/{userId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteUser(@PathParam("userId") Integer userId)
			throws SQLException
	{
		if (userId == null)
			return Response.status(Response.Status.NOT_FOUND).build();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			int result = context.deleteFrom(USERS)
								.where(USERS.ID.eq(userId))
								.execute();

			return Response.ok(result > 0).build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public PaginatedResult<List<Users>> postUsers(PaginatedRequest request)
			throws SQLException
	{
		processRequest(request);

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			SelectSelectStep<Record> select = context.select();

			if (previousCount == -1)
				select.hint("SQL_CALC_FOUND_ROWS");

			SelectJoinStep<Record> step = select.from(USERS);

			if (!StringUtils.isEmpty(searchTerm))
				step.where(USERS.USERNAME.contains(searchTerm));

			List<Users> result = setPaginationAndOrderBy(step)
					.fetchInto(Users.class);

			if (!CollectionUtils.isEmpty(result))
				result.forEach(u -> u.setPassword(null));

			long count = previousCount == -1 ? context.fetchOne("SELECT FOUND_ROWS()").into(Long.class) : previousCount;

			return new PaginatedResult<>(result, count);
		}
	}
}
