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
package org.immutables.fixture;

import org.immutables.value.Value;

/**
 * Test how initialization is generated in a interning and nobuilder
 * combination to avoid regression in having ExceptionInitializationError.
 */
@Value.Immutable(intern = true, copy = true, builder = false)
public interface ProperInitInternNoBuilder {
	@Value.Default
	default int x() {
		return 0;
	}
}
