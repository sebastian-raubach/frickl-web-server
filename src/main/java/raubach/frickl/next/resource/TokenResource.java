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
import org.jooq.tools.StringUtils;
import raubach.frickl.next.auth.*;
import raubach.frickl.next.pojo.*;
import raubach.frickl.next.util.ServerProperty;
import raubach.frickl.next.util.watcher.PropertyWatcher;

import java.io.IOException;
import java.util.*;

/**
 * @author Sebastian Raubach
 */
@Path("token")
public class TokenResource extends ContextResource
{
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
		throws IOException
	{
		boolean enabled = PropertyWatcher.authEnabled();

		if (!enabled)
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();

		String username = PropertyWatcher.get(ServerProperty.ADMIN_USERNAME);
		String password = PropertyWatcher.get(ServerProperty.ADMIN_PASSWORD);

		boolean canAccess = !StringUtils.isEmpty(request.getUsername()) && !StringUtils.isEmpty(request.getPassword()) && Objects.equals(username, request.getUsername()) && Objects.equals(password, request.getPassword());

		String token;
		String imageToken;

		if (canAccess)
		{
			token = UUID.randomUUID().toString();
			imageToken = UUID.randomUUID().toString();
			AuthenticationFilter.addToken(req, resp, token, imageToken);
		}
		else
		{
			return Response.status(Response.Status.FORBIDDEN.getStatusCode(), StatusMessage.FORBIDDEN_INVALID_CREDENTIALS).build();
		}

		return Response.ok(new Token(token, imageToken, AuthenticationFilter.AGE, System.currentTimeMillis())).build();
	}
}