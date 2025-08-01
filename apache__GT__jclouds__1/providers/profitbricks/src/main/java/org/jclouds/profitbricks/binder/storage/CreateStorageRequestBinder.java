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
package org.jclouds.profitbricks.binder.storage;

import static java.lang.String.format;
import org.jclouds.profitbricks.binder.BaseProfitBricksRequestBinder;
import org.jclouds.profitbricks.domain.Storage;

public class CreateStorageRequestBinder extends BaseProfitBricksRequestBinder<Storage.Request.CreatePayload> {

	protected final StringBuilder requestBuilder;

	CreateStorageRequestBinder() {
		super("storage");
		this.requestBuilder = new StringBuilder(128 * 2);
	}

	@Override
	protected String createPayload(Storage.Request.CreatePayload payload) {
		requestBuilder.append("<ws:createStorage>").append("<request>")
				.append(format("<dataCenterId>%s</dataCenterId>", payload.dataCenterId()))
				.append(formatIfNotEmpty("<storageName>%s</storageName>", payload.name()))
				.append(format("<size>%.0f</size>", payload.size()))
				.append(formatIfNotEmpty("<mountImageId>%s</mountImageId>", payload.mountImageId()))
				.append(formatIfNotEmpty("<profitBricksImagePassword>%s</profitBricksImagePassword>",
						payload.imagePassword()))
				.append("</request>").append("</ws:createStorage>");
		return requestBuilder.toString();
	}

}
