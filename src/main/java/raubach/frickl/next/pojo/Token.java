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

package raubach.frickl.next.pojo;

import raubach.frickl.next.util.Permission;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sebastian Raubach
 */
public class Token
{
	private String               token;
	private String               imageToken;
	private Short                permissions;
	private List<UserPermission> allPermissions = Arrays.stream(Permission.values()).map(p -> new UserPermission(p.name(), p.getNumericValue())).collect(Collectors.toList());
	private Long                 lifetime;
	private Long                 createdOn;

	public Token()
	{
	}

	public Token(String token, String imageToken, Short permissions, Long lifetime, Long createdOn)
	{
		this.token = token;
		this.imageToken = imageToken;
		this.permissions = permissions;
		this.lifetime = lifetime;
		this.createdOn = createdOn;
	}

	public String getToken()
	{
		return token;
	}

	public Token setToken(String token)
	{
		this.token = token;
		return this;
	}

	public String getImageToken()
	{
		return imageToken;
	}

	public Token setImageToken(String imageToken)
	{
		this.imageToken = imageToken;
		return this;
	}

	public Short getPermissions()
	{
		return permissions;
	}

	public Token setPermissions(Short permissions)
	{
		this.permissions = permissions;
		return this;
	}

	public List<UserPermission> getAllPermissions()
	{
		return allPermissions;
	}

	public Long getLifetime()
	{
		return lifetime;
	}

	public Token setLifetime(Long lifetime)
	{
		this.lifetime = lifetime;
		return this;
	}

	public Long getCreatedOn()
	{
		return createdOn;
	}

	public Token setCreatedOn(Long createdOn)
	{
		this.createdOn = createdOn;
		return this;
	}
}