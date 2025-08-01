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
package io.openmessaging.benchmark.driver.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QueueTypeTest {

	@Test
	public void classic() {
		assertThat(RabbitMqConfig.QueueType.CLASSIC.queueOptions()).isEmpty();
	}

	@Test
	public void quorum() {
		assertThat(RabbitMqConfig.QueueType.QUORUM.queueOptions()).satisfies(o -> {
			assertThat(o).containsEntry("x-queue-type", "quorum");
			assertThat(o).hasSize(1);
		});
	}
}
