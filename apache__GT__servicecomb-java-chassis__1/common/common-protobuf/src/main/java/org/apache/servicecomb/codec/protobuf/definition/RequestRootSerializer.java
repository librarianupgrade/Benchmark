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
package org.apache.servicecomb.codec.protobuf.definition;

import java.io.IOException;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.RootSerializer;

import io.vertx.core.json.JsonObject;

public class RequestRootSerializer {
	private final RootSerializer rootSerializer;

	private final boolean noTypesInfo;

	private final boolean isWrap;

	public RequestRootSerializer(RootSerializer serializer, boolean isWrap, boolean noTypesInfo) {
		this.rootSerializer = serializer;
		this.noTypesInfo = noTypesInfo;
		this.isWrap = isWrap;
	}

	@SuppressWarnings("unchecked")
	public byte[] serialize(Object value) throws IOException {
		if (noTypesInfo && !isWrap) {
			Object param = ((Map<String, Object>) value).values().iterator().next();
			if (param instanceof JsonObject) {
				param = ((JsonObject) param).getMap();
			}
			return this.rootSerializer.serialize(param);
		} else {
			return this.rootSerializer.serialize(value);
		}
	}
}
