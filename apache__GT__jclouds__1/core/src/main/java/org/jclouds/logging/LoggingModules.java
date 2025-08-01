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
package org.jclouds.logging;

import static com.google.common.collect.Iterables.find;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import org.jclouds.logging.config.LoggingModule;
import org.jclouds.logging.jdk.config.JDKLoggingModule;

import com.google.common.base.Predicates;

/**
 * The LoggingModules class provides static methods for accessing
 * loggingModules.
 */
public class LoggingModules {

	/**
	* Returns the {@link LoggingModule}s located on the classpath via
	* {@link java.util.ServiceLoader}.
	* 
	*/
	private static Iterable<LoggingModule> fromServiceLoader() {
		return ServiceLoader.load(LoggingModule.class);
	}

	/**
	* all available {@link LoggingModule}s
	*/
	public static Iterable<LoggingModule> all() {
		return fromServiceLoader();
	}

	/**
	* the first available {@link LoggingModule}s from
	* {@link java.util.ServiceLoader} or {@link JDKLoggingModule}
	*/
	public static LoggingModule firstOrJDKLoggingModule() {
		try {
			return find(ServiceLoader.load(LoggingModule.class),
					Predicates.not(Predicates.instanceOf(JDKLoggingModule.class)));
		} catch (NoSuchElementException e) {
			return new JDKLoggingModule();
		}
	}
}
