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
package org.apache.struts2.util;

/**
 * Entry in a list.
 *
 */
public class ListEntry {

	final private Object key;
	final private Object value;
	final private boolean isSelected;

	public ListEntry(Object key, Object value, boolean isSelected) {
		this.key = key;
		this.value = value;
		this.isSelected = isSelected;
	}

	public boolean getIsSelected() {
		return isSelected;
	}

	public Object getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}
}
