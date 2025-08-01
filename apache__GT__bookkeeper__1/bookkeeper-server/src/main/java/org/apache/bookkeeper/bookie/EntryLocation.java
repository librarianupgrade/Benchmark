/*
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

package org.apache.bookkeeper.bookie;

/**
 * An {@code EntryLocation} represents the location where an entry is stored.
 */
public class EntryLocation {
	public final long ledger;
	public final long entry;
	public final long location;

	public EntryLocation(long ledger, long entry, long location) {
		this.ledger = ledger;
		this.entry = entry;
		this.location = location;
	}

	public long getLedger() {
		return ledger;
	}

	public long getEntry() {
		return entry;
	}

	public long getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return new StringBuilder().append("EntryLocation{").append("ledger=").append(ledger).append(",entry=")
				.append(entry).append(",locationLog=").append(location >> 32 & 0xFFFFFFFF).append(",locationOffset=")
				.append((int) (location & 0xFFFFFFFF)).append("}").toString();
	}
}
