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

package org.immutables.criteria.mongo;

import com.google.common.reflect.TypeToken;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonNull;
import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.Decoder;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.immutables.criteria.backend.ExpressionNaming;
import org.immutables.criteria.backend.ProjectedTuple;
import org.immutables.criteria.expression.Expression;
import org.immutables.criteria.expression.Query;
import org.immutables.criteria.mongo.codecs.SimpleRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provider for {@link ProjectedTuple}. Used when there are projections.
 */
class TupleCodecProvider implements CodecProvider {

	private final Query query;
	private final ExpressionNaming naming;

	TupleCodecProvider(Query query, ExpressionNaming naming) {
		this.query = Objects.requireNonNull(query, "query");
		this.naming = Objects.requireNonNull(naming, "naming");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if (clazz == ProjectedTuple.class) {
			return (Codec<T>) new TupleCodec(registry, query, naming);
		}
		return null;
	}

	private static class FieldDecoder {
		private final String mongoField;
		private final Decoder<?> decoder;
		private final boolean isNullable;

		private FieldDecoder(Expression expression, String name, CodecRegistry registry) {
			this.mongoField = name;
			Type type = expression.returnType();
			this.isNullable = !Mongos.isOptional(type);
			this.decoder = SimpleRegistry.of(registry).get(TypeToken.of(type));
		}

		Object decode(BsonValue bson) {
			final Object value;
			if (isNullable && bson.isNull()) {
				// return NULL if field is nullable and BSON is null
				value = null;
			} else if (!bson.isDocument()) {
				value = decoder.decode(new BsonValueReader(bson), DecoderContext.builder().build());
			} else {
				value = decoder.decode(new BsonDocumentReader(bson.asDocument()), DecoderContext.builder().build());
			}
			return value;
		}
	}

	private static class TupleCodec implements Codec<ProjectedTuple> {
		private final Query query;
		private final List<FieldDecoder> decoders;
		private final CodecRegistry registry;

		private TupleCodec(CodecRegistry registry, Query query, ExpressionNaming naming) {
			this.query = query;
			if (query.projections().isEmpty()) {
				throw new IllegalArgumentException(String.format("No projections defined in query %s", query));
			}
			this.registry = Objects.requireNonNull(registry, "registry");
			this.decoders = query.projections().stream().map(p -> new FieldDecoder(p, naming.name(p), registry))
					.collect(Collectors.toList());
		}

		@Override
		public ProjectedTuple decode(BsonReader reader, DecoderContext context) {
			BsonDocument doc = registry.get(BsonDocument.class).decode(reader, context);
			final List<Object> values = new ArrayList<>();
			for (FieldDecoder field : decoders) {
				BsonValue bson = resolveOrNull(doc, Arrays.asList(field.mongoField.split("\\.")));
				values.add(field.decode(bson));
			}

			return ProjectedTuple.of(query.projections(), values);
		}

		private static BsonValue resolveOrNull(BsonValue value, List<String> paths) {
			if (paths.isEmpty()) {
				return value;
			}

			if (!value.isDocument()) {
				return BsonNull.VALUE;
			}

			BsonDocument document = value.asDocument();
			final String first = paths.get(0);
			if (!document.containsKey(first)) {
				return BsonNull.VALUE;
			}

			return resolveOrNull(document.get(first), paths.subList(1, paths.size()));
		}

		@Override
		public void encode(BsonWriter writer, ProjectedTuple value, EncoderContext encoderContext) {
			throw new UnsupportedOperationException(String.format("%s can't encode %s (only decode)",
					getClass().getSimpleName(), getEncoderClass().getName()));
		}

		@Override
		public Class<ProjectedTuple> getEncoderClass() {
			return ProjectedTuple.class;
		}

	}

	/**
	 * Simulates {@link BsonReader} but for a simple (scalar) {@link BsonValue} not {@link BsonDocument}
	 */
	private static class BsonValueReader extends BsonDocumentReader {

		private BsonValueReader(BsonValue value) {
			super(fromValue(value));
			readStartDocument();
			String name = readName(); // value
			if (!name.equals("value")) {
				throw new IllegalStateException(String.format("Expected 'value' got %s", name));
			}
			// now marker should be at BsonValue
		}

		private static BsonDocument fromValue(BsonValue value) {
			return value.isDocument() ? value.asDocument() : new BsonDocument("value", value);
		}
	}

}
