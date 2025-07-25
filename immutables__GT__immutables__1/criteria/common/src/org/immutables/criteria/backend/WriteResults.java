/*
 * Copyright 2019 Immutables Authors and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.immutables.criteria.backend;

/**
 * Store singleton instances of {@link WriteResult}
 */
final class WriteResults {

	static final ImmutableWriteResult UNKNOWN = ImmutableWriteResult.builder().build();

	static final ImmutableWriteResult EMPTY = ImmutableWriteResult.builder().deletedCount(0).updatedCount(0)
			.insertedCount(0).build();

	private WriteResults() {
	}
}
