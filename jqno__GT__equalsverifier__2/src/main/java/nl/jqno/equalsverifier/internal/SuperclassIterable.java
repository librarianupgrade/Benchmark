/*
 * Copyright 2015 Jan Ouwens
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Iterable to iterate over all superclasses of a class.
 *
 * @author Jan Ouwens
 */
public final class SuperclassIterable<T> implements Iterable<Class<? super T>> {
	private final Class<T> type;
	private final boolean includeSelf;

	/**
	 * Private constructor. Call {@link #of(Class)} or
	 * {@link #ofIncludeSelf(Class)} instead.
	 */
	private SuperclassIterable(Class<T> type, boolean includeSelf) {
		this.type = type;
		this.includeSelf = includeSelf;
	}

	/**
	 * Factory method for a SuperlcassIterator that iterates over type's
	 * superclasses, excluding itself and excluding Object.
	 *
	 * @param type The class over whose superclasses to iterate.
	 * @param <T> Type parameter for type.
	 * @return A SuperclassIterator.
	 */
	public static <T> SuperclassIterable<T> of(Class<T> type) {
		return new SuperclassIterable<>(type, false);
	}

	/**
	 * Factory method for a SuperlcassIterator that iterates over type's
	 * superclasses, including itself but excluding Object.
	 *
	 * @param type The class over whose superclasses to iterate.
	 * @param <T> Type parameter for type.
	 * @return A SuperclassIterator.
	 */
	public static <T> SuperclassIterable<T> ofIncludeSelf(Class<T> type) {
		return new SuperclassIterable<>(type, true);
	}

	/**
	 * Returns an iterator over all superclasses of the class. Is empty if type
	 * has no superclasses and SuperclassIterable does not include self.
	 *
	 * @return The iterator.
	 */
	@Override
	public Iterator<Class<? super T>> iterator() {
		return createClassList().iterator();
	}

	private List<Class<? super T>> createClassList() {
		List<Class<? super T>> result = new ArrayList<>();
		if (includeSelf) {
			result.add(type);
		}
		Class<? super T> i = type.getSuperclass();
		while (i != null && !i.equals(Object.class)) {
			result.add(i);
			i = i.getSuperclass();
		}
		return result;
	}
}
