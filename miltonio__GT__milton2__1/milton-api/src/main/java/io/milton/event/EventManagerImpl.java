/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author brad
 */
public class EventManagerImpl implements EventManager {

	private static final Logger log = LoggerFactory.getLogger(EventManagerImpl.class);
	private final List<Registration> registrations = new CopyOnWriteArrayList<>();

	@Override
	public void fireEvent(Event e) {
		log.trace("fireEvent: {}", e.getClass());
		for (Registration r : registrations) {
			if (r.clazz.isAssignableFrom(e.getClass())) {
				long tm = System.currentTimeMillis();
				r.listener.onEvent(e);

				if (log.isTraceEnabled()) {
					log.trace("  fired on: {} completed in {}ms", r.listener.getClass(),
							(System.currentTimeMillis() - tm));
				}
			}
		}
	}

	@Override
	public synchronized <T extends Event> void registerEventListener(EventListener l, Class<T> c) {
		log.info("registerEventListener: {} - {}", l.getClass().getCanonicalName(), c.getCanonicalName());
		Registration r = new Registration(l, c);
		registrations.add(r);
	}

	private static class Registration {

		private final EventListener listener;
		private final Class<? extends Event> clazz;

		public Registration(EventListener listener, Class<? extends Event> clazz) {
			this.listener = listener;
			this.clazz = clazz;
		}

	}
}
