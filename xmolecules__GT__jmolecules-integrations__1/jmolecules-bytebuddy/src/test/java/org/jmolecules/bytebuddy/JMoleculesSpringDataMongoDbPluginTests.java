/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jmolecules.bytebuddy;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Oliver Drotbohm
 */
class JMoleculesSpringDataMongoDbPluginTests {

	@Test
	void defaultsAggregateType() throws Exception {

		// Defaults @Entity
		assertThat(SampleAggregate.class.getDeclaredAnnotations())
				.filteredOn(it -> it.annotationType().equals(Document.class)).hasSize(1);

		// Defaults @Id
		assertThat(SampleAggregate.class.getDeclaredField("id").getAnnotations())
				.filteredOn(it -> it.annotationType().equals(Id.class)).hasSize(1);
	}
}
