/*
 * Copyright 2010, 2013 Jan Ouwens
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.jqno.equalsverifier.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Iterable to iterate over all declared fields in a class and, if needed,
 * over all declared fields of its superclasses.
 *
 * @author Jan Ouwens
 */
public final class FieldIterable implements Iterable<Field> {
	private final Class<?> type;
	private final boolean includeSuperclasses;

	/**
	 * Private constructor. Call {@link #of(Class)} or
	 * {@link #ofIgnoringSuper(Class)} instead.
	 */
	private FieldIterable(Class<?> type, boolean includeSuperclasses) {
		this.type = type;
		this.includeSuperclasses = includeSuperclasses;
	}

	/**
	 * Factory method for a FieldIterator that iterates over all declared
	 * fields of {@code type} and over the declared fields of all of its
	 * superclasses.
	 *
	 * @param type The class that contains the fields over which to iterate.
	 * @return A FieldIterator.
	 */
	public static FieldIterable of(Class<?> type) {
		return new FieldIterable(type, true);
	}

	/**
	 * Factory method for a FieldIterator that iterates over all declared
	 * fields of {@code type}, but that ignores the declared fields of its
	 * superclasses.
	 *
	 * @param type The class that contains the fields over which to iterate.
	 * @return A FieldIterator.
	 */
	public static FieldIterable ofIgnoringSuper(Class<?> type) {
		return new FieldIterable(type, false);
	}

	/**
	 * Returns an iterator over all declared fields of the class and all of its
	 * superclasses.
	 *
	 * @return An iterator over all declared fields of the class and all of its
	 *          superclasses.
	 */
	@Override
	public Iterator<Field> iterator() {
		return createFieldList().iterator();
	}

	private List<Field> createFieldList() {
		List<Field> result = new ArrayList<>();

		result.addAll(addFieldsFor(type));

		if (includeSuperclasses) {
			for (Class<?> c : SuperclassIterable.of(type)) {
				result.addAll(addFieldsFor(c));
			}
		}

		return result;
	}

	private List<Field> addFieldsFor(Class<?> c) {
		List<Field> result = new ArrayList<>();

		for (Field field : c.getDeclaredFields()) {
			if (!field.isSynthetic() && !"__cobertura_counters".equals(field.getName())) {
				result.add(field);
			}
		}

		return result;
	}
}
