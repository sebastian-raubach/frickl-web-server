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

package raubach.fricklweb.server.resource;

import org.jooq.tools.StringUtils;
import raubach.fricklweb.server.auth.*;
import raubach.fricklweb.server.computed.*;
import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
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
	public boolean deleteToken(LoginDetails user)
		throws IOException
	{
		boolean enabled = PropertyWatcher.authEnabled();

		if (!enabled)
		{
			resp.sendError(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
			return false;
		}

		if (user == null)
		{
			resp.sendError(Response.Status.NOT_FOUND.getStatusCode(), StatusMessage.NOT_FOUND_TOKEN);
			return false;
		}

		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();

		if (userDetails == null || !Objects.equals(userDetails.getToken(), user.getPassword()))
		{
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode(), StatusMessage.FORBIDDEN_ACCESS_TO_OTHER_USER);
			return false;
		}

		try
		{
			// Try and see if it's a valid UUID
			UUID.fromString(user.getPassword());
			AuthenticationFilter.removeToken(user.getPassword(), req, resp);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Token postToken(LoginDetails request)
		throws IOException
	{
		boolean enabled = PropertyWatcher.authEnabled();

		if (!enabled)
		{
			resp.sendError(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
			return null;
		}

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
			resp.sendError(Response.Status.FORBIDDEN.getStatusCode(), StatusMessage.FORBIDDEN_INVALID_CREDENTIALS);
			return null;
		}

		return new Token(token, imageToken, AuthenticationFilter.AGE, System.currentTimeMillis());
	}
}