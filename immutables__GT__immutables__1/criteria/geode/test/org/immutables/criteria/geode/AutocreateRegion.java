/*
 * Copyright 2019 Immutables Authors and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.immutables.criteria.geode;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Scope;
import org.immutables.criteria.backend.ContainerNaming;

import java.util.function.Consumer;

class AutocreateRegion implements Consumer<Class<?>> {

	private final Cache cache;
	private final ContainerNaming naming;

	AutocreateRegion(Cache cache) {
		this(cache, ContainerNaming.DEFAULT);
	}

	AutocreateRegion(Cache cache, ContainerNaming containerNaming) {
		this.cache = cache;
		this.naming = containerNaming;
	}

	@Override
	public void accept(Class<?> entity) {
		String name = naming.name(entity);
		// exists ?
		if (cache.getRegion(name) != null) {
			return;
		}

		@SuppressWarnings("unchecked")
		Class<Object> valueConstraint = (Class<Object>) entity;
		// if not, create
		cache.createRegionFactory().setScope(Scope.LOCAL) // otherwise ConcurrentMap API doesn't work for Region
				.setValueConstraint(valueConstraint).create(name);
	}
}
