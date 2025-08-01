
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

package net.minidev.json.writer;

import net.minidev.json.JSONAwareEx;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

public class JsonReader {
	private final ConcurrentHashMap<Type, JsonReaderI<?>> cache;

	public JsonReaderI<JSONAwareEx> DEFAULT;
	public JsonReaderI<JSONAwareEx> DEFAULT_ORDERED;

	public JsonReader() {
		cache = new ConcurrentHashMap<Type, JsonReaderI<?>>(100);

	}

	/**
	 * remap field name in custom classes
	 *
	 * @param fromJson field name in json
	 * @param toJava   field name in Java
	 * @since 2.1.1
	 */
	public <T> void remapField(Class<T> type, String fromJson, String toJava) {

	}

	public <T> void registerReader(Class<T> type, JsonReaderI<T> mapper) {
		cache.put(type, mapper);
	}

	@SuppressWarnings("unchecked")
	public <T> JsonReaderI<T> getMapper(Type type) {
		return (JsonReaderI<T>) FakeMapper.DEFAULT;
	}

	/**
	 * Get the corresponding mapper Class, or create it on first call
	 *
	 * @param type to be map
	 */
	public <T> JsonReaderI<T> getMapper(Class<T> type) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> JsonReaderI<T> getMapper(ParameterizedType type) {
		return null;
	}
}
