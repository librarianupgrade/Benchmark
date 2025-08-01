/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.delta.server.system;

import java.util.List;

/**
 * A {@code SubsystemRegistry} is a set of objects implementing {@code T}.
 */
public interface SubsystemRegistry<T> {

	/** Load - peform some kinds of search for {@link SubsystemLifecycle} implementations.
	 * This is called once in the initialization process.
	 */
	public void load();

	/** Add to the collection. */
	public void add(T module);

	/** check whether registered */
	public boolean isRegistered(T module);

	/** Remove from the collection. */
	public void remove(T module);

	public int size();

	public boolean isEmpty();

	/**
	 * Return the registered items in a copied list.
	 * The list is detached from the
	 * registry and the caller can mutate it.
	 * There is no specific ordering requirement. 
	 */
	public List<T> snapshot();
}
