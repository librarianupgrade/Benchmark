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
package org.immutables.fixture.style;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.BuilderVisibility;
import org.immutables.value.Value.Style.ImplementationVisibility;

interface NestingClassOrBuilder {
	@Value.Immutable
	@Value.Style(visibility = ImplementationVisibility.PACKAGE, overshadowImplementation = true, implementationNestedInBuilder = true, builder = "new")
	interface ImplNestedInBuild {
	}

	@Value.Immutable
	@Value.Style(visibility = ImplementationVisibility.PUBLIC, builderVisibility = BuilderVisibility.PACKAGE)
	interface NonPublicBuild {
	}

	@SuppressWarnings("CheckReturnValue")
	default void use() {
		ImplNestedInBuildBuilder builder = new ImplNestedInBuildBuilder();
		ImplNestedInBuild abstractValue = builder.build();
		abstractValue.getClass();

		ImmutableNonPublicBuild.Builder packagePrivateBuilder = ImmutableNonPublicBuild.builder();
		ImmutableNonPublicBuild immutable = packagePrivateBuilder.build();
		immutable.getClass();
	}
}
