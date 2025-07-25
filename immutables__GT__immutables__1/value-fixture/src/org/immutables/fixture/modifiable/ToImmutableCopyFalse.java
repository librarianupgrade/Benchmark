/*
   Copyright 2016 Immutables Authors and Contributors

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
package org.immutables.fixture.modifiable;

import org.immutables.value.Value;

// Compilation test to have toImmutable on modifiable companion class when copy = false
public interface ToImmutableCopyFalse {
	@Value.Modifiable
	@Value.Immutable(copy = false)
	public interface A {

		@SuppressWarnings("CheckReturnValue")
		default void use() {
			ModifiableA.create().toImmutable();
		}
	}

	@Value.Modifiable
	@Value.Immutable(copy = false)
	@Value.Style(strictBuilder = true)
	public interface B {

		@SuppressWarnings("CheckReturnValue")
		default void use() {
			ModifiableB.create().toImmutable();
		}
	}

	@Value.Modifiable
	@Value.Immutable(copy = false, builder = false)
	public interface C {
		@Value.Parameter
		int c();

		@SuppressWarnings("CheckReturnValue")
		default void use() {
			ModifiableC.create().toImmutable();
		}
	}
}
