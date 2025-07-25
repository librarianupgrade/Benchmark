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

package org.immutables.criteria.repository.async;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.immutables.criteria.backend.Backend;
import org.immutables.criteria.expression.Expression;
import org.immutables.criteria.expression.ImmutableQuery;
import org.immutables.criteria.expression.Query;
import org.immutables.criteria.matcher.Matchers;
import org.immutables.criteria.matcher.Projection;
import org.immutables.criteria.repository.AbstractReader;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AsyncReader<T> extends AbstractReader<AsyncReader<T>> implements AsyncFetcher.LimitOffset<T> {

	private final ImmutableQuery query;
	private final Backend.Session session;
	private final AsyncFetcherDelegate<T> fetcher;

	AsyncReader(Query query, Backend.Session session) {
		super(query);
		this.query = ImmutableQuery.copyOf(query);
		this.session = Objects.requireNonNull(session, "session");
		this.fetcher = AsyncFetcherDelegate.of(query, session);
	}

	@Override
	protected AsyncReader<T> newReader(Query query) {
		return new AsyncReader<>(query, session);
	}

	public <T1> AsyncMapper1.DistinctLimitOffset<T1> select(Projection<T1> proj1) {
		Query newQuery = this.query.addProjections(Matchers.toExpression(proj1));
		return new AsyncMappers.Mapper1<>(newQuery, session);
	}

	public <T1, T2> AsyncMapper2.DistinctLimitOffset<T1, T2> select(Projection<T1> proj1, Projection<T2> proj2) {
		Query newQuery = this.query.addProjections(Matchers.toExpression(proj1), Matchers.toExpression(proj2));
		return new AsyncMappers.Mapper2<>(newQuery, session);
	}

	public <T1, T2, T3> AsyncMapper3.DistinctLimitOffset<T1, T2, T3> select(Projection<T1> proj1, Projection<T2> proj2,
			Projection<T3> proj3) {
		Query newQuery = this.query.addProjections(Matchers.toExpression(proj1), Matchers.toExpression(proj2),
				Matchers.toExpression(proj3));
		return new AsyncMappers.Mapper3<>(newQuery, session);
	}

	public <T1, T2, T3, T4> AsyncMapper4.DistinctLimitOffset<T1, T2, T3, T4> select(Projection<T1> proj1,
			Projection<T2> proj2, Projection<T3> proj3, Projection<T4> proj4) {
		Query newQuery = this.query.addProjections(Matchers.toExpression(proj1), Matchers.toExpression(proj2),
				Matchers.toExpression(proj3), Matchers.toExpression(proj4));
		return new AsyncMappers.Mapper4<>(newQuery, session);
	}

	public <T1, T2, T3, T4, T5> AsyncMapper5.DistinctLimitOffset<T1, T2, T3, T4, T5> select(Projection<T1> proj1,
			Projection<T2> proj2, Projection<T3> proj3, Projection<T4> proj4, Projection<T5> proj5) {
		Query newQuery = this.query.addProjections(Matchers.toExpression(proj1), Matchers.toExpression(proj2),
				Matchers.toExpression(proj3), Matchers.toExpression(proj4), Matchers.toExpression(proj5));
		return new AsyncMappers.Mapper5<>(newQuery, session);
	}

	public AsyncMapperTuple.DistinctLimitOffset select(Iterable<Projection<?>> projections) {
		Objects.requireNonNull(projections, "projections");
		Preconditions.checkArgument(!Iterables.isEmpty(projections), "empty projections");
		List<Expression> expressions = StreamSupport.stream(projections.spliterator(), false)
				.map(Matchers::toExpression).collect(Collectors.toList());
		Query newQuery = this.query.addProjections(expressions);
		return new AsyncMappers.MapperTuple(newQuery, session);
	}

	@Override
	public CompletionStage<List<T>> fetch() {
		return fetcher.fetch();
	}

	@Override
	public CompletionStage<T> one() {
		return fetcher.one();
	}

	@Override
	public CompletionStage<Optional<T>> oneOrNone() {
		return fetcher.oneOrNone();
	}

	@Override
	public CompletionStage<Boolean> exists() {
		return fetcher.exists();
	}

	@Override
	public CompletionStage<Long> count() {
		return fetcher.count();
	}

	@Override
	public Offset<T> limit(long limit) {
		return newReader(query.withLimit(limit));
	}

	@Override
	public AsyncFetcher<T> offset(long offset) {
		return newReader(query.withOffset(offset));
	}
}
