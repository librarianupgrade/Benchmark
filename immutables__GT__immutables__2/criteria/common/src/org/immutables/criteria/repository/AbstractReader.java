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

package org.immutables.criteria.repository;

import org.immutables.criteria.expression.Collation;
import org.immutables.criteria.expression.Expression;
import org.immutables.criteria.expression.ImmutableQuery;
import org.immutables.criteria.expression.Ordering;
import org.immutables.criteria.expression.Query;
import org.immutables.criteria.matcher.Matchers;
import org.immutables.criteria.matcher.Projection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Template class for Readers. For future API compatibility prefer extend it rather than
 * implement {@link Reader} directly.s
 */
public abstract class AbstractReader<R extends Reader<R>> implements Reader<R> {

	private final ImmutableQuery query;

	protected AbstractReader(Query query) {
		Objects.requireNonNull(query, "query");
		this.query = ImmutableQuery.copyOf(query);
	}

	protected abstract R newReader(Query query);

	/**
	 * Expose current query (used mostly for SPIs)
	 */
	Query query() {
		return query;
	}

	@Override
	public R orderBy(Ordering first, Ordering... rest) {
		if (!query.collations().isEmpty()) {
			throw new IllegalStateException("OrderBy was already set");
		}

		final List<Ordering> orderings = new ArrayList<>();
		orderings.add(first);
		orderings.addAll(Arrays.asList(rest));

		final List<Collation> collations = orderings.stream().map(x -> (Collation) x).collect(Collectors.toList());
		return newReader(query.addCollations(collations));
	}

	@Override
	public R groupBy(Projection<?> first, Projection<?>... rest) {
		if (!query.groupBy().isEmpty()) {
			throw new IllegalStateException("GroupBy was already set");
		}

		final List<Projection<?>> all = new ArrayList<>();
		all.add(first);
		all.addAll(Arrays.asList(rest));

		final List<Expression> groupBy = all.stream().map(Matchers::toExpression).collect(Collectors.toList());
		return newReader(query.addGroupBy(groupBy));
	}

}
