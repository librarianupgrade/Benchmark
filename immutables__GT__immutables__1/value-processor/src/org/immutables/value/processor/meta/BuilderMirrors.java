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
package org.immutables.value.processor.meta;

import org.immutables.mirror.Mirror;

public final class BuilderMirrors {
	private BuilderMirrors() {
	}

	@Mirror.Annotation("org.immutables.builder.Builder.AccessibleFields")
	public @interface AccessibleFields {
	}

	@Mirror.Annotation("org.immutables.builder.Builder.Factory")
	public @interface Factory {
	}

	@Mirror.Annotation("org.immutables.builder.Builder.Constructor")
	public @interface FConstructor {
	}

	@Mirror.Annotation("org.immutables.builder.Builder.Include")
	public @interface FInclude {
		Class<?>[] value();
	}

	@Mirror.Annotation("org.immutables.builder.Builder.Parameter")
	public @interface FParameter {
	}

	@Mirror.Annotation("org.immutables.builder.Builder.Switch")
	public @interface Switch {
		String defaultName() default "";
	}
}
