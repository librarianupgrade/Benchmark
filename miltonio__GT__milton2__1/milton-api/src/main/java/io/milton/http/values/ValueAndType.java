/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.milton.http.values;

/**
 * This class exists to convey type information even when a value is null.
 *
 * <p>This is important because we will often want to select parses and formatters
 * based on knowledge of the type of the value, even when that value is null.
 *
 * @author brad
 */
public class ValueAndType {

	private final Object value;
	private final Class<?> type;

	public ValueAndType(Object value, Class<?> type) {
		if (type == null) {
			throw new IllegalArgumentException("type may not be null");
		}
		if (value != null && (value.getClass() != type)) {
			throw new RuntimeException("Inconsistent type information: " + value + " != " + type);
		}
		this.value = value;
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}
}
