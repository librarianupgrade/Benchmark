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
package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.TypeUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author wenshao[szujobs@hotmail.com]
 */
public class FloatCodec implements ObjectSerializer, ObjectDeserializer {
	public static FloatCodec instance = new FloatCodec();
	private NumberFormat decimalFormat;

	public FloatCodec() {

	}

	public FloatCodec(DecimalFormat decimalFormat) {
		this.decimalFormat = decimalFormat;
	}

	public FloatCodec(String decimalFormat) {
		this(new DecimalFormat(decimalFormat));
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialze(DefaultJSONParser parser) {
		final JSONLexer lexer = parser.lexer;

		if (lexer.token() == JSONToken.LITERAL_INT) {
			String val = lexer.numberString();
			lexer.nextToken(JSONToken.COMMA);
			return (T) Float.valueOf(Float.parseFloat(val));
		}

		if (lexer.token() == JSONToken.LITERAL_FLOAT) {
			float val = lexer.floatValue();
			lexer.nextToken(JSONToken.COMMA);
			return (T) Float.valueOf(val);
		}

		Object value = parser.parse();

		if (value == null) {
			return null;
		}

		return (T) TypeUtils.castToFloat(value);
	}

	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException {
		SerializeWriter out = serializer.out;

		if (object == null) {
			out.writeNull(SerializerFeature.WriteNullNumberAsZero);
			return;
		}

		float floatValue = ((Float) object).floatValue();
		if (decimalFormat != null) {
			String floatText = decimalFormat.format(floatValue);
			out.write(floatText);
		} else {
			out.writeFloat(floatValue, true);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
		try {
			return (T) deserialze(parser);
		} catch (Exception ex) {
			throw new JSONException("parseLong error, field : " + fieldName, ex);
		}
	}

	public int getFastMatchToken() {
		return JSONToken.LITERAL_INT;
	}
}
