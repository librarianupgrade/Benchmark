/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.distributedlog.lock;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

class DistributedLockContext {
	private final Set<Pair<String, Long>> lockIds;

	DistributedLockContext() {
		this.lockIds = new HashSet<Pair<String, Long>>();
	}

	synchronized void addLockId(Pair<String, Long> lockId) {
		this.lockIds.add(lockId);
	}

	synchronized void clearLockIds() {
		this.lockIds.clear();
	}

	synchronized boolean hasLockId(Pair<String, Long> lockId) {
		return this.lockIds.contains(lockId);
	}
}
