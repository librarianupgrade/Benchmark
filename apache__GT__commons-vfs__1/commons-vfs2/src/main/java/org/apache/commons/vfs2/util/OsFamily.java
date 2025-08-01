/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.util;

import org.apache.commons.lang3.SystemUtils;

/**
 * An enumerated type, which represents an OS family.
 *
 * @deprecated Use Apache Commons Lang's {@link SystemUtils}. Remove in 3.0.
 */
@Deprecated
public final class OsFamily {

	static final OsFamily[] EMPTY_OS_FAMILY_ARRAY = {};
	private final String name;
	private final OsFamily[] families;

	OsFamily(final String name) {
		this.name = name;
		families = EMPTY_OS_FAMILY_ARRAY;
	}

	OsFamily(final String name, final OsFamily[] families) {
		this.name = name;
		this.families = families;
	}

	/**
	 * Returns the OS families that this family belongs to.
	 *
	 * @return an array of OSFamily objects that this family belongs to.
	 */
	public OsFamily[] getFamilies() {
		return families;
	}

	/**
	 * Returns the name of this family.
	 *
	 * @return The name of this family.
	 */
	public String getName() {
		return name;
	}
}
