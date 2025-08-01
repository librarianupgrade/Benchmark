/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.foundation.common.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;

public final class AsyncUtils {
	private AsyncUtils() {
	}

	public static <T> CompletableFuture<T> tryCatchSupplier(Supplier<T> supplier) {
		try {
			T value = supplier.get();
			return CompletableFuture.completedFuture(value);
		} catch (Throwable e) {
			return completeExceptionally(e);
		}
	}

	public static <T> CompletableFuture<T> tryCatchSupplierFuture(Supplier<CompletableFuture<T>> supplier) {
		try {
			return supplier.get();
		} catch (Throwable e) {
			return completeExceptionally(e);
		}
	}

	public static <T> CompletableFuture<T> completeExceptionally(Throwable throwable) {
		CompletableFuture<T> future = new CompletableFuture<>();
		future.completeExceptionally(throwable);
		return future;
	}

	/**
	 * throws {@code exception} as RuntimeException.
	 *
	 * @param exception exception which will be rethrow
	 */
	public static RuntimeException rethrow(Throwable exception) {
		if (exception instanceof RuntimeException) {
			return (RuntimeException) exception;
		}
		return new ServiceCombException("Not declared exception", exception);
	}

	public static <T> T toSync(CompletableFuture<T> future) {
		try {
			return future.get();
		} catch (ExecutionException executionException) {
			throw rethrow(executionException.getCause());
		} catch (Throwable e) {
			throw rethrow(e);
		}
	}
}
