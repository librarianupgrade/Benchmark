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

package com.jn.easyjson.core.util;

import java.io.ObjectStreamException;
import java.math.BigDecimal;

public final class LazilyParsedNumber extends Number {
	private final String value;

	/**
	 * @param value must not be null
	 */
	public LazilyParsedNumber(String value) {
		this.value = value;
	}

	@Override
	public int intValue() {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			try {
				return (int) Long.parseLong(value);
			} catch (NumberFormatException nfe) {
				return new BigDecimal(value).intValue();
			}
		}
	}

	@Override
	public long longValue() {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return new BigDecimal(value).longValue();
		}
	}

	@Override
	public float floatValue() {
		return Float.parseFloat(value);
	}

	@Override
	public double doubleValue() {
		return Double.parseDouble(value);
	}

	@Override
	public String toString() {
		return value;
	}

	/**
	 * If somebody is unlucky enough to have to serialize one of these, serialize
	 * it as a BigDecimal so that they won't need Gson on the other side to
	 * deserialize it.
	 */
	private Object writeReplace() throws ObjectStreamException {
		return new BigDecimal(value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof LazilyParsedNumber) {
			LazilyParsedNumber other = (LazilyParsedNumber) obj;
			return value == other.value || value.equals(other.value);
		}
		return false;
	}
}