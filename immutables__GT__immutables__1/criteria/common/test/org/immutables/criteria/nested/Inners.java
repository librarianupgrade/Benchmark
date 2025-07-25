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

package org.immutables.criteria.nested;

import org.immutables.criteria.Criteria;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Criteria generation for inner classes
 */
public class Inners {

	@Value.Immutable
	@Criteria
	@Criteria.Repository
	interface Inner1 {
		// reference a different inner criteria
		Inner2 inner2();

		@Nullable
		Inner2 nullableInner2();

		Optional<Inner2> optionalInner2();

		List<Inner2> listInner2();
	}

	@Value.Immutable
	@Criteria
	@Criteria.Repository
	interface Inner2 {
		String value();
	}
}
