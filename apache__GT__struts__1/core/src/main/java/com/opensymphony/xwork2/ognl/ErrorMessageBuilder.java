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
package com.opensymphony.xwork2.ognl;

/**
 * Helper class to build error messages.
 */
public class ErrorMessageBuilder {

	private final StringBuilder message = new StringBuilder();

	public static ErrorMessageBuilder create() {
		return new ErrorMessageBuilder();
	}

	private ErrorMessageBuilder() {
	}

	public ErrorMessageBuilder errorSettingExpressionWithValue(String expr, Object value) {
		appenExpression(expr);
		if (value instanceof Object[]) {
			appendValueAsArray((Object[]) value, message);
		} else {
			appendValue(value);
		}
		return this;
	}

	private void appenExpression(String expr) {
		message.append("Error setting expression '");
		message.append(expr);
		message.append("' with value ");
	}

	private void appendValue(Object value) {
		message.append("'");
		message.append(value);
		message.append("'");
	}

	private void appendValueAsArray(Object[] valueArray, StringBuilder msg) {
		msg.append("[");
		for (int index = 0; index < valueArray.length; index++) {
			appendValue(valueArray[index]);
			if (hasMoreElements(valueArray, index)) {
				msg.append(", ");
			}
		}
		msg.append("]");
	}

	private boolean hasMoreElements(Object[] valueArray, int index) {
		return index < (valueArray.length + 1);
	}

	public String build() {
		return message.toString();
	}

}
