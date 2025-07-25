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

package org.immutables.criteria.repository.rxjava2;

import io.reactivex.Single;
import org.immutables.criteria.Criterion;
import org.immutables.criteria.backend.Backend;
import org.immutables.criteria.backend.WriteResult;
import org.immutables.criteria.repository.Updater;
import org.immutables.criteria.repository.reactive.ReactiveWritable;

public class RxJavaWritable<T> implements RxJavaRepository.Writable<T> {

	private final ReactiveWritable<T> writable;

	public RxJavaWritable(Backend.Session session) {
		this.writable = new ReactiveWritable<>(session);
	}

	@Override
	public Single<WriteResult> insertAll(Iterable<? extends T> docs) {
		return Single.fromPublisher(writable.insertAll(docs));
	}

	@Override
	public Single<WriteResult> delete(Criterion<T> criteria) {
		return Single.fromPublisher(writable.delete(criteria));
	}

	@Override
	public Single<WriteResult> upsertAll(Iterable<? extends T> docs) {
		return Single.fromPublisher(writable.upsertAll(docs));
	}

	@Override
	public Single<WriteResult> updateAll(Iterable<? extends T> docs) {
		return Single.fromPublisher(writable.updateAll(docs));
	}

	@Override
	public Updater<T, Single<WriteResult>> update(Criterion<T> criterion) {
		return new RxJavaUpdaterDelegate<>(writable.update(criterion));
	}
}
