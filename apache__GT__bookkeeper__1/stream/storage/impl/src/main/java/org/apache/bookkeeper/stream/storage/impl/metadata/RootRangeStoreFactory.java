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
package org.apache.bookkeeper.stream.storage.impl.metadata;

import org.apache.bookkeeper.statelib.api.mvcc.MVCCAsyncStore;
import org.apache.bookkeeper.stream.storage.api.metadata.RootRangeStore;

/**
 * factory to create root range store.
 */
@FunctionalInterface
public interface RootRangeStoreFactory {

	/**
	 * Create a root range store from the provided local <tt>store</tt>.
	 *
	 * @param store local store for storing root range metadata.
	 * @return root range store.
	 */
	RootRangeStore createStore(MVCCAsyncStore<byte[], byte[]> store);

}
