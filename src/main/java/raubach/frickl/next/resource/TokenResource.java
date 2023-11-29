/*
 * Copyright 2018 Information & Computational Sciences, The James Hutton Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package raubach.frickl.next.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jooq.DSLContext;
import raubach.frickl.next.Database;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.codegen.tables.records.UsersRecord;
import raubach.frickl.next.pojo.*;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import static raubach.frickl.next.codegen.tables.Users.USERS;

/**
 * @author Sebastian Raubach
 */
@Path("token")
public class TokenResource extends ContextResource
{
	public static Integer SALT = 10;

	@DELETE
	@Secured
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteToken(LoginDetails user)
			throws IOException
	{
		boolean enabled = PropertyWatcher.authEnabled();

		if (!enabled)
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();

		if (user == null)
			return Response.status(Response.Status.NOT_FOUND).build();

		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		if (userDetails == null || !Objects.equals(userDetails.getToken(), user.getPassword()))
			return Response.status(Response.Status.FORBIDDEN.getStatusCode(), StatusMessage.FORBIDDEN_ACCESS_TO_OTHER_USER).build();

		try
		{
			// Try and see if it's a valid UUID
			UUID.fromString(user.getPassword());
			AuthenticationFilter.removeToken(user.getPassword(), req, resp);
			return Response.ok(true).build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response.ok(false).build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postToken(LoginDetails request)
			throws IOException, SQLException
	{
		boolean canAccess;
		String token;
		String imageToken;
		UsersRecord user;

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			user = context.selectFrom(USERS)
						  .where(USERS.USERNAME.eq(request.getUsername()))
						  .fetchAny();

			if (user != null)
			{
				canAccess = BCrypt.checkpw(request.getPassword(), user.getPassword());

				if (canAccess)
				{
					// Keep track of this last login event
					user.setLastLogin(new Timestamp(System.currentTimeMillis()));
					user.store(USERS.LAST_LOGIN);
				}
			}
			else
			{
				Logger.getLogger("").log(Level.SEVERE, "User not found: " + request.getUsername());
				return Response.status(Response.Status.FORBIDDEN.getStatusCode(), StatusMessage.FORBIDDEN_INVALID_CREDENTIALS).build();
			}
		}

		if (canAccess)
		{
			token = UUID.randomUUID().toString();
			imageToken = UUID.randomUUID().toString();
			AuthenticationFilter.addToken(this.req, this.resp, token, imageToken, user.getId(), user.getPermissions());

			// The salt may have changed since the last time, so update the password in the database with the new salt.
			String saltedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(SALT));

			if (!Objects.equals(saltedPassword, user.getPassword()))
			{
				try (Connection conn = Database.getConnection())
				{
					DSLContext context = Database.getContext(conn);
					context.update(USERS)
						   .set(USERS.PASSWORD, saltedPassword)
						   .where(USERS.ID.eq(user.getId()))
						   .execute();
				}
				catch (SQLException e)
				{
					Logger.getLogger("").info(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		else
		{
			return Response.status(Response.Status.FORBIDDEN.getStatusCode(), StatusMessage.FORBIDDEN_INVALID_CREDENTIALS).build();
		}

		return Response.ok(new Token(token, imageToken, user.getPermissions(), AuthenticationFilter.AGE, System.currentTimeMillis())).build();
	}
}