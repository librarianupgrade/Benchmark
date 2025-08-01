/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.distributedlog.namespace;

import static org.apache.distributedlog.LocalDLMEmulator.DLOG_NAMESPACE;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import org.apache.distributedlog.BKDistributedLogNamespace;
import org.apache.distributedlog.DistributedLogConfiguration;
import org.apache.distributedlog.TestDistributedLogBase;
import org.apache.distributedlog.api.namespace.Namespace;
import org.apache.distributedlog.api.namespace.NamespaceBuilder;
import org.junit.Test;

/**
 * Test Namespace Builder.
 */
public class TestNamespaceBuilder extends TestDistributedLogBase {

	@Test(timeout = 60000, expected = NullPointerException.class)
	public void testEmptyBuilder() throws Exception {
		NamespaceBuilder.newBuilder().build();
	}

	@Test(timeout = 60000, expected = NullPointerException.class)
	public void testMissingUri() throws Exception {
		NamespaceBuilder.newBuilder().conf(new DistributedLogConfiguration()).build();
	}

	@Test(timeout = 60000, expected = NullPointerException.class)
	public void testMissingSchemeInUri() throws Exception {
		NamespaceBuilder.newBuilder().conf(new DistributedLogConfiguration()).uri(new URI("/test")).build();
	}

	@Test(timeout = 60000, expected = IllegalArgumentException.class)
	public void testInvalidSchemeInUri() throws Exception {
		NamespaceBuilder.newBuilder().conf(new DistributedLogConfiguration())
				.uri(new URI("dist://invalid/scheme/in/uri")).build();
	}

	@Test(timeout = 60000, expected = IllegalArgumentException.class)
	public void testInvalidSchemeCorrectBackendInUri() throws Exception {
		NamespaceBuilder.newBuilder().conf(new DistributedLogConfiguration())
				.uri(new URI("dist-bk://invalid/scheme/in/uri")).build();
	}

	@Test(timeout = 60000, expected = IllegalArgumentException.class)
	public void testUnknownBackendInUri() throws Exception {
		NamespaceBuilder.newBuilder().conf(new DistributedLogConfiguration())
				.uri(new URI("distributedlog-unknown://invalid/scheme/in/uri")).build();
	}

	@Test(timeout = 60000, expected = NullPointerException.class)
	public void testNullStatsLogger() throws Exception {
		NamespaceBuilder.newBuilder().conf(new DistributedLogConfiguration())
				.uri(new URI("distributedlog-bk://localhost/distributedlog")).statsLogger(null).build();
	}

	@Test(timeout = 60000, expected = NullPointerException.class)
	public void testNullClientId() throws Exception {
		NamespaceBuilder.newBuilder().conf(new DistributedLogConfiguration())
				.uri(new URI("distributedlog-bk://localhost/distributedlog")).clientId(null).build();
	}

	@Test(timeout = 60000)
	public void testBuildBKDistributedLogNamespace() throws Exception {
		Namespace namespace = NamespaceBuilder.newBuilder().conf(new DistributedLogConfiguration())
				.uri(new URI("distributedlog-bk://" + zkServers + DLOG_NAMESPACE + "/bknamespace")).build();
		try {
			assertTrue("distributedlog-bk:// should build bookkeeper based distributedlog namespace",
					namespace instanceof BKDistributedLogNamespace);
		} finally {
			namespace.close();
		}
	}

	@Test(timeout = 60000)
	public void testBuildWhenMissingBackendInUri() throws Exception {
		Namespace namespace = NamespaceBuilder.newBuilder().conf(new DistributedLogConfiguration())
				.uri(new URI("distributedlog://" + zkServers + DLOG_NAMESPACE + "/defaultnamespace")).build();
		try {
			assertTrue("distributedlog:// should build bookkeeper based distributedlog namespace",
					namespace instanceof BKDistributedLogNamespace);
		} finally {
			namespace.close();
		}
	}
}
