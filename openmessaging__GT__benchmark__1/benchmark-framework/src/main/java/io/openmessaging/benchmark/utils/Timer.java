/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.benchmark.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Timer {
	private final long startTime;
	private final Supplier<Long> nanoClock;

	Timer(Supplier<Long> nanoClock) {
		this.nanoClock = nanoClock;
		startTime = this.nanoClock.get();
	}

	public Timer() {
		this(System::nanoTime);
	}

	public double elapsedMillis() {
		return elapsed(TimeUnit.MILLISECONDS);
	}

	public double elapsedSeconds() {
		return elapsed(TimeUnit.SECONDS);
	}

	private double elapsed(TimeUnit unit) {
		long now = nanoClock.get();
		return (now - startTime) / (double) unit.toNanos(1);
	}
}
