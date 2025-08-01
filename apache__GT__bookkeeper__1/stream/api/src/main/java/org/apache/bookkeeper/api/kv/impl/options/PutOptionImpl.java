/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.bookkeeper.api.kv.impl.options;

import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.bookkeeper.api.kv.options.PutOption;

@Accessors(fluent = true)
@Getter
@ToString(exclude = "handle")
class PutOptionImpl<K> implements PutOption<K> {

	static <K> PutOptionImpl<K> create(Recycler<PutOptionImpl<K>> recycler) {
		return recycler.get();
	}

	private final Handle<PutOptionImpl<K>> handle;

	private boolean prevKv;

	PutOptionImpl(Handle<PutOptionImpl<K>> handle) {
		this.handle = handle;
	}

	void set(PutOptionBuilderImpl<K> builderImpl) {
		this.prevKv = builderImpl.prevKv();
	}

	@Override
	public void close() {
		this.prevKv = false;

		handle.recycle(this);
	}
}
