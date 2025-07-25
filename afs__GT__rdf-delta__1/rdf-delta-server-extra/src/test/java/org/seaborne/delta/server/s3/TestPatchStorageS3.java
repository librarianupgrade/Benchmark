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

package org.seaborne.delta.server.s3;

import java.io.IOException;
import java.net.ServerSocket;

import com.amazonaws.services.s3.AmazonS3;

import io.findify.s3mock.S3Mock;
import org.junit.After;
import org.junit.Before;
import org.seaborne.delta.DeltaException;
import org.seaborne.delta.lib.LogX;
import org.seaborne.delta.server.local.LocalServerConfig;
import org.seaborne.delta.server.local.patchstores.PatchStorage;
import org.seaborne.delta.server.patchstores.AbstractTestPatchStorage;

public class TestPatchStorageS3 extends AbstractTestPatchStorage {
	static {
		LogX.setJavaLogging();
	}

	private static String testRegion = "uk-bristol-1";
	private static String testBucketName = "delta";
	private static String testPrefix = "patches/";

	private int port = choosePort();
	private S3Mock s3Mock;
	private String endpoint = "http://localhost:" + port + "/";

	/** Choose an unused port for a server to listen on */
	public static int choosePort() {
		try (ServerSocket s = new ServerSocket(0)) {
			return s.getLocalPort();
		} catch (IOException ex) {
			throw new DeltaException("Failed to find a port");
		}
	}

	@Before
	public void before() {
		s3Mock = new S3Mock.Builder().withPort(port).withInMemoryBackend().build();
		s3Mock.start();
	}

	@After
	public void after() {
		s3Mock.shutdown();
	}

	@Override
	protected PatchStorage patchStorage() {
		S3Config cfg = S3Config.create().bucketName(testBucketName).region(testRegion).endpoint(endpoint).build();

		LocalServerConfig config = S3.configZkS3("", cfg);
		AmazonS3 aws = S3.buildS3(config);
		S3.ensureBucketExists(aws, testBucketName);
		return new PatchStorageS3(aws, testBucketName, testPrefix);
	}
}
