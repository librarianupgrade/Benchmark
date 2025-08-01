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

package org.apache.bookkeeper.statelib.api.kv;

import org.apache.bookkeeper.common.annotation.InterfaceAudience.Public;
import org.apache.bookkeeper.common.annotation.InterfaceStability.Evolving;

/**
 * A multi operation that modify the key/value state store.
 */
@Public
@Evolving
public interface KVMulti<K, V> {

	/**
	 * Update the value associated with the provided key.
	 *
	 * @param key   the key to update
	 * @param value the new value to associate with the key.
	 */
	void put(K key, V value);

	/**
	 * Delete the value associated with the provided key.
	 *
	 * @param key the key to delete.
	 */
	void delete(K key);

	/**
	 * Delete the range from <code>key</code> to <code>value</code>.
	 *
	 * @param from the begin key to delete (inclusive)
	 * @param to   the end key to delete (exclusive)
	 */
	void deleteRange(K from, K to);

	/**
	 * Execute the multi operation.
	 */
	void execute();

}
