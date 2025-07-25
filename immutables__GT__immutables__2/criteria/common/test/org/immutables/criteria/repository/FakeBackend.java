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

import io.reactivex.Flowable;
import org.immutables.criteria.backend.Backend;
import org.immutables.criteria.backend.DefaultResult;
import org.immutables.criteria.backend.StandardOperations;
import org.immutables.criteria.expression.Query;
import org.reactivestreams.Publisher;

import java.util.Objects;

/**
 * Backend which returns same result for any operation. It can also perform basic count.
 * Intended for simple tests.
 */
public class FakeBackend implements Backend {

	private final Publisher<?> existing;

	public FakeBackend() {
		this(Flowable.empty());
	}

	public FakeBackend(Publisher<?> existing) {
		this.existing = Objects.requireNonNull(existing, "result");
	}

	@Override
	public Session open(Class<?> entityType) {
		return new Session(entityType);
	}

	private class Session implements Backend.Session {

		private final Class<?> entityType;

		private Session(Class<?> entityType) {
			this.entityType = entityType;
		}

		@Override
		public Class<?> entityType() {
			return entityType;
		}

		@Override
		public Result execute(Operation operation) {
			if (operation instanceof StandardOperations.Select) {
				Query query = ((StandardOperations.Select) operation).query();
				if (query.count()) {
					// just count number of elements
					return DefaultResult.of(Flowable.fromPublisher(existing).count().toFlowable());
				}
			}

			return DefaultResult.of(existing);
		}
	}
}
