/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jn.easyjson.jackson;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jn.easyjson.core.JSON;
import com.jn.easyjson.core.JSONBuilder;
import com.jn.easyjson.core.annotation.DependOn;
import com.jn.easyjson.core.exclusion.ExclusionConfiguration;
import com.jn.easyjson.jackson.deserializer.BooleanDeserializer;
import com.jn.easyjson.jackson.deserializer.Deserializers;
import com.jn.easyjson.jackson.deserializer.EnumDeserializer;
import com.jn.easyjson.jackson.ext.EasyJsonObjectMapper;
import com.jn.easyjson.jackson.serializer.BooleanSerializer;
import com.jn.easyjson.jackson.serializer.EnumSerializer;
import com.jn.langx.annotation.Name;

@Name("jackson")
@DependOn("com.fasterxml.jackson.databind.ObjectMapper")
public class JacksonJSONBuilder extends JSONBuilder {
	private static boolean moduleRegistered = false;
	private static EasyJsonObjectMapper objectMapper = new EasyJsonObjectMapper();

	static {
		makesureEasyJsonBaseModuleRegisted();
	}

	public JacksonJSONBuilder() {
		super();
	}

	public JacksonJSONBuilder(ExclusionConfiguration exclusionConfiguration) {
		super(exclusionConfiguration);
	}

	private static void makesureEasyJsonBaseModuleRegisted() {
		if (!moduleRegistered) {
			synchronized (JacksonJSONBuilder.class) {
				if (!moduleRegistered) {
					SimpleModule module = new SimpleModule();
					SimpleDeserializers simpleDeserializers = new Deserializers();
					module.setDeserializers(simpleDeserializers);

					// boolean
					module.addSerializer(Boolean.class, new BooleanSerializer());
					module.addDeserializer(Boolean.class, new BooleanDeserializer());

					// enum
					module.addDeserializer(Enum.class, new EnumDeserializer<Enum>());
					module.addSerializer(Enum.class, new EnumSerializer<Enum>());

					objectMapper.registerModule(module);
					moduleRegistered = true;
				}
			}
		}
	}

	private void configEnum(EasyJsonObjectMapper objectMapper) {
		// Enum: jackson default priority: ordinal() > toString() > name()
		// Our EnumSerializer priority: ordinal() > toString() > field > name()

		// step 1 : clear old config:
		SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
		serializationConfig = serializationConfig
				.withoutAttribute(JacksonConstants.SERIALIZE_ENUM_USING_FIELD_ATTR_KEY);

		DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig();
		deserializationConfig = deserializationConfig
				.withoutAttribute(JacksonConstants.SERIALIZE_ENUM_USING_INDEX_ATTR_KEY);
		deserializationConfig = deserializationConfig
				.withoutAttribute(JacksonConstants.SERIALIZE_ENUM_USING_FIELD_ATTR_KEY);

		// ordinal()
		objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, serializeEnumUsingValue());
		deserializationConfig = deserializationConfig
				.withAttribute(JacksonConstants.SERIALIZE_ENUM_USING_INDEX_ATTR_KEY, serializeEnumUsingValue());

