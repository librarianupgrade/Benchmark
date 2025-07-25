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

import javax.persistence.Convert;
import javax.persistence.Embeddable;

import org.hibernate.annotations.EmbeddableInstantiator;
import org.jmolecules.spring.hibernate.RecordInstantiator;
import org.jmolecules.spring.jpa.JpaAssociationAttributeConverter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

/**
 * Tests for {@link JMoleculesSpringJpaPlugin}.
 *
 * @author Oliver Drotbohm
 */
class JMoleculesSpringJpaPluginTests {

	@Test // #27, #36, #71
	void registersAssociationAttributeConverter() {

		Field field = ReflectionUtils.findField(SampleAggregate.class, "association");
		Convert annotation = field.getDeclaredAnnotation(Convert.class);

		Class<?> converterType = annotation.converter();

		assertThat(converterType.getPackage().getName()).isEqualTo(SampleAggregate.class.getPackage().getName());
		assertThat(JpaAssociationAttributeConverter.class).isAssignableFrom(converterType);

		ResolvableType boundGenerics = ResolvableType.forClass(converterType)
				.as(JpaAssociationAttributeConverter.class);

		assertThat(boundGenerics.getGeneric(0).getRawClass()).isEqualTo(SampleAggregate.class);
		assertThat(boundGenerics.getGeneric(1).getRawClass()).isEqualTo(SampleAggregateIdentifier.class);
		assertThat(boundGenerics.getGeneric(2).getRawClass()).isEqualTo(String.class);
	}

	@Test // #98
	void registersEmbeddableInstantiatorForRecord() {

		assertThat(SampleRecord.class).hasAnnotations(Embeddable.class, EmbeddableInstantiator.class);

		EmbeddableInstantiator annotation = SampleRecord.class.getAnnotation(EmbeddableInstantiator.class);

		assertThat(RecordInstantiator.class).isAssignableFrom(annotation.value());
	}
}
