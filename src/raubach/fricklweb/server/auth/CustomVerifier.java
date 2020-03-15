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

package raubach.fricklweb.server.auth;

import org.jooq.tools.StringUtils;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.resource.ResourceException;
import org.restlet.security.Verifier;
import raubach.fricklweb.server.util.watcher.PropertyWatcher;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Sebastian Raubach
 */
public class CustomVerifier implements Verifier
{
	public static final long AGE = 43200000; // 12 hours

	private static Map<String, UserDetails> tokenToTimestamp = new ConcurrentHashMap<>();
	private static Map<String, String> tokenToImageToken = new ConcurrentHashMap<>();

	public CustomVerifier()
	{
		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				tokenToTimestamp.entrySet().removeIf(token -> {
					boolean expired = token.getValue().timestamp < (System.currentTimeMillis() - AGE);

					if (expired)
						tokenToImageToken.remove(token.getKey());

					return expired;
				});
			}
		}, 0, 60000);
	}

	public static boolean removeToken(String token, Request request, Response response)
	{
		if (token != null)
		{
			UserDetails exists = tokenToTimestamp.remove(token);

			if (exists != null)
			{
				tokenToImageToken.remove(exists.imageToken);
				setCookie(request, response, null);
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	private static TokenResult getToken(Request request, Response response)
	{
		ChallengeResponse cr = request.getChallengeResponse();
		if (cr != null)
		{
			TokenResult result = new TokenResult();
			result.token = cr.getRawValue();

			if (StringUtils.isEmpty(result.token) || result.token.equalsIgnoreCase("null"))
				result.token = null;

			// If we do, validate it against the cookie
			List<Cookie> cookies = request.getCookies()
					.stream()
					.filter(c -> Objects.equals(c.getName(), "token"))
					.collect(Collectors.toList());

			if (cookies.size() > 0)
			{
				result.match = Objects.equals(result.token, cookies.get(0).getValue());

				if (!result.match)
					setCookie(request, response, null);
			}
			else
			{
				if (result.token == null)
					return null;
				else
					result.match = true;
			}

			return result;
		}

		return null;
	}

	public static UserDetails getFromSession(Request request, Response response)
	{
		TokenResult token = getToken(request, response);

		// If there is no token or token and cookie don't match, remove the cookie
		if (token == null || !token.match)
			setCookie(request, response, null);

		if (token == null)
		{
			// We get here if no token is found at all
			return new UserDetails(null, null, AGE);
		}
		else if (!StringUtils.isEmpty(token.token) && !token.match)
		{
			// We get here if a token is provided, but no matching cookie is found.
			throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);
		}
		else
		{
			// We get here if the token is present and it matches the cookie
			UserDetails details = token.token != null ? tokenToTimestamp.get(token.token) : null;

			if (details == null)
				throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

			return details;
		}
	}

	public static void addToken(Request request, Response response, String token, String imageToken)
	{
		setCookie(request, response, token);
		UserDetails details = new UserDetails();
		details.timestamp = System.currentTimeMillis();
		details.imageToken = imageToken;
		details.token = token;
		tokenToTimestamp.put(token, details);
		tokenToImageToken.put(token, details.imageToken);
	}

	private static void setCookie(Request request, Response response, String token)
	{
		boolean delete = StringUtils.isEmpty(token);

		CookieSetting cookie = new CookieSetting(0, "token", token);
		cookie.setAccessRestricted(true);

		if (delete)
		{
			cookie.setMaxAge(0);
			cookie.setPath(getContextPath(request));
			cookie.setValue("");
		}
		else
		{
			cookie.setMaxAge((int) (AGE / 1000));
			cookie.setPath(getContextPath(request));
		}
		response.getCookieSettings().add(cookie);
	}

	private static String getContextPath(Request request)
	{
		String result = ServletUtils.getRequest(request).getContextPath();

		if (!StringUtils.isEmpty(result))
		{
			try
			{
				result = URLDecoder.decode(result, "UTF-8");

				int index = result.lastIndexOf("/api");

				if (index != -1)
				{
					result = result.substring(0, index + 4);
				}
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		if (StringUtils.isEmpty(result))
			result = "/";

		return result;
	}

	public static boolean isValidImageToken(String imageToken)
	{
		return !StringUtils.isEmpty(imageToken) && tokenToImageToken.containsValue(imageToken);
	}

	@Override
	public int verify(Request request, Response response)
	{
		boolean enabled = PropertyWatcher.authEnabled();
		String method = request.getMethod().getName();

		// If authentication is enabled and it's neither a POST nor a GET, then check credentials
		if (enabled && !Objects.equals(method, "POST") && !Objects.equals(method, "GET"))
		{
			ChallengeResponse cr = request.getChallengeResponse();
			if (cr != null)
			{
				TokenResult token = getToken(request, response);

				if (token != null && token.token != null)
				{
					boolean canAccess = false;

					// Check if it's a valid token
					UserDetails details = tokenToTimestamp.get(token.token);

					if (details != null)
					{
						// First, check the bearer token and see if we have it in the cache
						if ((System.currentTimeMillis() - AGE) < details.timestamp)
						{
							canAccess = true;
							// Extend the cookie
							details.timestamp = System.currentTimeMillis();
							tokenToTimestamp.put(token.token, details);
						}
						else
						{
							return RESULT_STALE;
						}
					}

					if (canAccess)
					{
						// Extend the cookie here
						setCookie(request, response, token.token);
						return RESULT_VALID;
					}
					else
					{
						removeToken(token.token, request, response);
						return RESULT_INVALID;
					}
				}
				else
				{
					removeToken(null, request, response);
					return RESULT_INVALID;
				}
			}
			else
			{
				return RESULT_MISSING;
			}
		}
		else
		{
			return RESULT_VALID;
		}
	}

	private static class TokenResult
	{
		private String token;
		private boolean match;

		@Override
		public String toString()
		{
			return "TokenResult{" +
					"token='" + token + '\'' +
					", match=" + match +
					'}';
		}
	}

	public static class UserDetails
	{
		private String token;
		private String imageToken;
		private Long timestamp;

		public UserDetails()
		{
		}

		public UserDetails(String token, String imageToken, Long timestamp)
		{
			this.token = token;
			this.imageToken = imageToken;
			this.timestamp = timestamp;
		}

		public String getToken()
		{
			return token;
		}

		public String getImageToken()
		{
			return imageToken;
		}

		public Long getTimestamp()
		{
			return timestamp;
		}

		@Override
		public String toString()
		{
			return "UserDetails{" +
					", token='" + token + '\'' +
					", imageToken='" + imageToken + '\'' +
					", timestamp=" + timestamp +
					'}';
		}
	}
}