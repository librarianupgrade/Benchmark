/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.scriptbuilder.domain;

import java.net.URI;

import com.google.common.collect.Multimap;

/**
 * Pipes the content of the http response to bash
 */
public class PipeHttpResponseToBash extends PipeHttpResponseTo {
	/**
	* 
	* 
	* @param method
	*           http method: ex GET
	* @param endpoint
	*           uri corresponding to the request
	* @param headers
	*           request headers to send
	*/
	public PipeHttpResponseToBash(String method, URI endpoint, Multimap<String, String> headers) {
		super(Statements.interpret("bash"), method, endpoint, headers);
	}

}
