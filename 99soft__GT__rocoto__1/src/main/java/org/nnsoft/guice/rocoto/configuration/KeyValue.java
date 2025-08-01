package org.nnsoft.guice.rocoto.configuration;

/*
 *    Copyright 2009-2012 The 99 Software Foundation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import java.util.Map.Entry;

/**
 * A simple {@code Entry<String, String>} implementation.
 */
final class KeyValue implements Entry<String, String> {

	/**
	 * The entry key.
	 */
	private final String key;

	/**
	 * The entry value.
	 */
	private final String value;

	/**
	 * Creates a new {@code Entry<String, String>}.
	 *
	 * @param key the entry key.
	 * @param value the entry value.
	 */
	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getKey() {
		return key;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public String setValue(String value) {
		throw new UnsupportedOperationException("Value is read-only in this version");
	}

}
