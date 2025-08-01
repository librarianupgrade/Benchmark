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
package org.apache.distributedlog.limiter;

/**
 * Simple interface for a rate limiter used by RequestLimiter.
 */
public interface RateLimiter {

	RateLimiter REJECT = new RateLimiter() {
		@Override
		public boolean acquire(int permits) {
			return false;
		}
	};

	RateLimiter ACCEPT = new RateLimiter() {
		@Override
		public boolean acquire(int permits) {
			return true;
		}
	};

	/**
	 * Builder for a rate limiter.
	 */
	abstract class Builder {
		public abstract RateLimiter build();
	}

	/**
	 * Try to acquire a certain number of permits.
	 *
	 * @param permits number of permits to acquire
	 */
	boolean acquire(int permits);
}
