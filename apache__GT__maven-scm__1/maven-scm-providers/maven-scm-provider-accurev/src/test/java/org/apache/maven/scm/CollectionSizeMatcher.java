package org.apache.maven.scm;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class CollectionSizeMatcher<T> extends TypeSafeMatcher<Iterable<T>> {

	private int size;

	public CollectionSizeMatcher(int size) {
		this.size = size;
	}

	@Override
	public boolean matchesSafely(Iterable<T> iterable) {
		Collection<?> collection = (Collection<?>) iterable;
		return collection.size() == this.size;

	}

	public void describeTo(Description desc) {
		desc.appendText("with size ");
		desc.appendValue(size);

	}

	public static <T> Matcher<Iterable<T>> size(int length, Class<T> clazz) {
		return new CollectionSizeMatcher<T>(length);
	}
}
