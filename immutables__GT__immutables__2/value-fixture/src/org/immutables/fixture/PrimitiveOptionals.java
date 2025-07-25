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

import com.google.common.base.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import org.immutables.value.Value;

@Value.Immutable
public interface PrimitiveOptionals {
	@Value.Parameter
	Optional<Integer> v1();

	@Value.Parameter
	Optional<Double> v2();

	@Value.Parameter
	java.util.Optional<Integer> v3();

	java.util.Optional<Boolean> v4();

	java.util.Optional<String> v5();

	Optional<String> v6();

	OptionalInt v7();

	OptionalDouble v8();

	OptionalLong v9();
}
