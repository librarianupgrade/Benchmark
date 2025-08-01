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

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;
import org.immutables.value.Value;

// Compilation test for optinal which are not considered optional for generation
// for #502
@Value.Immutable
public abstract class OptionalNonOptional {
	@Value.Default
	public Optional<Path> getStubBinaryPath() {
		return Optional.empty();
	}

	public abstract com.google.common.base.Optional<? extends Path> stubBinaryPath2();

	public abstract @Nullable Optional<Path> stubBinaryPath3();
}
