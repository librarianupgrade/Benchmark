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
package org.apache.struts2.interceptor;

import org.apache.struts2.dispatcher.HttpParameters;

import java.util.Map;

/**
 * <p>
 * Actions that want access to the current servlet request attributes should implement this interface.
 * </p>
 *
 * <p>
 * This interface is only relevant if the Action is used in a servlet environment.
 * </p>
 * @deprecated please use {@link org.apache.struts2.action.ParametersAware} instead
 */
@Deprecated
public interface RequestAware {

	/**
	 * Sets the Map of request attributes in the implementing class.
	 *
	 * @param request a Map of HTTP request attribute name/value pairs.
	 * @deprecated please use {@link org.apache.struts2.action.ParametersAware#withParameters(HttpParameters)} instead
	 */
	@Deprecated
	public void setRequest(Map<String, Object> request);
}
