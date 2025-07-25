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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.util.ReflectionUtils;

/**
 * Tests for {@link JMoleculesSpringDataJpaPlugin}.
 *
 * @author Oliver Drotbohm
 */
class JMoleculesSpringDataJpaTests {

	SampleAggregateIdentifier id = new SampleAggregateIdentifier();
	Persistable<?> persistable = (Persistable<?>) new SampleAggregate(id);

	@Test // #28
	void implementsPersistable() {
		assertThat(Persistable.class).isAssignableFrom(SampleAggregate.class);
	}

	@Test // #28
	void generatesEqualsAndHashCodeBasedOnId() {

		SampleAggregateIdentifier id = new SampleAggregateIdentifier();
		SampleAggregateIdentifier anotherId = new SampleAggregateIdentifier();
		SampleAggregate left = new SampleAggregate(id);
		SampleAggregate right = new SampleAggregate(id);
		SampleAggregate other = new SampleAggregate(anotherId);

		assertThat(left).isEqualTo(right);
		assertThat(right).isEqualTo(left);
		assertThat(left).isNotEqualTo(other);
		assertThat(other).isNotEqualTo(left);

		assertThat(left.hashCode()).isEqualTo(right.hashCode());
		assertThat(left.hashCode()).isNotEqualTo(other.hashCode());
	}

	@Test // #28
	void exposesIdViaPersistable() {
		assertThat(persistable.getId()).isEqualTo(id);
	}

	@Test // #28
	void registersIsNewFieldAndDefaultsThatToTrue() {

		Field isNewField = ReflectionUtils.findField(SampleAggregate.class, PersistableImplementor.IS_NEW_FIELD);
		assertThat(isNewField).isNotNull();
		assertThat(isNewField.getAnnotation(Transient.class)).isNotNull();

		ReflectionUtils.makeAccessible(isNewField);
		assertThat(ReflectionUtils.getField(isNewField, persistable)).isEqualTo(true);
	}

	@Test // #28
	void exposesLifecycleMethodToMarkPersistableAsNotNew() {

		assertThat(Arrays.stream(SampleAggregate.class.getDeclaredMethods())
				.filter(it -> Stream.of(PostLoad.class, PrePersist.class)
						.anyMatch(annotation -> AnnotatedElementUtils.hasAnnotation(it, annotation))))
				.isNotEmpty().allSatisfy(it -> {

					SampleAggregateIdentifier id = new SampleAggregateIdentifier();
					Persistable<?> persistable = (Persistable<?>) new SampleAggregate(id);

					assertThat(persistable.isNew()).isTrue();

					ReflectionUtils.makeAccessible(it);
					ReflectionUtils.invokeMethod(it, persistable);

					assertThat(persistable.isNew()).isFalse();
				});
	}

	@Test // #77
	void forwardsIsNewStateForWithers() {

		Field isNewField = ReflectionUtils.findField(SampleAggregate.class, PersistableImplementor.IS_NEW_FIELD);
		ReflectionUtils.makeAccessible(isNewField);

		SampleAggregate aggregate = new SampleAggregate(new SampleAggregateIdentifier());
		ReflectionUtils.setField(isNewField, aggregate, false);

		SampleAggregate result = aggregate.wither();

		assertThat(result).isNotSameAs(aggregate);
		assertThat(ReflectionUtils.getField(isNewField, result)).isEqualTo(false);
	}
}
