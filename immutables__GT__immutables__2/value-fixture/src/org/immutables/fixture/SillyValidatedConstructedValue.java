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
package org.immutables.fixture;

import org.immutables.value.Value;
import static com.google.common.base.Preconditions.*;

@Value.Immutable(builder = false)
public abstract class SillyValidatedConstructedValue {
	@Value.Parameter(order = 0)
	public abstract int value();

	@Value.Parameter(order = 1)
	public abstract boolean negativeOnly();

	@Value.Check
	protected void validate() {
		checkState(!negativeOnly() || value() < 0, "if negativeOnly = true, value should be negative");
	}
}
