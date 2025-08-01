/*
 * Copyright 2019 Immutables Authors and Contributors
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Objects;

class JsonChecker {

	private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
			.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES) // user-friendly settings to
			.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES) // avoid too much quoting
			.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES); // avoid too much quoting

	private final JsonNode actual;

	private JsonChecker(JsonNode actual) {
		this.actual = Objects.requireNonNull(actual, "actual");
	}

	static JsonChecker of(JsonNode actual) {
		return new JsonChecker(actual);
	}

	private void assertSameJson(JsonNode expected) throws JsonProcessingException {
		Objects.requireNonNull(expected, "expected");
		if (!actual.equals(expected)) {
			ObjectWriter writer = MAPPER.writerWithDefaultPrettyPrinter();
			String expectedPretty = writer.writeValueAsString(expected);
			String actualPretty = writer.writeValueAsString(actual);
			Assertions.assertEquals(expectedPretty, actualPretty);
		}
	}

	void is(String... lines) {
		is(Arrays.asList(lines));
	}

	void is(Iterable<String> lines) {
		try {
			JsonNode expected = expandDots(MAPPER.readTree(String.join("", lines)));
			is(expected);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	void is(JsonNode expected) {
		try {
			assertSameJson(expected);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Expands attributes with dots ({@code .}) into sub-nodes.
	 * Use for more friendly JSON format:
	 *
	 * <pre>
	 *   {'a.b.c': 1}
	 *   expanded to
	 *   {a: { b: {c: 1}}}}
	 * </pre>
	 * @param parent current node
	 * @param <T> type of node (usually JsonNode).
	 * @return copy of existing node with field {@code a.b.c} expanded.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends JsonNode> T expandDots(T parent) {
		Objects.requireNonNull(parent, "parent");

		if (parent.isValueNode()) {
			return parent.deepCopy();
		}

		// ArrayNode
		if (parent.isArray()) {
			ArrayNode arr = (ArrayNode) parent;
			ArrayNode copy = arr.arrayNode();
			arr.elements().forEachRemaining(e -> copy.add(expandDots(e)));
			return (T) copy;
		}

		// ObjectNode
		ObjectNode objectNode = (ObjectNode) parent;
		final ObjectNode copy = objectNode.objectNode();
		objectNode.fields().forEachRemaining(e -> {
			final String property = e.getKey();
			final JsonNode node = e.getValue();

			final String[] names = property.split("\\.");
			ObjectNode copy2 = copy;
			for (int i = 0; i < names.length - 1; i++) {
				copy2 = copy2.with(names[i]);
			}
			copy2.set(names[names.length - 1], expandDots(node));
		});

		return (T) copy;
	}
}
