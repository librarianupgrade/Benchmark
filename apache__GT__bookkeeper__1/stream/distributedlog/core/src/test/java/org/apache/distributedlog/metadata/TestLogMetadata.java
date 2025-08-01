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
package org.apache.distributedlog.metadata;

import static org.apache.distributedlog.metadata.LogMetadata.ALLOCATION_PATH;
import static org.apache.distributedlog.metadata.LogMetadata.LOCK_PATH;
import static org.apache.distributedlog.metadata.LogMetadata.LOGSEGMENTS_PATH;
import static org.apache.distributedlog.metadata.LogMetadata.MAX_TXID_PATH;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import org.apache.distributedlog.DLMTestUtil;
import org.junit.Test;

/**
 * Test Case for {@link LogMetadata}s.
 */
public class TestLogMetadata {

	@Test(timeout = 60000)
	public void testGetPaths() throws Exception {
		String rootPath = "/test-get-paths";
		URI uri = DLMTestUtil.createDLMURI(2181, rootPath);
		String logName = "test-log";
		String logIdentifier = "<default>";
		String logRootPath = uri.getPath() + "/" + logName + "/" + logIdentifier;
		String logSegmentName = "test-segment";

		LogMetadata logMetadata = new LogMetadata(uri, logName, logIdentifier);
		assertEquals("wrong log name", logName, logMetadata.getLogName());
		assertEquals("wrong root path", logRootPath, logMetadata.getLogRootPath());
		assertEquals("wrong log segments path", logRootPath + LOGSEGMENTS_PATH, logMetadata.getLogSegmentsPath());
		assertEquals("wrong log segment path", logRootPath + LOGSEGMENTS_PATH + "/" + logSegmentName,
				logMetadata.getLogSegmentPath(logSegmentName));
		assertEquals("wrong lock path", logRootPath + LOCK_PATH, logMetadata.getLockPath());
		assertEquals("wrong max tx id path", logRootPath + MAX_TXID_PATH, logMetadata.getMaxTxIdPath());
		assertEquals("wrong allocation path", logRootPath + ALLOCATION_PATH, logMetadata.getAllocationPath());
		assertEquals("wrong qualified name", logName + ":" + logIdentifier, logMetadata.getFullyQualifiedName());
	}

}
