/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.bookkeeper.common.router;

import org.apache.bookkeeper.common.hash.Murmur3;

/**
 * Hash router that computes the hash value of a byte array.
 */
public class BytesHashRouter implements HashRouter<byte[]> {

	public static BytesHashRouter of() {
		return ROUTER;
	}

	private static final BytesHashRouter ROUTER = new BytesHashRouter();

	private BytesHashRouter() {
	}

	@Override
	public Long getRoutingKey(byte[] key) {
		return Murmur3.hash128(key, 0, key.length, AbstractHashRouter.HASH_SEED)[0];
	}
}
