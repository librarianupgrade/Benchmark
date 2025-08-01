/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.bookkeeper.auth;

import java.util.Objects;

/**
 * A Principal is the user bound to the connection.
 */
public class BookKeeperPrincipal {

	private final String name;

	public static final BookKeeperPrincipal ANONYMOUS = new BookKeeperPrincipal("ANONYMOUS");

	public BookKeeperPrincipal(String name) {
		this.name = name;
	}

	public final String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "BookKeeperPrincipal{" + name + '}';
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + Objects.hashCode(this.name);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BookKeeperPrincipal other = (BookKeeperPrincipal) obj;
		return Objects.equals(this.name, other.name);
	}

}
