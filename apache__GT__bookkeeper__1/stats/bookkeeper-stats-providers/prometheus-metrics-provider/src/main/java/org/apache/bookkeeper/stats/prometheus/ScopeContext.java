/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.bookkeeper.stats.prometheus;

import java.util.Map;
import java.util.Objects;

/**
 * Holder for a scope and a set of associated labels.
 */
public class ScopeContext {
	private final String scope;
	private final Map<String, String> labels;

	public ScopeContext(String scope, Map<String, String> labels) {
		this.scope = scope;
		this.labels = labels;
	}

	public String getScope() {
		return scope;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ScopeContext that = (ScopeContext) o;
		return Objects.equals(scope, that.scope) && Objects.equals(labels, that.labels);
	}

	@Override
	public int hashCode() {
		return Objects.hash(scope, labels);
	}
}
