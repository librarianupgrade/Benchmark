
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

package org.immutables.criteria.elasticsearch;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.reindex.ReindexPlugin;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.painless.PainlessPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static java.util.Collections.emptyMap;

/**
 * Represents a single elastic search node which can run embedded in a java application.
 *
 * <p>Intended for unit and integration tests.
 */
class EmbeddedElasticsearchNode implements Closeable {

	private final Node node;
	private volatile boolean isStarted;

	private EmbeddedElasticsearchNode(Node node) {
		this.node = Objects.requireNonNull(node, "node");
	}

	/**
	 * Creates an instance with existing settings
	 * @param settings configuration parameters of ES instance
	 * @return instance which needs to be explicitly started (using {@link #start()})
	 */
	private static EmbeddedElasticsearchNode create(Settings settings) {
		// ensure PainlessPlugin is installed or otherwise scripted fields would not work
		Node node = new LocalNode(settings,
				Arrays.asList(Netty4Plugin.class, PainlessPlugin.class, ReindexPlugin.class));
		return new EmbeddedElasticsearchNode(node);
	}

	/**
	 * Creates elastic node as single member of a cluster. Node will not be started
	 * unless {@link #start()} is explicitly called.
	 * <p>Need {@code synchronized} because of static caches inside ES (which are not thread safe).
	 * @return instance which needs to be explicitly started (using {@link #start()})
	 */
	static synchronized EmbeddedElasticsearchNode create() {
		File home = Files.createTempDir();
		home.deleteOnExit();

		Settings settings = Settings.builder().put("node.name", "embedded-test-elastic")
				.put("path.home", home.getAbsolutePath()).put("http.type", "netty4")
				// allow multiple instances to run in parallel
				.put("transport.tcp.port", 0).put("http.port", 0).put("network.host", "localhost").build();

		return create(settings);
	}

	/**
	 * Starts current node
	 */
	void start() {
		Preconditions.checkState(!isStarted, "already started");
		try {
			node.start();
			this.isStarted = true;
		} catch (NodeValidationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns current address to connect to with HTTP client.
	 * @return hostname/port for HTTP connection
	 */
	TransportAddress httpAddress() {
		Preconditions.checkState(isStarted, "node was not started");

		NodesInfoResponse response = node.client().admin().cluster().prepareNodesInfo().execute().actionGet();
		if (response.getNodes().size() != 1) {
			throw new IllegalStateException("Expected single node but got " + response.getNodes().size());
		}
		NodeInfo node = response.getNodes().get(0);
		return node.getHttp().address().boundAddresses()[0];
	}

	@Override
	public void close() throws IOException {
		node.close();
		// cleanup data dirs
		for (String name : Arrays.asList("path.data", "path.home")) {
			final String path = node.settings().get(name);
			if (path != null) {
				deleteRecursively(new File(path));
			}
		}
	}

	/**
	 * Deletes current file or (if a directory) all files recursively.
	 */
	private static void deleteRecursively(File path) {
		if (!path.exists()) {
			return;
		}

		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				deleteRecursively(f);
			}
		}

		path.delete();
	}

	/**
	 * Having separate class to expose (protected) constructor which allows to install
	 * different plugins. In our case it is {@code GroovyPlugin} for scripted fields
	 * like {@code loc[0]} or {@code loc[1]['foo']}.
	 *
	 * <p>This class is intended solely for tests
	 */
	private static class LocalNode extends Node {

		private LocalNode(Settings settings, Collection<Class<? extends Plugin>> classpathPlugins) {
			super(InternalSettingsPreparer.prepareEnvironment(settings, emptyMap(), null, () -> "default_node_name"),
					classpathPlugins, false);
		}
	}
}
