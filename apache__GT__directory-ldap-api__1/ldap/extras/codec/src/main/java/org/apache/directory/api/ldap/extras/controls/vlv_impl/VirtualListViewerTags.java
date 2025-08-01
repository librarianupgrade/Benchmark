/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.apache.directory.api.ldap.extras.controls.vlv_impl;

/**
 * Tags used for decoding VirtualListViewerRequest.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public enum VirtualListViewerTags {
	/** byOffset [0] */
	BY_OFFSET_TAG(0xA0),

	/** greaterThanOrEqual [1] */
	ASSERTION_VALUE_TAG(0x81);

	/** Internal value for each tag */
	private int value;

	VirtualListViewerTags(int value) {
		this.value = value;
	}

	/**
	 * @return The interned value
	 */
	public int getValue() {
		return value;
	}
}
