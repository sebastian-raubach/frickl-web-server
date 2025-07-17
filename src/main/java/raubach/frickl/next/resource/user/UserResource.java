package raubach.frickl.next.resource.user;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.conf.ParamType;
import raubach.frickl.next.Database;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.tables.pojos.Users;
import raubach.frickl.next.codegen.tables.records.UsersRecord;
import raubach.frickl.next.pojo.*;
import raubach.frickl.next.resource.*;
import raubach.frickl.next.util.*;
import raubach.frickl.next.util.BCrypt;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import static raubach.frickl.next.codegen.tables.Users.USERS;

@Path("/user")
public class UserResource extends PaginatedServerResource
{
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(Permission.IS_ADMIN)
	public Response putUser(Users newUser)
			throws SQLException
	{
		if (newUser == null || StringUtils.isEmpty(newUser.getUsername()) || StringUtils.isEmpty(newUser.getPassword()))
			return Response.status(Response.Status.BAD_REQUEST).build();

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
	@Secured(Permission.IS_ADMIN)
	public Response putUser(@PathParam("userId") Integer userId, Users changedUser)
			throws SQLException
	{
		if (userId == null || changedUser == null || !Objects.equals(userId, changedUser.getId()))
			return Response.status(Response.Status.BAD_REQUEST).build();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			UsersRecord existingUser = context.selectFrom(USERS).where(USERS.ID.eq(userId)).fetchAny();

			if (existingUser == null)
				return Response.status(Response.Status.NOT_FOUND).build();
			if (Objects.equals(existingUser.getUsername(), PropertyWatcher.get(ServerProperty.ADMIN_USERNAME)))
				return Response.status(Response.Status.FORBIDDEN).build();

			if (!StringUtils.isEmpty(changedUser.getUsername()))
				existingUser.setUsername(changedUser.getUsername());
			if (!StringUtils.isEmpty(changedUser.getPassword()))
				existingUser.setPassword(BCrypt.hashpw(changedUser.getPassword(), BCrypt.gensalt(TokenResource.SALT)));
			if (changedUser.getPermissions() != null)
				existingUser.setPermissions(changedUser.getPermissions());
			if (changedUser.getViewType() != null)
				existingUser.setViewType(changedUser.getViewType());

			UserAlbumAccessStore.forceUpdate(context, new AuthenticationFilter.UserDetails(existingUser.getId(), null, null, existingUser.getPermissions(), existingUser.getViewType(), null));

			return Response.ok(existingUser.store() > 0).build();
		}
	}

	@DELETE
	@Path("/{userId:\\d+}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(Permission.IS_ADMIN)
	public Response deleteUser(@PathParam("userId") Integer userId)
			throws SQLException
	{
		if (userId == null)
			return Response.status(Response.Status.NOT_FOUND).build();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);

			UsersRecord user = context.selectFrom(USERS).where(USERS.ID.eq(userId)).fetchAny();

			if (user != null)
			{
				if (Permission.IS_ADMIN.allows(user.getPermissions()) && Objects.equals(PropertyWatcher.get(ServerProperty.ADMIN_USERNAME), user.getUsername()))
				{
					// This is the main admin. We can't allow deleting that one
					return Response.status(Response.Status.FORBIDDEN).build();
				}
				else
				{
					user.delete();
					return Response.ok().build();
				}
			}
			else
			{
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(Permission.SETTINGS_CHANGE)
	public PaginatedResult<List<UserDetails>> postUsers(PaginatedRequest request)
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

			List<UserDetails> result = setPaginationAndOrderBy(step)
					.fetchInto(UserDetails.class);

			if (!CollectionUtils.isEmpty(result))
			{
				result.forEach(u -> {
					u.setCanBeEdited(!(Objects.equals(u.getUsername(), PropertyWatcher.get(ServerProperty.ADMIN_USERNAME))));
					u.setPassword(null);
				});
			}

			long count = previousCount == -1 ? context.fetchOne("SELECT FOUND_ROWS()").into(Long.class) : previousCount;

			return new PaginatedResult<>(result, count);
		}
	}
}
