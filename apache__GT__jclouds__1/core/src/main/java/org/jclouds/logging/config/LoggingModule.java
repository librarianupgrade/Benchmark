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
package org.jclouds.logging.config;

import static com.google.inject.matcher.Matchers.any;

import javax.inject.Singleton;

import org.jclouds.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Creates a post-injection listener that binds Loggers named the same as the enclosing class.
 */
public abstract class LoggingModule extends AbstractModule {

	@Override
	protected void configure() {
		bindListener(any(), new BindLoggersAnnotatedWithResource(createLoggerFactory()));
	}

	@Provides
	@Singleton
	public final Logger.LoggerFactory provideLoggerFactory() {
		return createLoggerFactory();
	}

	public abstract Logger.LoggerFactory createLoggerFactory();
}
