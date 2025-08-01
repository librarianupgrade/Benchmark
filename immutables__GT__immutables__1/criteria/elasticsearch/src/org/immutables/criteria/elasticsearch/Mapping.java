/*
 * Copyright 2019 Immutables Authors and Contributors
 * Copyright 2016-2018 Apache Software Foundation (ASF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.immutables.criteria.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores Elasticsearch
 * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html">
 * mapping</a> information for particular index. This information is
 * extracted from {@code /$index/_mapping} endpoint.
 *
 * <p>Instances of this class are immutable.
 *
 * <p>Some parts of this class have been copied from <a href="https://calcite.apache.org/">Apache Calcite</a> project.
 */
class Mapping {

	private final Map<String, Datatype> mapping;

	private Mapping(final Map<String, String> mapping) {
		Objects.requireNonNull(mapping, "mapping");

		final Map<String, Datatype> transformed = mapping.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> new Datatype(e.getValue())));
		this.mapping = ImmutableMap.copyOf(transformed);
	}

	/**
	 * Create mapping from existing elastic mapping
	 */
	static Mapping ofElastic(final Map<String, String> mapping) {
		return new Mapping(mapping);
	}

	/**
	 * Returns ES schema for each field. Mapping is represented as field name
	 * {@code foo.bar.qux} and type ({@code keyword}, {@code boolean},
	 * {@code long}).
	 *
	 * @return immutable mapping between field and ES type
	 *
	 * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html">Mapping Types</a>
	 */
	Map<String, Datatype> fields() {
		return this.mapping;
	}

	/**
	 * Used as special aggregation key for missing values (documents that are
	 * missing a field).
	 *
	 * <p>Buckets with that value are then converted to {@code null}s in flat
	 * tabular format.
	 *
	 * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-sum-aggregation.html">Missing Value</a>
	 */
	Optional<JsonNode> missingValueFor(String fieldName) {
		if (!fields().containsKey(fieldName)) {
			final String message = String.format(Locale.ROOT, "Field %s not defined", fieldName);
			throw new IllegalArgumentException(message);
		}

		return fields().get(fieldName).missingValue();
	}

	/**
	 * Represents elastic data-type, like {@code long}, {@code keyword},
	 * {@code date} etc.
	 *
	 * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html">Mapping Types</a>
	 */
	static class Datatype {
		private static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;

		// pre-cache missing values
		private static final Set<JsonNode> MISSING_VALUES = Stream.of("string", // for ES2
				"text", "keyword", "date", "long", "integer", "double", "float").map(Datatype::missingValueForType)
				.collect(Collectors.toSet());

		private final String name;
		private final JsonNode missingValue;

		private Datatype(final String name) {
			this.name = Objects.requireNonNull(name, "name");
			this.missingValue = missingValueForType(name);
		}

		/**
		 * Mapping between ES type and json value that represents
		 * {@code missing value} during aggregations. This value can't be
		 * {@code null} and should match type or the field (for ES long type it
		 * also has to be json integer, for date it has to match date format or be
		 * integer (millis epoch) etc.
		 *
		 * <p>It is used for terms aggregations to represent SQL {@code null}.
		 *
		 * @param name name of the type ({@code long}, {@code keyword} ...)
		 *
		 * @return json that will be used in elastic search terms aggregation for
		 * missing value
		 *
		 * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-terms-aggregation.html#_missing_value_13">Missing Value</a>
		 */
		private static @Nullable JsonNode missingValueForType(String name) {
			switch (name) {
			case "string": // for ES2
			case "text":
			case "keyword":
				return FACTORY.textNode("__MISSING__");
			case "long":
				return FACTORY.numberNode(Long.MIN_VALUE);
			case "integer":
				return FACTORY.numberNode(Integer.MIN_VALUE);
			case "short":
				return FACTORY.numberNode(Short.MIN_VALUE);
			case "double":
				return FACTORY.numberNode(Double.MIN_VALUE);
			case "float":
				return FACTORY.numberNode(Float.MIN_VALUE);
			case "date":
				// sentinel for missing dates: 9999-12-31
				final long millisEpoch = LocalDate.of(9999, 12, 31).atStartOfDay().toInstant(ZoneOffset.UTC)
						.toEpochMilli();
				// by default elastic returns dates as longs
				return FACTORY.numberNode(millisEpoch);
			}

			// this is unknown type
			return null;
		}

		/**
		 * Name of the type: {@code text}, {@code integer}, {@code float} etc.
		 */
		String name() {
			return this.name;
		}

		Optional<JsonNode> missingValue() {
			return Optional.ofNullable(missingValue);
		}

		static boolean isMissingValue(JsonNode node) {
			return MISSING_VALUES.contains(node);
		}
	}

}
