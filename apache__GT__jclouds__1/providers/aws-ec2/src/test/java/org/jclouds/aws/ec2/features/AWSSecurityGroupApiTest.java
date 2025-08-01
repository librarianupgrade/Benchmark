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
package org.jclouds.aws.ec2.features;

import static org.jclouds.reflect.Reflection2.method;

import java.io.IOException;

import org.jclouds.Fallbacks.EmptySetOnNotFoundOr404;
import org.jclouds.Fallbacks.VoidOnNotFoundOr404;
import org.jclouds.aws.ec2.xml.AWSEC2DescribeSecurityGroupsResponseHandler;
import org.jclouds.ec2.util.IpPermissions;
import org.jclouds.http.functions.ParseSax;
import org.jclouds.http.functions.ReleasePayloadAndReturn;
import org.jclouds.net.domain.IpPermission;
import org.jclouds.net.domain.IpProtocol;
import org.jclouds.rest.internal.GeneratedHttpRequest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.reflect.Invokable;

/**
 * Tests behavior of {@code AWSSecurityGroupApi}
 */
// NOTE:without testName, this will not call @Before* and fail w/NPE during
// surefire
@Test(groups = "unit", testName = "AWSSecurityGroupApiTest")
public class AWSSecurityGroupApiTest extends BaseAWSEC2ApiTest<AWSSecurityGroupApi> {
	public AWSSecurityGroupApiTest() {
		provider = "aws-ec2";
	}

	public void testDeleteSecurityGroup() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AWSSecurityGroupApi.class, "deleteSecurityGroupInRegionById", String.class,
				String.class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList(null, "id"));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, "Action=DeleteSecurityGroup&GroupId=id", "application/x-www-form-urlencoded",
				false);

		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, VoidOnNotFoundOr404.class);

		checkFilters(request);
	}

	public void testDescribeSecurityGroups() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AWSSecurityGroupApi.class, "describeSecurityGroupsInRegionById", String.class,
				String[].class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList((String) null));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, "Action=DescribeSecurityGroups", "application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ParseSax.class);
		assertSaxResponseParserClassEquals(method, AWSEC2DescribeSecurityGroupsResponseHandler.class);
		assertFallbackClassEquals(method, EmptySetOnNotFoundOr404.class);

		checkFilters(request);
	}

	public void testDescribeSecurityGroupsArgs() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AWSSecurityGroupApi.class, "describeSecurityGroupsInRegionById", String.class,
				String[].class);
		GeneratedHttpRequest request = processor.createRequest(method, Lists.<Object>newArrayList(null, "1", "2"));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request, "Action=DescribeSecurityGroups&GroupId.1=1&GroupId.2=2",
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ParseSax.class);
		assertSaxResponseParserClassEquals(method, AWSEC2DescribeSecurityGroupsResponseHandler.class);
		assertFallbackClassEquals(method, EmptySetOnNotFoundOr404.class);

		checkFilters(request);
	}

	public void testCreateSecurityGroup() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AWSSecurityGroupApi.class, "createSecurityGroupInRegion", String.class,
				String.class, String.class);
		GeneratedHttpRequest request = processor.createRequest(method,
				Lists.<Object>newArrayList(null, "name", "description"));

		request = (GeneratedHttpRequest) request.getFilters().get(0).filter(request);

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request,
				"Action=CreateSecurityGroup&GroupName=name&GroupDescription=description&Version=2012-06-01",
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	public void testAuthorizeSecurityGroupIpPermission() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AWSSecurityGroupApi.class, "authorizeSecurityGroupIngressInRegion",
				String.class, String.class, IpPermission.class);
		GeneratedHttpRequest request = processor.createRequest(method,
				Lists.<Object>newArrayList(null, "group", IpPermissions.permitAnyProtocol()));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request,
				"Action=AuthorizeSecurityGroupIngress&GroupId=group&IpPermissions.0.IpProtocol=-1&IpPermissions.0.FromPort=1&IpPermissions.0.ToPort=65535&IpPermissions.0.IpRanges.0.CidrIp=0.0.0.0/0",
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	public void testAuthorizeSecurityGroupIpPermissions() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AWSSecurityGroupApi.class, "authorizeSecurityGroupIngressInRegion",
				String.class, String.class, Iterable.class);
		GeneratedHttpRequest request = processor.createRequest(method,
				Lists.<Object>newArrayList(null, "group", ImmutableSet.<IpPermission>of(
						IpPermissions.permit(IpProtocol.TCP).originatingFromCidrBlock("1.1.1.1/32"),
						IpPermissions.permitICMP().type(8).andCode(0).originatingFromSecurityGroupId("groupId"))));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request,
				"Action=AuthorizeSecurityGroupIngress&GroupId=group&IpPermissions.0.IpProtocol=tcp&IpPermissions.0.FromPort=1&IpPermissions.0.ToPort=65535&IpPermissions.0.IpRanges.0.CidrIp=1.1.1.1/32&IpPermissions.1.IpProtocol=icmp&IpPermissions.1.FromPort=8&IpPermissions.1.ToPort=0&IpPermissions.1.Groups.0.GroupId=groupId",
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	public void testRevokeSecurityGroupIpPermission() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AWSSecurityGroupApi.class, "revokeSecurityGroupIngressInRegion", String.class,
				String.class, IpPermission.class);
		GeneratedHttpRequest request = processor.createRequest(method,
				Lists.<Object>newArrayList(null, "group", IpPermissions.permitAnyProtocol()));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request,
				"Action=RevokeSecurityGroupIngress&GroupId=group&IpPermissions.0.IpProtocol=-1&IpPermissions.0.FromPort=1&IpPermissions.0.ToPort=65535&IpPermissions.0.IpRanges.0.CidrIp=0.0.0.0/0",
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}

	public void testRevokeSecurityGroupIpPermissions() throws SecurityException, NoSuchMethodException, IOException {
		Invokable<?, ?> method = method(AWSSecurityGroupApi.class, "revokeSecurityGroupIngressInRegion", String.class,
				String.class, Iterable.class);
		GeneratedHttpRequest request = processor.createRequest(method,
				Lists.<Object>newArrayList(null, "group", ImmutableSet.<IpPermission>of(
						IpPermissions.permit(IpProtocol.TCP).originatingFromCidrBlock("1.1.1.1/32"),
						IpPermissions.permitICMP().type(8).andCode(0).originatingFromSecurityGroupId("groupId"))));

		assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
		assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
		assertPayloadEquals(request,
				"Action=RevokeSecurityGroupIngress&GroupId=group&IpPermissions.0.IpProtocol=tcp&IpPermissions.0.FromPort=1&IpPermissions.0.ToPort=65535&IpPermissions.0.IpRanges.0.CidrIp=1.1.1.1/32&IpPermissions.1.IpProtocol=icmp&IpPermissions.1.FromPort=8&IpPermissions.1.ToPort=0&IpPermissions.1.Groups.0.GroupId=groupId",
				"application/x-www-form-urlencoded", false);

		assertResponseParserClassEquals(method, request, ReleasePayloadAndReturn.class);
		assertSaxResponseParserClassEquals(method, null);
		assertFallbackClassEquals(method, null);

		checkFilters(request);
	}
}