		// toString()
		objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, serializeEnumUsingToString());
		objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, serializeEnumUsingToString());

		// field
		if (serializeEnumUsingField() != null) {
			serializationConfig = serializationConfig
					.withAttribute(JacksonConstants.SERIALIZE_ENUM_USING_FIELD_ATTR_KEY, serializeEnumUsingField());
			deserializationConfig = deserializationConfig
					.withAttribute(JacksonConstants.SERIALIZE_ENUM_USING_FIELD_ATTR_KEY, serializeEnumUsingField());
		}
		objectMapper.setDescrializationConfig(deserializationConfig);
		objectMapper.setSerializationConfig(serializationConfig);
	}

	private void configBoolean(EasyJsonObjectMapper objectMapper) {
		SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
		serializationConfig = serializationConfig.withAttribute(JacksonConstants.SERIALIZE_BOOLEAN_USING_1_0_ATTR_KEY,
				serializeBooleanUsing1_0());
		serializationConfig = serializationConfig
				.withAttribute(JacksonConstants.SERIALIZE_BOOLEAN_USING_ON_OFF_ATTR_KEY, serializeBooleanUsingOnOff());

		DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig();
		deserializationConfig = deserializationConfig
				.withAttribute(JacksonConstants.SERIALIZE_BOOLEAN_USING_1_0_ATTR_KEY, serializeBooleanUsing1_0());
		deserializationConfig = deserializationConfig
				.withAttribute(JacksonConstants.SERIALIZE_BOOLEAN_USING_ON_OFF_ATTR_KEY, serializeBooleanUsingOnOff());

		objectMapper.setSerializationConfig(serializationConfig);
		objectMapper.setDescrializationConfig(deserializationConfig);
	}

	private void configDate(EasyJsonObjectMapper objectMapper) {
		SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
		serializationConfig = serializationConfig
				.withAttribute(JacksonConstants.SERIALIZE_DATE_USING_DATE_FORMAT_ATTR_KEY, serializeUseDateFormat());
		serializationConfig = serializationConfig.withAttribute(JacksonConstants.SERIALIZE_DATE_USING_PATTERN_ATTR_KEY,
				serializeDateUsingPattern());
		serializationConfig = serializationConfig
				.withAttribute(JacksonConstants.SERIALIZE_DATE_USING_TO_STRING_ATTR_KEY, serializeDateUsingToString());

		DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig();
		deserializationConfig = deserializationConfig
				.withAttribute(JacksonConstants.SERIALIZE_DATE_USING_DATE_FORMAT_ATTR_KEY, serializeUseDateFormat());
		deserializationConfig = deserializationConfig
				.withAttribute(JacksonConstants.SERIALIZE_DATE_USING_PATTERN_ATTR_KEY, serializeDateUsingPattern());
		deserializationConfig = deserializationConfig
				.withAttribute(JacksonConstants.SERIALIZE_DATE_USING_TO_STRING_ATTR_KEY, serializeDateUsingToString());

		objectMapper.setSerializationConfig(serializationConfig);
		objectMapper.setDescrializationConfig(deserializationConfig);
	}

	private void configNumber(EasyJsonObjectMapper objectMapper) {
		SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
		serializationConfig = serializationConfig.withAttribute(JacksonConstants.SERIALIZE_LONG_USING_STRING_ATTR_KEY,
				serializeLongAsString());
		serializationConfig = serializationConfig.withAttribute(JacksonConstants.SERIALIZE_NUMBER_USING_STRING_ATTR_KEY,
				serializeNumberAsString());

		DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig();
		deserializationConfig = deserializationConfig
				.withAttribute(JacksonConstants.SERIALIZE_LONG_USING_STRING_ATTR_KEY, serializeLongAsString());
		deserializationConfig = deserializationConfig
				.withAttribute(JacksonConstants.SERIALIZE_NUMBER_USING_STRING_ATTR_KEY, serializeNumberAsString());

		objectMapper.setSerializationConfig(serializationConfig);
		objectMapper.setDescrializationConfig(deserializationConfig);
	}

	@Override
	public JSON build() {
		makesureEasyJsonBaseModuleRegisted();
		EasyJsonObjectMapper mapper = new EasyJsonObjectMapper(objectMapper);
		mapper.setJsonBuilder(this);

		if (serializeNulls()) {
			//  objectMapper.configure(SerializationFeature);
		}

		configBoolean(mapper);
		configNumber(mapper);
		configEnum(mapper);
		configDate(mapper);

		mapper.configure(SerializationFeature.INDENT_OUTPUT, prettyFormat());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JacksonAdapter jsonHandler = new JacksonAdapter();
		jsonHandler.setObjectMapper(mapper);
		JSON json = new JSON();
		json.setJsonHandler(jsonHandler);
		return json;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		JacksonJSONBuilder result = new JacksonJSONBuilder(this.getExclusionConfiguration());
		this.copyTo(result);
		return result;
	}
}
