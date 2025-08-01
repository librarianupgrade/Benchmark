/*
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

package org.apache.bookkeeper.bookie;

import java.io.IOException;
import org.apache.bookkeeper.bookie.LedgerStorage.LedgerDeletionListener;
import org.apache.bookkeeper.util.collections.ConcurrentLongHashMap;

class HandleFactoryImpl implements HandleFactory, LedgerDeletionListener {
	private final ConcurrentLongHashMap<LedgerDescriptor> ledgers;
	private final ConcurrentLongHashMap<LedgerDescriptor> readOnlyLedgers;

	final LedgerStorage ledgerStorage;

	HandleFactoryImpl(LedgerStorage ledgerStorage) {
		this.ledgerStorage = ledgerStorage;
		this.ledgers = ConcurrentLongHashMap.<LedgerDescriptor>newBuilder().build();
		this.readOnlyLedgers = ConcurrentLongHashMap.<LedgerDescriptor>newBuilder().build();

		ledgerStorage.registerLedgerDeletionListener(this);
	}

	@Override
	public LedgerDescriptor getHandle(final long ledgerId, final byte[] masterKey) throws IOException, BookieException {
		LedgerDescriptor handle = ledgers.get(ledgerId);

		if (handle == null) {
			handle = LedgerDescriptor.create(masterKey, ledgerId, ledgerStorage);
			ledgers.putIfAbsent(ledgerId, handle);
		}

		handle.checkAccess(masterKey);
		return handle;
	}

	@Override
	public LedgerDescriptor getReadOnlyHandle(final long ledgerId) throws IOException, Bookie.NoLedgerException {
		LedgerDescriptor handle = readOnlyLedgers.get(ledgerId);

		if (handle == null) {
			handle = LedgerDescriptor.createReadOnly(ledgerId, ledgerStorage);
			readOnlyLedgers.putIfAbsent(ledgerId, handle);
		}

		return handle;
	}

	@Override
	public void ledgerDeleted(long ledgerId) {
		ledgers.remove(ledgerId);
		readOnlyLedgers.remove(ledgerId);
	}
}
