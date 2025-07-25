/*
   Copyright 2015 Immutables Authors and Contributors

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

import org.immutables.value.Value;

public class AbstractToStringHashCode {

	@Value.Immutable
	static abstract class AbstractHashCode {
		@Override
		public abstract int hashCode();
	}

	@Value.Immutable
	static abstract class AbstractToString {
		@Override
		public abstract String toString();
	}

	@Value.Immutable(intern = true)
	static abstract class InternCustomHashCode {
		abstract int a();

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof InternCustomHashCode;
		}
	}

	@Value.Immutable
	static abstract class CustomEquals {
		abstract int a();

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof CustomEquals;
		}
	}
}
