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
package org.jclouds.ec2.features;

import static org.jclouds.ec2.options.DescribeSnapshotsOptions.Builder.ownedBy;
import static org.jclouds.ec2.options.DetachVolumeOptions.Builder.fromInstance;
import static org.jclouds.reflect.Reflection2.method;

import java.io.IOException;

import org.jclouds.Fallbacks.EmptySetOnNotFoundOr404;
import org.jclouds.ec2.EC2Fallbacks.VoidOnVolumeAvailable;
import org.jclouds.ec2.options.CreateSnapshotOptions;
import org.jclouds.ec2.options.DescribeSnapshotsOptions;
import org.jclouds.ec2.options.DetachVolumeOptions;
import org.jclouds.ec2.xml.AttachmentHandler;
import org.jclouds.ec2.xml.DescribeSnapshotsResponseHandler;
import org.jclouds.ec2.xml.DescribeVolumesResponseHandler;
import org.jclouds.ec2.xml.PermissionHandler;
import org.jclouds.ec2.xml.SnapshotHandler;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.functions.ParseSax;
import org.jclouds.http.functions.ReleasePayloadAndReturn;
import org.jclouds.rest.internal.GeneratedHttpRequest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.Invokable;

/**
 * Tests behavior of {@code ElasticBlockStoreApi}
 */
// NOTE:without testName, this will not call @Before* and fail w/NPE during surefire
@Test(groups = "unit", testName = "ElasticBlockStoreApiTest")
public class ElasticBlockStoreApiTest extends BaseEC2ApiTest<ElasticBlockStoreApi> {

