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
package org.jclouds.glesys.config;

import org.jclouds.glesys.GleSYSApi;
import org.jclouds.glesys.handlers.GleSYSErrorHandler;
import org.jclouds.http.HttpErrorHandler;
import org.jclouds.http.HttpRetryHandler;
import org.jclouds.http.annotation.ClientError;
import org.jclouds.http.annotation.Redirection;
import org.jclouds.http.annotation.ServerError;
import org.jclouds.http.handlers.BackoffLimitedRetryHandler;
import org.jclouds.location.suppliers.ImplicitLocationSupplier;
import org.jclouds.location.suppliers.implicit.OnlyLocationOrFirstZone;
import org.jclouds.rest.ConfiguresHttpApi;
import org.jclouds.rest.config.HttpApiModule;

import com.google.inject.Scopes;

/**
 * Configures the GleSYS connection.
 */
@ConfiguresHttpApi
public class GleSYSHttpApiModule extends HttpApiModule<GleSYSApi> {

	@Override
	protected void configure() {
		install(new GleSYSParserModule());
		super.configure();
	}

	@Override
	protected void bindErrorHandlers() {
		bind(HttpErrorHandler.class).annotatedWith(Redirection.class).to(GleSYSErrorHandler.class);
		bind(HttpErrorHandler.class).annotatedWith(ClientError.class).to(GleSYSErrorHandler.class);
		bind(HttpErrorHandler.class).annotatedWith(ServerError.class).to(GleSYSErrorHandler.class);
	}

	@Override
	protected void bindRetryHandlers() {
		bind(HttpRetryHandler.class).annotatedWith(ClientError.class).to(BackoffLimitedRetryHandler.class);
	}

	@Override
	protected void installLocations() {
		super.installLocations();
		bind(ImplicitLocationSupplier.class).to(OnlyLocationOrFirstZone.class).in(Scopes.SINGLETON);
	}
}
