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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class TimerTest {

	@Test
	void elapsedMillis() {
		Supplier<Long> mockClock = mock(Supplier.class);
		when(mockClock.get()).thenReturn(MILLISECONDS.toNanos(1), MILLISECONDS.toNanos(3));
		Timer timer = new Timer(mockClock);
		assertThat(timer.elapsedMillis()).isEqualTo(2.0d);
	}

	@Test
	void elapsedSeconds() {
		Supplier<Long> mockClock = mock(Supplier.class);
		when(mockClock.get()).thenReturn(SECONDS.toNanos(1), SECONDS.toNanos(3));
		Timer timer = new Timer(mockClock);
		assertThat(timer.elapsedSeconds()).isEqualTo(2.0d);
	}
}
