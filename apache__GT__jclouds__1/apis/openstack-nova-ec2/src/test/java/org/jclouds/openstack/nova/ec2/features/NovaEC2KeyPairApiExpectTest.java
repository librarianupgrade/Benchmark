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
package org.jclouds.openstack.nova.ec2.features;

import static org.testng.Assert.assertEquals;

import org.jclouds.ec2.domain.KeyPair;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.HttpResponse;
import org.jclouds.openstack.nova.ec2.internal.BaseNovaEC2RestApiExpectTest;
import org.testng.annotations.Test;

public class NovaEC2KeyPairApiExpectTest extends BaseNovaEC2RestApiExpectTest {

	public void testImportKeyPair() {
		NovaEC2KeyPairApi client = requestsSendResponses(describeAvailabilityZonesRequest,
				describeAvailabilityZonesResponse,
				HttpRequest.builder().method("POST").endpoint("http://localhost:8773/services/Cloud/")
						.addHeader("Host", "localhost:8773")
						.payload(payloadFromStringWithContentType(
								"Action=ImportKeyPair&KeyName=mykey&PublicKeyMaterial=c3NoLXJzYSBBQQ%3D%3D&Signature=wOOKOlDfJezRkx7NKcyOyaBQuY7PoVE3HFa9495RL7s%3D&SignatureMethod=HmacSHA256&SignatureVersion=2&Timestamp=2012-04-16T15%3A54%3A08.897Z&Version=2009-04-04&AWSAccessKeyId=identity",
								"application/x-www-form-urlencoded"))
						.build(),
				HttpResponse.builder().statusCode(200)
						.payload(payloadFromResource("/nova_ec2_import_keypair_response.xml")).build())
				.getKeyPairApi().get();

		KeyPair result = client.importKeyPairInRegion(null, "mykey", "ssh-rsa AA");
		assertEquals(result.getKeyName(), "aplowe-nova-ec22");
		assertEquals(result.getSha1OfPrivateKey(), "e3:fd:de:f6:4c:36:7d:9b:8f:2f:4c:20:f8:ae:b0:ea");
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testImportKeyPairFailsAlreadyExists() {
		NovaEC2KeyPairApi client = requestsSendResponses(describeAvailabilityZonesRequest,
				describeAvailabilityZonesResponse,
				HttpRequest.builder().method("POST").endpoint("http://localhost:8773/services/Cloud/")
						.addHeader("Host", "localhost:8773")
						.payload(payloadFromStringWithContentType(
								"Action=ImportKeyPair&KeyName=mykey&PublicKeyMaterial=c3NoLXJzYSBBQQ%3D%3D&Signature=wOOKOlDfJezRkx7NKcyOyaBQuY7PoVE3HFa9495RL7s%3D&SignatureMethod=HmacSHA256&SignatureVersion=2&Timestamp=2012-04-16T15%3A54%3A08.897Z&Version=2009-04-04&AWSAccessKeyId=identity",
								"application/x-www-form-urlencoded"))
						.build(),
				HttpResponse.builder().statusCode(409).build()).getKeyPairApi().get();

		client.importKeyPairInRegion(null, "mykey", "ssh-rsa AA");
	}
}
