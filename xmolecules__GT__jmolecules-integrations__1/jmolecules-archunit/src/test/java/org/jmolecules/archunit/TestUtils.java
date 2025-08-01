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
package org.jmolecules.archunit;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * @author Oliver Drotbohm
 */
class TestUtils {

	static Consumer<String> violation(Class<?> from, Class<?> to) {

		return value -> {

			int fromIndex = value.indexOf(from.getName());

			assertThat(fromIndex).isNotEqualTo(-1).describedAs("Did not find source type %s.", from.getName());
			assertThat(value.indexOf(to.getName())).isGreaterThan(fromIndex).describedAs("Did not find target type %s.",
					to.getName());
		};
	}

	static Consumer<String> violation(String template, Object... arguments) {

		String expanded = String.format(template, arguments);

		return value -> {
			assertThat(value).matches(expanded);
		};
	}

	static Consumer<String> violation(Class<?> source, String field, Class<?> expected, @Nullable Class<?> actual) {

		return violation("Field.*%s\\.%s.*%s.*%s.*", //
				source.getSimpleName(), field, expected.getSimpleName(), actual == null ? "" : actual.getSimpleName());
	}

	static String abbreviate(Class<?> type) {

		String abbreviatedPackage = Stream //
				.of(type.getPackage().getName().split("\\.")) //
				.map(it -> it.substring(0, 1)) //
				.collect(Collectors.joining("."));

		return abbreviatedPackage.concat(".") //
				.concat(ClassUtils.getShortName(type.getName()));
	}
}
