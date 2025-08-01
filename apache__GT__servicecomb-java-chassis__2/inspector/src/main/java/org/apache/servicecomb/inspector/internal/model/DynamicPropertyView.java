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
package org.apache.servicecomb.inspector.internal.model;

public class DynamicPropertyView {
	private String key;

	private String value;

	private String changedTime = "";

	private int callbackCount;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getChangedTime() {
		return changedTime;
	}

	public void setChangedTime(String changedTime) {
		this.changedTime = changedTime;
	}

	public int getCallbackCount() {
		return callbackCount;
	}

	public void setCallbackCount(int callbackCount) {
		this.callbackCount = callbackCount;
	}
}
