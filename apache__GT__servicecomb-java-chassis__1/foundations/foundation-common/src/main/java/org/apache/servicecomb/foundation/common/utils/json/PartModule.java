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
package org.apache.servicecomb.foundation.common.utils.json;

import org.apache.servicecomb.foundation.common.utils.SPIOrder;

import com.fasterxml.jackson.databind.module.SimpleModule;

import jakarta.servlet.http.Part;

public class PartModule extends SimpleModule implements SPIOrder {
	private static final long serialVersionUID = 4201325332650814739L;

	public PartModule() {
		super("javax-servlet-part");

		addSerializer(Part.class, new JavaxServletPartSerializer());
		addDeserializer(Part.class, new JavaxServletPartDeserializer());
	}

	@Override
	public Object getTypeId() {
		return getModuleName();
	}

	@Override
	public int getOrder() {
		return Short.MAX_VALUE;
	}
}