	public void testDeleteVolume() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "deleteVolumeInRegion", String.class, String.class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList(null, "id"));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, "Action=DeleteVolume&VolumeId=id", "application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	HttpRequest describeVolumes = HttpRequest.builder().method("POST").endpoint("https://ec2.us-east-1.amazonaws.com/")
			.addHeader("Host", "ec2.us-east-1.amazonaws.com").addFormParam("Action", "DescribeVolumes")
			.addFormParam("Signature", "nNxWg5dwYZEQu1QCzCtNp7iDmPR8wXXdKhWmLEKFLGI=")
			.addFormParam("SignatureMethod", "HmacSHA256").addFormParam("SignatureVersion", "2")
			.addFormParam("Timestamp", "2009-11-08T15:54:08.897Z").addFormParam("Version", "2010-08-31")
			.addFormParam("AWSAccessKeyId", "identity").build();

	public void testDescribeVolumes() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "describeVolumesInRegion", String.class,
				String[].class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList((String) null));

		request = (GeneratedHttpRequest) request.getFilters().get(0).filter(request);

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, describeVolumes.getPayload().getRawContent().toString(),
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ParseSax.class);
		assertSaxResponseParserClassEquals(method, DescribeVolumesResponseHandler.class);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	public void testDescribeVolumesArgs() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "describeVolumesInRegion", String.class,
				String[].class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList(null, "1", "2"));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, "Action=DescribeVolumes&VolumeId.1=1&VolumeId.2=2",
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ParseSax.class);
		assertSaxResponseParserClassEquals(method, DescribeVolumesResponseHandler.class);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	HttpRequest attachVolume = HttpRequest.builder().method("POST").endpoint("https://ec2.us-east-1.amazonaws.com/")
			.addHeader("Host", "ec2.us-east-1.amazonaws.com").addFormParam("Action", "AttachVolume")
			.addFormParam("Device", "/device").addFormParam("InstanceId", "instanceId")
			.addFormParam("Signature", "0+UY5oCQzoJapEHq4Dl2R/0nAA8uXEHqvHbnnNI5NcA=")
			.addFormParam("SignatureMethod", "HmacSHA256").addFormParam("SignatureVersion", "2")
			.addFormParam("Timestamp", "2009-11-08T15:54:08.897Z").addFormParam("Version", "2010-08-31")
			.addFormParam("VolumeId", "id").addFormParam("AWSAccessKeyId", "identity").build();

	public void testAttachVolume() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "attachVolumeInRegion", String.class, String.class,
				String.class, String.class);
		GeneratedHttpRequest request = processor.createRequest(method,
				Lists.<Object>newArrayList(null, "id", "instanceId", "/device"));

		request = (GeneratedHttpRequest) request.getFilters().get(0).filter(request);

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, attachVolume.getPayload().getRawContent().toString(),
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ParseSax.class);
		assertSaxResponseParserClassEquals(method, AttachmentHandler.class);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	HttpRequest detachVolume = HttpRequest.builder().method("POST").endpoint("https://ec2.us-east-1.amazonaws.com/")
			.addHeader("Host", "ec2.us-east-1.amazonaws.com").addFormParam("Action", "DetachVolume")
			.addFormParam("Force", "false").addFormParam("Signature", "tfNB1g2WVqb3EwvBJlk4duU1H1fDOa1SBstsm1elpbg=")
			.addFormParam("SignatureMethod", "HmacSHA256").addFormParam("SignatureVersion", "2")
			.addFormParam("Timestamp", "2009-11-08T15:54:08.897Z").addFormParam("Version", "2010-08-31")
			.addFormParam("VolumeId", "id").addFormParam("AWSAccessKeyId", "identity").build();

	public void testDetachVolume() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "detachVolumeInRegion", String.class, String.class,
				boolean.class, DetachVolumeOptions[].class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList(null, "id", false));

		request = (GeneratedHttpRequest) request.getFilters().get(0).filter(request);

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, detachVolume.getPayload().getRawContent().toString(),
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, VoidOnVolumeAvailable.class);

		checkFilters(request);
	}

	HttpRequest detachVolumeOptions = HttpRequest.builder().method("POST")
			.endpoint("https://ec2.us-east-1.amazonaws.com/").addHeader("Host", "ec2.us-east-1.amazonaws.com")
			.addFormParam("Action", "DetachVolume").addFormParam("Device", "/device").addFormParam("Force", "true")
			.addFormParam("InstanceId", "instanceId")
			.addFormParam("Signature", "VFhgwdkKBKXr/dEn2gvk6Vqq3JIunw4zZgM2Tt/ouME=")
			.addFormParam("SignatureMethod", "HmacSHA256").addFormParam("SignatureVersion", "2")
			.addFormParam("Timestamp", "2009-11-08T15:54:08.897Z").addFormParam("Version", "2010-08-31")
			.addFormParam("VolumeId", "id").addFormParam("AWSAccessKeyId", "identity").build();

	public void testDetachVolumeOptions() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "detachVolumeInRegion", String.class, String.class,
				boolean.class, DetachVolumeOptions[].class);
		GeneratedHttpRequest request = processor.createRequest(method,
				Lists.<Object>newArrayList(null, "id", true, fromInstance("instanceId").fromDevice("/device")));

		request = (GeneratedHttpRequest) request.getFilters().get(0).filter(request);

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, detachVolumeOptions.getPayload().getRawContent().toString(),
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, VoidOnVolumeAvailable.class);

		checkFilters(request);
	}

	public void testCreateSnapshot() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "createSnapshotInRegion", String.class,
				String.class, CreateSnapshotOptions[].class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList(null, "volumeId"));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, "Action=CreateSnapshot&VolumeId=volumeId", "application/x-www-form-urlencoded",
				false);

		assertResponseParserClassEquals(method, request, ParseSax.class);
		assertSaxResponseParserClassEquals(method, SnapshotHandler.class);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	public void testCreateSnapshotOptions() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "createSnapshotInRegion", String.class,
				String.class, CreateSnapshotOptions[].class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList(null, "volumeId",
				CreateSnapshotOptions.Builder.withDescription("description")));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, "Action=CreateSnapshot&VolumeId=volumeId&Description=description",
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ParseSax.class);
		assertSaxResponseParserClassEquals(method, SnapshotHandler.class);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	public void testDescribeSnapshots() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "describeSnapshotsInRegion", String.class,
				DescribeSnapshotsOptions[].class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList((String) null));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, "Action=DescribeSnapshots", "application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ParseSax.class);
		assertSaxResponseParserClassEquals(method, DescribeSnapshotsResponseHandler.class);
		assertFallbackClassEquals(method, EmptySetOnNotFoundOr404.class);

		checkFilters(request);
	}

	public void testDescribeSnapshotsArgs() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "describeSnapshotsInRegion", String.class,
				DescribeSnapshotsOptions[].class);
		GeneratedHttpRequest request = processor.createRequest(method,
				Lists.<Object>newArrayList(null, ownedBy("o1", "o2").restorableBy("r1", "r2").snapshotIds("s1", "s2")));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request,
				"Action=DescribeSnapshots&Owner.1=o1&Owner.2=o2&RestorableBy.1=r1&RestorableBy.2=r2&SnapshotId.1=s1&SnapshotId.2=s2",
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ParseSax.class);
		assertSaxResponseParserClassEquals(method, DescribeSnapshotsResponseHandler.class);
		assertFallbackClassEquals(method, EmptySetOnNotFoundOr404.class);

		checkFilters(request);
	}

	public void testGetCreateVolumePermissionForSnapshot()
			throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "getCreateVolumePermissionForSnapshotInRegion",
				String.class, String.class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList(null, "snapshotId"));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request,
				"Action=DescribeSnapshotAttribute&Attribute=createVolumePermission&SnapshotId=snapshotId",
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ParseSax.class);
		assertSaxResponseParserClassEquals(method, PermissionHandler.class);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	HttpRequest addCreateVolumePermissionsToSnapshot = HttpRequest.builder().method("POST")
			.endpoint("https://ec2.us-east-1.amazonaws.com/").addHeader("Host", "ec2.us-east-1.amazonaws.com")
			.addFormParam("Action", "ModifySnapshotAttribute").addFormParam("Attribute", "createVolumePermission")
			.addFormParam("OperationType", "add")
			.addFormParam("Signature", "s8m8DqQRXmecWguuRjxfl3Ibd+1AjjktGzTlJLUTcPc=")
			.addFormParam("SignatureMethod", "HmacSHA256").addFormParam("SignatureVersion", "2")
			.addFormParam("SnapshotId", "snapshotId").addFormParam("Timestamp", "2009-11-08T15:54:08.897Z")
			.addFormParam("UserGroup.1", "all").addFormParam("UserId.1", "bob").addFormParam("UserId.2", "sue")
			.addFormParam("Version", "2010-08-31").addFormParam("AWSAccessKeyId", "identity").build();

	public void testAddCreateVolumePermissionsToSnapshot()
			throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "addCreateVolumePermissionsToSnapshotInRegion",
				String.class, Iterable.class, Iterable.class, String.class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList(null,
				ImmutableList.of("bob", "sue"), ImmutableList.of("all"), "snapshotId"));

		request = (GeneratedHttpRequest) request.getFilters().get(0).filter(request);

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, addCreateVolumePermissionsToSnapshot.getPayload().getRawContent().toString(),
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	HttpRequest removeCreateVolumePermissionsFromSnapshot = HttpRequest.builder().method("POST")
			.endpoint("https://ec2.us-east-1.amazonaws.com/").addHeader("Host", "ec2.us-east-1.amazonaws.com")
			.addFormParam("Action", "ModifySnapshotAttribute").addFormParam("Attribute", "createVolumePermission")
			.addFormParam("OperationType", "remove")
			.addFormParam("Signature", "RzxHUIWV80cyhQDtrMiHDWUosS1g8cn1+7ONLJCe1dg=")
			.addFormParam("SignatureMethod", "HmacSHA256").addFormParam("SignatureVersion", "2")
			.addFormParam("SnapshotId", "snapshotId").addFormParam("Timestamp", "2009-11-08T15:54:08.897Z")
			.addFormParam("UserGroup.1", "all").addFormParam("UserId.1", "bob").addFormParam("UserId.2", "sue")
			.addFormParam("Version", "2010-08-31").addFormParam("AWSAccessKeyId", "identity").build();

	public void testRemoveCreateVolumePermissionsFromSnapshot()
			throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "removeCreateVolumePermissionsFromSnapshotInRegion",
				String.class, Iterable.class, Iterable.class, String.class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList(null,
				ImmutableList.of("bob", "sue"), ImmutableList.of("all"), "snapshotId"));

		request = (GeneratedHttpRequest) request.getFilters().get(0).filter(request);

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, removeCreateVolumePermissionsFromSnapshot.getPayload().getRawContent().toString(),
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	public void testResetCreateVolumePermissionsOnSnapshot()
			throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(ElasticBlockStoreApi.class, "resetCreateVolumePermissionsOnSnapshotInRegion",
				String.class, String.class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList(null, "snapshotId"));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request,
				"Action=ResetSnapshotAttribute&Attribute=createVolumePermission&SnapshotId=snapshotId",
				"application/x-www-form-urlencoded", false);
		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}
}
