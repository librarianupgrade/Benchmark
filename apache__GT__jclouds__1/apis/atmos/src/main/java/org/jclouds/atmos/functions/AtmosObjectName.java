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
package org.jclouds.atmos.functions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Singleton;

import org.jclouds.atmos.domain.AtmosObject;

import com.google.common.base.Function;

@Singleton
public class AtmosObjectName implements Function<Object, String> {
	@Override
	public String apply(Object input) {
		checkArgument(checkNotNull(input, "input") instanceof AtmosObject,
				"this function is only valid for AtmosObjects!");
		AtmosObject object = AtmosObject.class.cast(input);

		return checkNotNull(object.getContentMetadata().getName() != null ? object.getContentMetadata().getName()
				: object.getSystemMetadata().getObjectName(), "objectName");
	}

}
