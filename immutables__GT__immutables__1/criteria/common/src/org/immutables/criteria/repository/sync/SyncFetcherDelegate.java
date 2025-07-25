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

package org.immutables.criteria.repository.sync;

import com.google.common.base.Preconditions;
import org.immutables.criteria.backend.Backend;
import org.immutables.criteria.expression.ImmutableQuery;
import org.immutables.criteria.expression.Query;
import org.immutables.criteria.repository.Publishers;
import org.immutables.criteria.repository.reactive.ReactiveFetcher;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Implementation of {@link SyncFetcher}. Also has some package-private methods
 * to allow delegation.
 */
class SyncFetcherDelegate<T> implements SyncFetcher<T>, SyncFetcher.DistinctLimitOffset<T> {

	private final ReactiveFetcher<T> fetcher;

	private SyncFetcherDelegate(ReactiveFetcher<T> fetcher) {
		this.fetcher = Objects.requireNonNull(fetcher, "fetcher");
	}

	@Override
	public List<T> fetch() {
		return Publishers.blockingListGet(fetcher.fetch());
	}

	@Override
	public T one() {
		return Publishers.blockingGet(fetcher.one());
	}

	@Override
	public Optional<T> oneOrNone() {
		List<T> list = Publishers.blockingListGet(fetcher.oneOrNone());
		Preconditions.checkState(list.size() <= 1, "Invalid list size: %s", list.size());
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	@Override
	public boolean exists() {
		return Publishers.blockingGet(fetcher.exists());
	}

	@Override
	public long count() {
		return Publishers.blockingGet(fetcher.count());
	}

	@Override
	public LimitOffset<T> distinct() {
		return changeQuery(query -> ImmutableQuery.copyOf(query).withDistinct(true));
	}

	@Override
	public Offset<T> limit(long limit) {
		return changeQuery(query -> ImmutableQuery.copyOf(query).withLimit(limit));
	}

	@Override
	public SyncFetcher<T> offset(long offset) {
		return changeQuery(query -> ImmutableQuery.copyOf(query).withOffset(offset));
	}

	private SyncFetcherDelegate<T> changeQuery(UnaryOperator<Query> fn) {
		return new SyncFetcherDelegate<>(fetcher.changeQuery(fn));
	}

	static <T> SyncFetcherDelegate<T> fromReactive(ReactiveFetcher<T> fetcher) {
		return new SyncFetcherDelegate<T>(fetcher);
	}

	static <T> SyncFetcherDelegate<T> of(Query query, Backend.Session session) {
		return fromReactive(ReactiveFetcher.of(query, session));
	}

}
