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
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import raubach.fricklweb.server.auth.CustomVerifier;
import raubach.fricklweb.server.computed.LoginDetails;
import raubach.fricklweb.server.computed.StatusMessage;
import raubach.fricklweb.server.computed.Token;
import raubach.fricklweb.server.util.ServerProperty;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.util.Objects;
import java.util.UUID;

/**
 * {@link ServerResource} handling {@link TokenResource} requests.
 *
 * @author Sebastian Raubach
 */
public class TokenResource extends ServerResource
{
	@Delete("json")
	public boolean deleteJson(LoginDetails user)
	{
		boolean enabled = PropertyWatcher.authEnabled();

		if (!enabled)
			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);

		if (user == null)
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, StatusMessage.NOT_FOUND_TOKEN);

		CustomVerifier.UserDetails sessionUser = CustomVerifier.getFromSession(getRequest(), getResponse());

		if (sessionUser == null || !Objects.equals(sessionUser.getToken(), user.getPassword()))
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, StatusMessage.FORBIDDEN_ACCESS_TO_OTHER_USER);

		try
		{
			// Try and see if it's a valid UUID
			UUID.fromString(user.getPassword());
			return CustomVerifier.removeToken(user.getPassword(), getRequest(), getResponse());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Post("json")
	public Token postJson(LoginDetails request)
	{
		boolean enabled = PropertyWatcher.authEnabled();

		if (!enabled)
			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);

		String username = PropertyWatcher.get(ServerProperty.ADMIN_USERNAME);
		String password = PropertyWatcher.get(ServerProperty.ADMIN_PASSWORD);

		boolean canAccess = !StringUtils.isEmpty(request.getUsername()) && !StringUtils.isEmpty(request.getPassword()) && Objects.equals(username, request.getUsername()) && Objects.equals(password, request.getPassword());

		String token;
		String imageToken;

		if (canAccess)
		{
			token = UUID.randomUUID().toString();
			imageToken = UUID.randomUUID().toString();
			CustomVerifier.addToken(getRequest(), getResponse(), token, imageToken);
		}
		else
		{
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, StatusMessage.FORBIDDEN_INVALID_CREDENTIALS);
		}

		return new Token(token, imageToken, CustomVerifier.AGE, System.currentTimeMillis());
	}
}