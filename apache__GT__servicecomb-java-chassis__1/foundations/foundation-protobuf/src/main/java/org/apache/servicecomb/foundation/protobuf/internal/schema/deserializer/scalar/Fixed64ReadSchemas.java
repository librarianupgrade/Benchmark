/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar;

import java.io.IOException;

import org.apache.servicecomb.foundation.common.utils.bean.LongSetter;
import org.apache.servicecomb.foundation.protobuf.internal.ProtoUtils;
import org.apache.servicecomb.foundation.protobuf.internal.bean.PropertyDescriptor;
import org.apache.servicecomb.foundation.protobuf.internal.schema.deserializer.scalar.AbstractScalarReadSchemas.AbstractLongSchema;

import com.fasterxml.jackson.databind.JavaType;

import io.protostuff.InputEx;
import io.protostuff.compiler.model.Field;
import io.protostuff.runtime.FieldSchema;

public class Fixed64ReadSchemas {
	public static <T> FieldSchema<T> create(Field protoField, PropertyDescriptor propertyDescriptor) {
		JavaType javaType = propertyDescriptor.getJavaType();
		if (long.class.equals(javaType.getRawClass())) {
			return new Fixed64PrimitiveSchema<>(protoField, propertyDescriptor);
		}

		if (Long.class.equals(javaType.getRawClass()) || javaType.isJavaLangObject()) {
			return new Fixed64Schema<>(protoField, propertyDescriptor);
		}

		ProtoUtils.throwNotSupportMerge(protoField, propertyDescriptor.getJavaType());
		return null;
	}

	private static class Fixed64Schema<T> extends AbstractLongSchema<T> {
		public Fixed64Schema(Field protoField, PropertyDescriptor propertyDescriptor) {
			super(protoField, propertyDescriptor);
		}

		@Override
		public int mergeFrom(InputEx input, T message) throws IOException {
			long value = input.readFixed64();
			setter.set(message, value);
			return input.readFieldNumber();
		}
	}

	private static class Fixed64PrimitiveSchema<T> extends FieldSchema<T> {
		protected final LongSetter<T> setter;

		public Fixed64PrimitiveSchema(Field protoField, PropertyDescriptor propertyDescriptor) {
			super(protoField, propertyDescriptor.getJavaType());
			this.setter = propertyDescriptor.getSetter();
		}

		@Override
		public int mergeFrom(InputEx input, T message) throws IOException {
			long value = input.readFixed64();
			setter.set(message, value);
			return input.readFieldNumber();
		}
	}
}
