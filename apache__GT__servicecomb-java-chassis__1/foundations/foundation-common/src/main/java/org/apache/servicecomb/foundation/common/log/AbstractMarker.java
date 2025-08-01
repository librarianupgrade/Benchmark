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
package org.apache.servicecomb.foundation.common.log;

import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Marker;

public abstract class AbstractMarker implements Marker {
	private static final long serialVersionUID = -1L;

	@Override
	public void add(Marker reference) {

	}

	@Override
	public boolean remove(Marker reference) {
		return false;
	}

	@Deprecated
	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public boolean hasReferences() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Marker> iterator() {
		return Collections.EMPTY_LIST.iterator();
	}

	@Override
	public boolean contains(Marker other) {
		return false;
	}

	@Override
	public boolean contains(String name) {
		return false;
	}

	@Override
	public String toString() {
		return getName();
	}
}
