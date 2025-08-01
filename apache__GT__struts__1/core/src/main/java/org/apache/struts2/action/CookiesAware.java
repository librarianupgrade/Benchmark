/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts2.action;

import org.apache.struts2.interceptor.CookieInterceptor;

import java.util.Map;

/**
 * Actions implementing the CookiesAware interface will receive
 * a Map of filtered cookies via the setCookiesMap method.
 *
 * Please note that the {@link CookieInterceptor} needs to be
 * activated to receive a cookies map.
 *
 * @since 2.6
 */
public interface CookiesAware {
	/**
	 * Sets a map of filtered cookies.
	 * @param cookies the cookies
	 */
	void withCookies(Map<String, String> cookies);
}