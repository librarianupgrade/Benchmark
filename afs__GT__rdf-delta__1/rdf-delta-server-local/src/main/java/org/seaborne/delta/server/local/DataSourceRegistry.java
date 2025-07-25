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

package org.seaborne.delta.server.local;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.Registry;
import org.seaborne.delta.Id;
import org.slf4j.Logger;

/** The registry of all {@link DataSource} under the control of server.
 *  Each {@link LocalServer} has a single {@code DataRegistry}
 *  which provides the lookup map for the managed {@link DataSource}s.
 *  <p>
 *  The patches area is a {@link PatchLog},
 *  and the implementations are determined by {@link PatchStore}, technology for a group of implementations.
 *  <p>
 *  The can be different implementations of {@link PatchLog} in one system, e.g. file backed and database backed.
 */
public class DataSourceRegistry extends Registry<Id, DataSource> {

	private static Logger LOG = DPS.LOG;
	private final String label;
	// Index DataSources by URI, only if the URI is not null.
	private Map<String, DataSource> indexByURI = new ConcurrentHashMap<>();
	private Map<String, DataSource> indexByName = new ConcurrentHashMap<>();

	public DataSourceRegistry(String label) {
		this.label = label;
	}

	public void add(DataSource ds) {
		put(ds.getId(), ds);
	}

	@Override
	public void put(Id key, DataSource ds) {
		if (LOG.isDebugEnabled())
			LOG.debug("Register datasource: " + key);
		remove(key);
		super.put(key, ds);
		if (ds.getURI() != null)
			indexByURI.put(ds.getURI(), ds);
		if (ds.getName() != null)
			indexByName.put(ds.getName(), ds);
	}

	@Override
	public void remove(Id key) {
		DataSource ds = get(key);
		if (ds == null)
			return;
		super.remove(key);
		indexByName.remove(ds.getName());
		indexByURI.remove(ds.getURI());
	}

	@Override
	public DataSource get(Id key) {
		return super.get(key);
	}

	public boolean contains(Id key) {
		return super.isRegistered(key);
	}

	public DataSource getByURI(String uri) {
		return indexByURI.get(uri);
	}

	public boolean containsURI(String uri) {
		return indexByURI.containsKey(uri);
	}

	public DataSource getByName(String name) {
		return indexByName.get(name);
	}

	public boolean containsName(String name) {
		return indexByName.containsKey(name);
	}

	public Stream<String> names() {
		return indexByName.keySet().stream();
	}

	public Stream<DataSource> dataSources() {
		return indexByName.values().stream();
	}

	@Override
	public String toString() {
		if (label != null)
			return String.format("Registry: '%s': size=%d : %s", label, super.size(), super.keys());
		else
			return String.format("Registry: size=%d : %s", super.size(), super.keys());
	}
}
