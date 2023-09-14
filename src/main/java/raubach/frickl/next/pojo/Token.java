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

/**
 * @author Sebastian Raubach
 */
public class Token
{
	private String token;
	private String imageToken;
	private Long   lifetime;
	private Long   createdOn;

	public Token()
	{
	}

	public Token(String token, String imageToken, Long lifetime, Long createdOn)
	{
		this.token = token;
		this.imageToken = imageToken;
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