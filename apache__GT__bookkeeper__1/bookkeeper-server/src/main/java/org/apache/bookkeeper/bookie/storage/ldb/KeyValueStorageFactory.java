/**
 *
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
 *
 */
package org.apache.bookkeeper.bookie.storage.ldb;

import java.io.IOException;
import org.apache.bookkeeper.conf.ServerConfiguration;

/**
 * Factory class to create instances of the key-value storage implementation.
 */
public interface KeyValueStorageFactory {

	/**
	 * Enum used to specify different config profiles in the underlying storage.
	 */
	enum DbConfigType {
		Default, // Used for default,command until or test case
		LedgerMetadata, // Used for ledgers db, doesn't need particular configuration
		EntryLocation // Used for location index, lots of writes and much bigger dataset
	}

	KeyValueStorage newKeyValueStorage(String defaultBasePath, String subPath, DbConfigType dbConfigType,
			ServerConfiguration conf) throws IOException;
}
