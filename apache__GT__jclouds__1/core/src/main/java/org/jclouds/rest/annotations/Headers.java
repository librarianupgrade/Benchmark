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
package org.jclouds.rest.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Designates that a header will be added to the request. This header will contain the specified
 * {@code value}, expanding any variables annotated with {@code PathParam}.
 * 
 * @see PathParam
 */
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface Headers {

	/**
	* @see HttpHeaders
	*/
	String[] keys();

	/**
	* can be defined literally, or with enclosed variables (ex. <code>{variable}</code>)
	* <p/>
	* The inputs to these variables are taken from method parameters annotated with {@code
	* @PathParam}.
	* 
	* @see javax.ws.rs.PathParam
	* 
	*/
	String[] values();
}
