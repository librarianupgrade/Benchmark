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

import org.junit.jupiter.api.Test;
import static org.immutables.check.Checkers.check;

public class CustomBaseInternedTest {
	@Test
	public void customInternedNormalization() {
		CustomInternedNormalized a1 = ImmutableCustomInternedNormalized.builder().a(1).b(2).build();

		CustomInternedNormalized a2 = ImmutableCustomInternedNormalized.builder().a(1).b(2).build();

		check(a1).same(a2);
	}
}
