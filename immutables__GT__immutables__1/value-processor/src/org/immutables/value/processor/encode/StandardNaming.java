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
package org.immutables.value.processor.encode;

public enum StandardNaming {
	NONE("*"), GET("*"), INIT("*"), WITH("with*"), ADD("add*", true), ADD_ALL("addAll*"), PUT("put*", true),
	PUT_ALL("putAll*");

	public final String pattern;
	public final boolean depluralize;

	StandardNaming(String pattern) {
		this(pattern, false);
	}

	StandardNaming(String pattern, boolean depluralize) {
		this.pattern = pattern;
		this.depluralize = depluralize;
	}
}
