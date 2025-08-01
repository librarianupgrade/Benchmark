/*
   Copyright 2013-2018 Immutables Authors and Contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.immutables.ordinal;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

class Domain extends OrdinalDomain<Ord> {
	private final LoadingCache<Integer, Ord> values = CacheBuilder.newBuilder().build(new CacheLoader<Integer, Ord>() {
		@Override
		public Ord load(Integer key) {
			return new Ord(Domain.this, key);
		}
	});

	@Override
	public Ord get(int ordinal) {
		return values.getUnchecked(ordinal);
	}

	@Override
	public int length() {
		return values.asMap().size();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).addValue(length()).toString();
	}
}
