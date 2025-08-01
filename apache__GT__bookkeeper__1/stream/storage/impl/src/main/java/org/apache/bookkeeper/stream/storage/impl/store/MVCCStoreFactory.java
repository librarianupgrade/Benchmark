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
package org.apache.bookkeeper.stream.storage.impl.store;

import java.util.concurrent.CompletableFuture;
import org.apache.bookkeeper.statelib.api.mvcc.MVCCAsyncStore;

/**
 * The factory that creates store for each range to store its states.
 */
public interface MVCCStoreFactory extends AutoCloseable {
	/**
	 * Open the state store for storing range <tt>scId</tt>/<tt>streamId</tt>/<tt>rangeId</tt>.
	 *
	 * @param scId     storage container id
	 * @param streamId stream id
	 * @param rangeId  range id
	 * @param ttlSeconds minimum milliseconds to keep records, 0 for "infinite"
	 * @return a state store instance
	 */
	CompletableFuture<MVCCAsyncStore<byte[], byte[]>> openStore(long scId, long streamId, long rangeId, int ttlSeconds);

	/**
	 * Close the provided store and remove it from cache.
	 *
	 * @param scId storage container id
	 * @return future to represent the close result.
	 */
	CompletableFuture<Void> closeStores(long scId);

	@Override
	void close();
}
