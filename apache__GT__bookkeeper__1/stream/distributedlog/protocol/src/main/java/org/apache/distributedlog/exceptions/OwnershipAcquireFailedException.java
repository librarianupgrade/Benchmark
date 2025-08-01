/**
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
package org.apache.distributedlog.exceptions;

/**
 * Exception is thrown when a log writer attempt to acquire a lock.
 *
 * <p>It is typically thrown when the lock is already acquired by another writer.
 */
public class OwnershipAcquireFailedException extends LockingException {
	private static final long serialVersionUID = 8176056926552748001L;
	private final String currentOwner;

	public OwnershipAcquireFailedException(String lockPath, String currentOwner) {
		super(StatusCode.FOUND, lockPath,
				String.format("Lock acquisition failed, the current owner is %s", currentOwner));
		this.currentOwner = currentOwner;
	}

	public String getCurrentOwner() {
		return currentOwner;
	}
}
