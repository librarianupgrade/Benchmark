/*
 * Copyright 2009 Jan Ouwens
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.jqno.equalsverifier.testhelpers.types;

public class Multiple {
	private final int a;
	private final int b;

	public Multiple(int a, int b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public final boolean equals(Object obj) {
		if (!(obj instanceof Multiple)) {
			return false;
		}
		Multiple other = (Multiple) obj;
		return a * b == other.a * other.b;
	}

	@Override
	public final int hashCode() {
		return a * b;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + a + "*" + b + "=" + (a * b);
	}
}
