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
package org.apache.servicecomb.foundation.protobuf.performance.engine;

import java.io.IOException;

import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot;
import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot.Root;
import org.apache.servicecomb.foundation.protobuf.internal.model.ProtobufRoot.Root.Builder;
import org.apache.servicecomb.foundation.protobuf.performance.ProtubufCodecEngine;

public class Protobuf implements ProtubufCodecEngine {
	@Override
	public byte[] serialize(Object builder) {
		return ((Builder) builder).build().toByteArray();
	}

	@Override
	public Object deserialize(byte[] bytes) throws IOException {
		Root.Builder builder = ProtobufRoot.Root.newBuilder().mergeFrom(bytes);
		builder.build();
		return builder;
	}
}
