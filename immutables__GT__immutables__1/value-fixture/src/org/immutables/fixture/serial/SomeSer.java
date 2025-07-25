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
package org.immutables.fixture.serial;

import java.io.Serializable;
import org.immutables.value.Value;

@Value.Modifiable
@Value.Immutable(intern = true)
public abstract class SomeSer implements Serializable {

	private static final long serialVersionUID = 1L;

	@Value.Default
	int regular() {
		return 0;
	}

	@Value.Lazy
	int version() {
		return 1;
	}

	@Value.Modifiable
	@Value.Immutable(singleton = true)
	interface OthSer extends Serializable {
	}
}
