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

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author wenshao[szujobs@hotmail.com]
 */
public class PrimitiveArraySerializer implements ObjectSerializer {

	public static PrimitiveArraySerializer instance = new PrimitiveArraySerializer();

	public final void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException {
		SerializeWriter out = serializer.out;

		if (object == null) {
			out.writeNull(SerializerFeature.WriteNullListAsEmpty);
			return;
		}

		if (object instanceof int[]) {
			int[] array = (int[]) object;
			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}
				out.writeInt(array[i]);
			}
			out.write(']');
			return;
		}

		if (object instanceof short[]) {
			short[] array = (short[]) object;
			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}
				out.writeInt(array[i]);
			}
			out.write(']');
			return;
		}

		if (object instanceof long[]) {
			long[] array = (long[]) object;

			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}
				out.writeLong(array[i]);
			}
			out.write(']');
			return;
		}

		if (object instanceof boolean[]) {
			boolean[] array = (boolean[]) object;
			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}
				out.write(array[i]);
			}
			out.write(']');
			return;
		}

		if (object instanceof float[]) {
			float[] array = (float[]) object;
			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}

				float item = array[i];
				if (Float.isNaN(item)) {
					out.writeNull();
				} else {
					out.append(Float.toString(item));
				}
			}
			out.write(']');
			return;
		}

		if (object instanceof double[]) {
			double[] array = (double[]) object;
			out.write('[');
			for (int i = 0; i < array.length; ++i) {
				if (i != 0) {
					out.write(',');
				}

				double item = array[i];
				if (Double.isNaN(item)) {
					out.writeNull();
				} else {
					out.append(Double.toString(item));
				}
			}
			out.write(']');
			return;
		}

		if (object instanceof byte[]) {
			byte[] array = (byte[]) object;
			out.writeByteArray(array);
			return;
		}

		char[] chars = (char[]) object;
		out.writeString(chars);
	}
}
