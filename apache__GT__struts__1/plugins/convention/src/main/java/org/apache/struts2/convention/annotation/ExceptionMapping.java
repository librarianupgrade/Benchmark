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
package org.apache.struts2.convention.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <!-- START SNIPPET: javadoc -->
 * <p>
 * Adds an exception mapping to an action
 * </p>
 * <!-- END SNIPPET: javadoc -->
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExceptionMapping {
	/**
	 * @return  Result name
	 */
	String result();

	/**
	 * @return  Class name of the exception to be thrown
	 */
	String exception();

	/**
	 * @return  The parameters passed to the exception. This is a list of strings that form a name/value
	 *          pair chain since creating a Map for annotations is not possible. An example would be:
	 *          <code>{"key", "value", "key2", "value2"}</code>.
	 */
	String[] params() default {};
}
