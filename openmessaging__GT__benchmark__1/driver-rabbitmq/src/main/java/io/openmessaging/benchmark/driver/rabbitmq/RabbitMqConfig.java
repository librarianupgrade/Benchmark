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

import static io.openmessaging.benchmark.driver.rabbitmq.RabbitMqConfig.QueueType.CLASSIC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RabbitMqConfig {

	public List<String> amqpUris = new ArrayList<>();
	public boolean messagePersistence = false;
	public QueueType queueType = CLASSIC;
	public long producerCreationDelay = 100;
	public int producerCreationBatchSize = 5;
	public long consumerCreationDelay = 100;
	public int consumerCreationBatchSize = 5;

	public enum QueueType {
		CLASSIC {
			@Override
			Map<String, Object> queueOptions() {
				return Collections.emptyMap();
			}
		},
		QUORUM {
			@Override
			Map<String, Object> queueOptions() {
				return Collections.singletonMap("x-queue-type", "quorum");
			}
		},
		STREAM {
			@Override
			Map<String, Object> queueOptions() {
				return Collections.singletonMap("x-queue-type", "stream");
			}
		};

		abstract Map<String, Object> queueOptions();
	}
}
