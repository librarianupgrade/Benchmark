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

import org.immutables.criteria.repository.MapperFunction3;
import org.immutables.criteria.repository.Tuple;

import java.util.function.Function;

public interface SyncMapper3<T1, T2, T3> {

	<R> SyncFetcher<R> map(MapperFunction3<T1, T2, T3, R> mapFn);

	<R> SyncFetcher<R> map(Function<? super Tuple, ? extends R> mapFn);

	interface DistinctLimitOffset<T1, T2, T3> extends LimitOffset<T1, T2, T3> {
		LimitOffset<T1, T2, T3> distinct();
	}

	interface LimitOffset<T1, T2, T3> extends Offset<T1, T2, T3> {
		Offset<T1, T2, T3> limit(long limit);
	}

	interface Offset<T1, T2, T3> extends SyncMapper3<T1, T2, T3> {
		SyncMapper3<T1, T2, T3> offset(long offset);
	}

}
