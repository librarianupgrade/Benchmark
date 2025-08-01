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
package org.jclouds.oauth.v2.functions;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.BaseEncoding.base64Url;
import static org.jclouds.oauth.v2.config.PrivateKeySupplierTest.loadPrivateKey;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

@Test(groups = "unit")
public class ClaimsToAssertionTest {

	private static final String PAYLOAD = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.\n"
			+ "eyJpc3MiOiI3NjEzMjY3OTgwNjktcjVtbGpsbG4xcmQ0bHJiaGc3NWVmZ2lncDM2bTc4ajVAZ"
			+ "GV2ZWxvcGVyLmdzZXJ2aWNlYWNjb3VudC5jb20iLCJzY29wZSI6Imh0dHBzOi8vd3d3Lmdvb2ds"
			+ "ZWFwaXMuY29tL2F1dGgvcHJlZGljdGlvbiIsImF1ZCI6Imh0dHBzOi8vYWNjb3VudHMuZ29vZ2x"
			+ "lLmNvbS9vL29hdXRoMi90b2tlbiIsImV4cCI6MTMyODU1NDM4NSwiaWF0IjoxMzI4NTUwNzg1fQ";

	private static final String SHA256withRSA_PAYLOAD_SIGNATURE_RESULT = "bmQrCv4gjkLWDK1JNJni74_kPiSDUMF_FImgqKJMUIgkDX1m2Sg3bH1yjF-cjBN7CvfAscnageo"
			+ "GtL2TGbwoTjJgUO5Yy0esavUUF-mBQHQtSw-2nL-9TNyM4SNi6fHPbgr83GGKOgA86r" + "I9-nj3oUGd1fQty2k4Lsd-Zdkz6es";

	public void sha256() throws Exception {
		byte[] payloadSignature = ClaimsToAssertion.sha256(loadPrivateKey(), PAYLOAD.getBytes(UTF_8));
		assertNotNull(payloadSignature);

		assertEquals(base64Url().omitPadding().encode(payloadSignature), SHA256withRSA_PAYLOAD_SIGNATURE_RESULT);
	}
}
