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

package org.immutables.criteria;

import org.immutables.criteria.expression.Query;
import org.immutables.criteria.expression.Queryable;
import org.immutables.criteria.matcher.Matchers;

import java.util.Objects;

/**
 * Set of utilities for criterias
 */
public final class Criterias {

	private Criterias() {
	}

	/**
	 * Extracts {@link Query} from a criteria. Any criteria implements (or holds)
	 * {@code Queryable} interface at runtime.
	 */
	public static Query toQuery(Criterion<?> criteria) {
		Objects.requireNonNull(criteria, "criteria");

		if (criteria instanceof Queryable) {
			return ((Queryable) criteria).query();
		}

		return Matchers.extract(criteria).query();
	}

}
