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
package org.immutables.fixture.generics;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.immutables.value.Value;

// Compilation test (and warning for the raw types)
@Value.Immutable
@SuppressWarnings("rawtypes")
@Value.Style(strictBuilder = true)
public interface RawType {
	Set set();

	Map map();

	@SuppressWarnings("CheckReturnValue")
	static void use() {
		ImmutableRawType.builder().set(Collections.emptySet()).map(Collections.emptyMap()).build();
	}
}
