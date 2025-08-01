/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.apache.commons.vfs2.FileSystemException;

/**
 * A {@link VfsComponent} that contains a set of subcomponents.
 */
public abstract class AbstractVfsContainer extends AbstractVfsComponent {

	/**
	 * The components contained by this component.
	 */
	private final ArrayList<Object> components = new ArrayList<>(); // @GuardedBy("self")

	/**
	 * Adds a subcomponent to this component.
	 * <p>
	 * If the sub-component implements {@link VfsComponent}, it is initialized. All sub-components are closed when this
	 * component is closed.
	 * </p>
	 *
	 * @param component the component to add.
	 * @throws FileSystemException if any error occurs.
	 */
	protected void addComponent(final Object component) throws FileSystemException {
		synchronized (components) {
			if (!components.contains(component)) {
				// Initialize
				if (component instanceof VfsComponent) {
					final VfsComponent vfsComponent = (VfsComponent) component;
					vfsComponent.setLogger(getLogger());
					vfsComponent.setContext(getContext());
					vfsComponent.init();
				}

				// Keep track of component, to close it later
				components.add(component);
			}
		} // synchronized
	}

	/**
	 * Closes the subcomponents of this component.
	 */
	@Override
	public void close() {
		final Object[] toclose;
		synchronized (components) {
			toclose = components.toArray();
			components.clear();
		}

		// Close all components
		Stream.of(toclose).filter(component -> component instanceof VfsComponent)
				.forEach(component -> ((VfsComponent) component).close());
	}

	/**
	 * Removes a subcomponent from this component.
	 *
	 * @param component the component to remove.
	 */
	protected void removeComponent(final Object component) {
		synchronized (components) {
			// multiple instances should not happen
			components.remove(component);
		}
	}
}
