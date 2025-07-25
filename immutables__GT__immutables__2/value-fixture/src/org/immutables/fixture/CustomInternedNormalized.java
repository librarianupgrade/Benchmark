/*
   Copyright 2018 Immutables Authors and Contributors

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
package org.immutables.fixture;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.immutables.value.Value;

// This tests proper inheritance of normalization check and generic variable substitution
abstract class BaseInterned<T extends BaseInterned<T>> {
	private static final Interner<Object> INTERNER = Interners.newStrongInterner();

	@SuppressWarnings("unchecked")
	@Value.Check
	protected T intern() {
		return (T) INTERNER.intern(this);
	}
}

@Value.Immutable
public abstract class CustomInternedNormalized extends BaseInterned<CustomInternedNormalized> {
	abstract int a();

	abstract int b();
}
