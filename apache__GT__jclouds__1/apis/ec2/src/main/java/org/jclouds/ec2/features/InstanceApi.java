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

import static org.jclouds.aws.reference.FormParameters.ACTION;

import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.jclouds.Fallbacks.EmptySetOnNotFoundOr404;
import org.jclouds.aws.filters.FormSigner;
import org.jclouds.ec2.binders.BindBlockDeviceMappingToIndexedFormParams;
import org.jclouds.ec2.binders.BindFiltersToIndexedFormParams;
import org.jclouds.ec2.binders.BindInstanceIdsToIndexedFormParams;
import org.jclouds.ec2.binders.IfNotNullBindAvailabilityZoneToFormParam;
import org.jclouds.ec2.domain.BlockDevice;
import org.jclouds.ec2.domain.InstanceStateChange;
import org.jclouds.ec2.domain.Reservation;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.ec2.domain.Volume.InstanceInitiatedShutdownBehavior;
import org.jclouds.ec2.functions.ConvertUnencodedBytesToBase64EncodedString;
import org.jclouds.ec2.options.RunInstancesOptions;
import org.jclouds.ec2.xml.BlockDeviceMappingHandler;
import org.jclouds.ec2.xml.BooleanValueHandler;
import org.jclouds.ec2.xml.DescribeInstancesResponseHandler;
import org.jclouds.ec2.xml.GetConsoleOutputResponseHandler;
import org.jclouds.ec2.xml.InstanceInitiatedShutdownBehaviorHandler;
import org.jclouds.ec2.xml.InstanceStateChangeHandler;
import org.jclouds.ec2.xml.InstanceTypeHandler;
import org.jclouds.ec2.xml.RunInstancesResponseHandler;
import org.jclouds.ec2.xml.StringValueHandler;
import org.jclouds.ec2.xml.UnencodeStringValueHandler;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.location.functions.RegionToEndpointOrProviderIfNull;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.EndpointParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.FormParams;
import org.jclouds.rest.annotations.ParamParser;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SinceApiVersion;
import org.jclouds.rest.annotations.VirtualHost;
import org.jclouds.rest.annotations.XMLResponseParser;

import com.google.common.collect.Multimap;

/**
 * Provides access to EC2 Instance Services via their REST API.
 * <p/>
 */
@RequestFilters(FormSigner.class)
@VirtualHost
public interface InstanceApi {

	/**
	* Returns information about instances that you own.
	* <p/>
	* 
	* If you specify one or more instance IDs, Amazon EC2 returns information
	* for those instances. If you do not specify instance IDs, Amazon EC2
	* returns information for all relevant instances. If you specify an invalid
	* instance ID, a fault is returned. If you specify an instance that you do
	* not own, it will not be included in the returned results.
	* <p/>
	* Recently terminated instances might appear in the returned results.This
	* interval is usually less than one hour.
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* 
	* @see #runInstancesInRegion
	* @see #terminateInstancesInRegion
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DescribeInstances.html"
	*      />
	*/
	@Named("DescribeInstances")
	@POST
	@Path("/")
	@FormParams(keys = ACTION, values = "DescribeInstances")
	@XMLResponseParser(DescribeInstancesResponseHandler.class)
	@Fallback(EmptySetOnNotFoundOr404.class)
	Set<? extends Reservation<? extends RunningInstance>> describeInstancesInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@BinderParam(BindInstanceIdsToIndexedFormParams.class) String... instanceIds);

	/**
	* Returns information about instances that you own.
	* <p/>
	*
	* If you specify one or filters, Amazon EC2 returns information for instances
	* matching those filters. If you do not specify any filters, Amazon EC2
	* returns information for all relevant instances. If you specify an invalid
	* filter, a fault is returned. Only instances you own will be included in the
	* results.
	* <p/>
	* Recently terminated instances might appear in the returned results. This
	* interval is usually less than one hour.
	*
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param filter
	*
	* @see #runInstancesInRegion
	* @see #terminateInstancesInRegion
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-DescribeInstances.html"
	*      />
	*/
	@SinceApiVersion("2010-08-31")
	@Named("DescribeInstances")
	@POST
	@Path("/")
	@FormParams(keys = ACTION, values = "DescribeInstances")
	@XMLResponseParser(DescribeInstancesResponseHandler.class)
	@Fallback(EmptySetOnNotFoundOr404.class)
	Set<? extends Reservation<? extends RunningInstance>> describeInstancesInRegionWithFilter(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@BinderParam(BindFiltersToIndexedFormParams.class) Multimap<String, String> filter);

	/**
	* Launches a specified number of instances of an AMI for which you have
	* permissions.
	* <p/>
	* 
	* If Amazon EC2 cannot launch the minimum number AMIs you request, no
	* instances will be launched. If there is insufficient capacity to launch
	* the maximum number of AMIs you request, Amazon EC2 launches the minimum
	* number specified for each AMI and allocate the remaining available
	* instances using round robin.
	* <p/>
	* <h4>Security Groups</h4>
	* <b>Note:</b> Every instance is launched in a security group (created using
	* the CreateSecurityGroup operation.
	* <h4>Key Pair</h4>
	* You can provide an optional key pair ID for each image in the launch
	* request (created using the CreateKeyPair operation). All instances that
	* are created from images that use this key pair will have access to the
	* associated public key at boot. You can use this key to provide secure
	* access to an instance of an image on a per-instance basis. Amazon EC2
	* public images use this feature to provide secure access without passwords.
	* <p/>
	* <b>Note:</b> Launching public images without a key pair ID will leave them
	* inaccessible.
	* <p/>
	* The public key material is made available to the instance at boot time by
	* placing it in the openssh_id.pub file on a logical device that is exposed
	* to the instance as /dev/sda2 (the instance store). The format of this file
	* is suitable for use as an entry within ~/.ssh/authorized_keys (the OpenSSH
	* format). This can be done at boot (e.g., as part of rc.local) allowing for
	* secure access without passwords.
	* <h4>User Data</h4>
	* Optional user data can be provided in the launch request. All instances
	* that collectively comprise the launch request have access to this data.
	* For more information, go the Amazon Elastic Compute Cloud Developer Guide.
	* <h4>Product Codes</h4>
	* 
	* <b>Note:</b> If any of the AMIs have a product code attached for which the
	* user has not subscribed, the RunInstances call will fail.
	* <h4>Kernel</h4>
	* 
	* <b>Important:</b> We strongly recommend using the 2.6.18 Xen stock kernel
	* with High-CPU and High-Memory instances. Although the default Amazon EC2
	* kernels will work, the new kernels provide greater stability and
	* performance for these instance types. For more information about kernels,
	* go the Amazon Elastic Compute Cloud Developer Guide.
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param nullableAvailabilityZone
	*           Specifies the placement constraints (Availability Zones) for
	*           launching the instances. If null, Amazon will determine the best
	*           availability zone to place the instance.
	* @param imageId
	*           Unique ID of a machine image, returned by a call to
	* @param minCount
	*           Minimum number of instances to launch. If the value is more than
	*           Amazon EC2 can launch, no instances a re launched at all.
	*           Constraints: Between 1 and the maximum number allowed for your
	*           account (default: 20).
	* @param maxCount
	*           Maximum number of instances to launch. If the value is more than
	*           Amazon EC2 can launch, the largest possible number above
	*           minCount will be launched instead. Constraints: Between 1 and
	*           the maximum number allowed for your account (default: 20).
	* @see #describeInstancesInRegion
	* @see #terminateInstancesInRegion
	* @see #authorizeSecurityGroupIngressInRegion
	* @see #revokeSecurityGroupIngressInRegion
	* @see #describeSecurityGroupsInRegion
	* @see #createSecurityGroupInRegion
	* @see #createKeyPairInRegion
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-RunInstances.html"
	*      />
	* @see RunInstancesOptions
	*/
	@Named("RunInstances")
	@POST
	@Path("/")
	@FormParams(keys = ACTION, values = "RunInstances")
	@XMLResponseParser(RunInstancesResponseHandler.class)
	Reservation<? extends RunningInstance> runInstancesInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@Nullable @BinderParam(IfNotNullBindAvailabilityZoneToFormParam.class) String nullableAvailabilityZone,
			@FormParam("ImageId") String imageId, @FormParam("MinCount") int minCount,
			@FormParam("MaxCount") int maxCount, RunInstancesOptions... options);

	/**
	* Requests a reboot of one or more instances. This operation is
	* asynchronous; it only queues a request to reboot the specified
	* instance(s). The operation will succeed if the instances are valid and
	* belong to you. Requests to reboot terminated instances are ignored. <h3>
	* Note</h3> If a Linux/UNIX instance does not cleanly shut down within four
	* minutes, Amazon EC2 will perform a hard reboot.
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* 
	* @param instanceIds
	*           Instance ID to reboot.
	* 
	* @see #startInstancesInRegion
	* @see #runInstancesInRegion
	* @see #describeInstancesInRegion
	* @see #terminateInstancesInRegion
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-StopInstances.html"
	*      />
	*/
	@Named("RebootInstances")
	@POST
	@Path("/")
	@FormParams(keys = ACTION, values = "RebootInstances")
	void rebootInstancesInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@BinderParam(BindInstanceIdsToIndexedFormParams.class) String... instanceIds);

	/**
	* Shuts down one or more instances. This operation is idempotent; if you
	* terminate an instance more than once, each call will succeed.
	* <p/>
	* Terminated instances will remain visible after termination (approximately
	* one hour).
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceIds
	*           Instance ID to terminate.
	* @see #describeInstancesInRegion
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-TerminateInstances.html"
	*      />
	*/
	@Named("TerminateInstances")
	@POST
	@Path("/")
	@FormParams(keys = ACTION, values = "TerminateInstances")
	@XMLResponseParser(InstanceStateChangeHandler.class)
	@Fallback(EmptySetOnNotFoundOr404.class)
	Set<? extends InstanceStateChange> terminateInstancesInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@BinderParam(BindInstanceIdsToIndexedFormParams.class) String... instanceIds);

	/**
	* Stops an instance that uses an Amazon EBS volume as its root device.
	* <p/>
	* Instances that use Amazon EBS volumes as their root devices can be quickly
	* stopped and started. When an instance is stopped, the compute resources
	* are released and you are not billed for hourly instance usage. However,
	* your root partition Amazon EBS volume remains, continues to persist your
	* data, and you are charged for Amazon EBS volume usage. You can restart
	* your instance at any time.
	* <h3>Note</h3>
	* Before stopping an instance, make sure it is in a state from which it can
	* be restarted. Stopping an instance does not preserve data stored in RAM.
	* <p/>
	* Performing this operation on an instance that uses an instance store as
	* its root device returns an error.
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param force
	*           Forces the instance to stop. The instance will not have an
	*           opportunity to flush file system caches nor file system meta
	*           data. If you use this option, you must perform file system check
	*           and repair procedures. This option is not recommended for
	*           Windows instances.
	* @param instanceIds
	*           Instance ID to stop.
	* 
	* @see #startInstancesInRegion
	* @see #runInstancesInRegion
	* @see #describeInstancesInRegion
	* @see #terminateInstancesInRegion
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-StopInstances.html"
	*      />
	*/
	@Named("StopInstances")
	@POST
	@Path("/")
	@FormParams(keys = ACTION, values = "StopInstances")
	@XMLResponseParser(InstanceStateChangeHandler.class)
	Set<? extends InstanceStateChange> stopInstancesInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("Force") boolean force,
			@BinderParam(BindInstanceIdsToIndexedFormParams.class) String... instanceIds);

	/**
	* Starts an instance that uses an Amazon EBS volume as its root device.
	* <p/>
	* Instances that use Amazon EBS volumes as their root devices can be quickly
	* stopped and started. When an instance is stopped, the compute resources
	* are released and you are not billed for hourly instance usage. However,
	* your root partition Amazon EBS volume remains, continues to persist your
	* data, and you are charged for Amazon EBS volume usage. You can restart
	* your instance at any time.
	* <h3>Note</h3>
	* Before stopping an instance, make sure it is in a state from which it can
	* be restarted. Stopping an instance does not preserve data stored in RAM.
	* <p/>
	* Performing this operation on an instance that uses an instance store as
	* its root device returns an error.
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceIds
	*           Instance ID to start.
	* 
	* @see #stopInstancesInRegion
	* @see #runInstancesInRegion
	* @see #describeInstancesInRegion
	* @see #terminateInstancesInRegion
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-StartInstances.html"
	*      />
	*/
	@Named("StartInstances")
	@POST
	@Path("/")
	@FormParams(keys = ACTION, values = "StartInstances")
	@XMLResponseParser(InstanceStateChangeHandler.class)
	Set<? extends InstanceStateChange> startInstancesInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@BinderParam(BindInstanceIdsToIndexedFormParams.class) String... instanceIds);

	/**
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to describe the attribute of
	* @return unencoded user data
	*/
	@Named("DescribeInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "DescribeInstanceAttribute", "userData" })
	@XMLResponseParser(UnencodeStringValueHandler.class)
	String getUserDataForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId);

	/**
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to describe the attribute of
	* @return The root device name (e.g., /dev/sda1).
	*/
	@Named("DescribeInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "DescribeInstanceAttribute", "rootDeviceName" })
	@XMLResponseParser(StringValueHandler.class)
	String getRootDeviceNameForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId);

	/**
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to describe the attribute of
	* @return the ID of the RAM disk associated with the AMI.
	*/
	@Named("DescribeInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "DescribeInstanceAttribute", "ramdisk" })
	@XMLResponseParser(StringValueHandler.class)
	String getRamdiskForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId);

	/**
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to describe the attribute of
	* @return the ID of the kernel associated with the AMI.
	*/
	@Named("DescribeInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "DescribeInstanceAttribute", "kernel" })
	@XMLResponseParser(StringValueHandler.class)
	String getKernelForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId);

	/**
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to describe the attribute of
	* @return Specifies whether the instance can be terminated using the APIs.
	*         You must modify this attribute before you can terminate any
	*         "locked" instances from the APIs.
	*/
	@Named("DescribeInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "DescribeInstanceAttribute", "disableApiTermination" })
	@XMLResponseParser(BooleanValueHandler.class)
	boolean isApiTerminationDisabledForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId);

	/**
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to describe the attribute of
	* @return The instance type of the instance.
	*/
	@Named("DescribeInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "DescribeInstanceAttribute", "instanceType" })
	@XMLResponseParser(InstanceTypeHandler.class)
	String getInstanceTypeForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId);

	/**
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to describe the attribute of
	* @return whether the instance's Amazon EBS volumes are stopped or
	*         terminated when the instance is shut down.
	*/
	@Named("DescribeInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "DescribeInstanceAttribute",
			"instanceInitiatedShutdownBehavior" })
	@XMLResponseParser(InstanceInitiatedShutdownBehaviorHandler.class)
	InstanceInitiatedShutdownBehavior getInstanceInitiatedShutdownBehaviorForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId);

	/**
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to describe the attribute of
	* @return Describes the mapping that defines native device names to use when
	*         exposing virtual devices.
	*/
	@Named("DescribeInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "DescribeInstanceAttribute", "blockDeviceMapping" })
	@XMLResponseParser(BlockDeviceMappingHandler.class)
	Map<String, BlockDevice> getBlockDeviceMappingForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId);

	/**
	* Resets an attribute of an instance to its default value.
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to reset the attribute of
	* @return the ID of the RAM disk associated with the AMI.
	*/
	@Named("ResetInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "ResetInstanceAttribute", "ramdisk" })
	void resetRamdiskForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId);

	/**
	* Resets an attribute of an instance to its default value.
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to reset the attribute of
	* @return the ID of the kernel associated with the AMI.
	*/
	@Named("ResetInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "ResetInstanceAttribute", "kernel" })
	void resetKernelForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId);

	/**
	* Sets the userData used for starting the instance.
	* <p/>
	* The instance needs to be in a {@link InstanceState#STOPPED} state, which
	* implies two things:
	* <ol>
	* <li>The instance was launched from an EBS-backed AMI so that it can stop</li>
	* <li>You have stopped and waited for the instance to transition from
	* {@link InstanceState#STOPPING} to {@link InstanceState#STOPPED}</li>
	* </ol>
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to change the attribute of
	* @param unencodedData
	*           unencoded data to set as userData
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-ModifyInstanceAttribute.html"
	*      />
	*/
	@Named("ModifyInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "ModifyInstanceAttribute", "userData" })
	void setUserDataForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId,
			@FormParam("Value") @ParamParser(ConvertUnencodedBytesToBase64EncodedString.class) byte[] unencodedData);

	/**
	* Sets the ramdisk used for starting the instance.
	* <p/>
	* The instance needs to be in a {@link InstanceState#STOPPED} state, which
	* implies two things:
	* <ol>
	* <li>The instance was launched from an EBS-backed AMI so that it can stop</li>
	* <li>You have stopped and waited for the instance to transition from
	* {@link InstanceState#STOPPING} to {@link InstanceState#STOPPED}</li>
	* </ol>
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to change the attribute of
	* @param ramdisk
	*           ramdisk used to start the instance
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-ModifyInstanceAttribute.html"
	*      />
	*/
	@Named("ModifyInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "ModifyInstanceAttribute", "ramdisk" })
	void setRamdiskForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId, @FormParam("Value") String ramdisk);

	/**
	* Sets the kernelId used for starting the instance.
	* <p/>
	* The instance needs to be in a {@link InstanceState#STOPPED} state, which
	* implies two things:
	* <ol>
	* <li>The instance was launched from an EBS-backed AMI so that it can stop</li>
	* <li>You have stopped and waited for the instance to transition from
	* {@link InstanceState#STOPPING} to {@link InstanceState#STOPPED}</li>
	* </ol>
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to change the attribute of
	* @param kernel
	*           kernelId used to start the instance
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-ModifyInstanceAttribute.html"
	*      />
	*/
	@Named("ModifyInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "ModifyInstanceAttribute", "kernel" })
	void setKernelForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId, @FormParam("Value") String kernel);

	/**
	* This command works while the instance is running and controls whether or
	* not the api can be used to terminate the instance.
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to reset the attribute of
	* @param apiTerminationDisabled
	*           true to disable api termination
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-ModifyInstanceAttribute.html"
	*      />
	*/
	@Named("ModifyInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "ModifyInstanceAttribute", "disableApiTermination" })
	void setApiTerminationDisabledForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId, @FormParam("Value") boolean apiTerminationDisabled);

	/**
	* Sets the instanceType used for starting the instance.
	* <p/>
	* The instance needs to be in a {@link InstanceState#STOPPED} state, which
	* implies two things:
	* <ol>
	* <li>The instance was launched from an EBS-backed AMI so that it can stop</li>
	* <li>You have stopped and waited for the instance to transition from
	* {@link InstanceState#STOPPING} to {@link InstanceState#STOPPED}</li>
	* </ol>
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to change the attribute of
	* @param instanceType
	*           instanceType used to start the instance
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-ModifyInstanceAttribute.html"
	*      />
	*/
	@Named("ModifyInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "ModifyInstanceAttribute", "instanceType" })
	void setInstanceTypeForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId, @FormParam("Value") String instanceType);

	/**
	* Specifies whether the instance's Amazon EBS volumes are stopped or
	* terminated when the instance is shut down.
	* <p/>
	* The instance needs to be in a {@link InstanceState#STOPPED} state, which
	* implies two things:
	* <ol>
	* <li>The instance was launched from an EBS-backed AMI so that it can stop</li>
	* <li>You have stopped and waited for the instance to transition from
	* {@link InstanceState#STOPPING} to {@link InstanceState#STOPPED}</li>
	* </ol>
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to change the attribute of
	* @param instanceInitiatedShutdownBehavior
	*           whether the instance's Amazon EBS volumes are stopped or
	*           terminated when the instance is shut down.
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-ModifyInstanceAttribute.html"
	*      />
	*/
	@Named("ModifyInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION, "Attribute" }, values = { "ModifyInstanceAttribute",
			"instanceInitiatedShutdownBehavior" })
	void setInstanceInitiatedShutdownBehaviorForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId,
			@FormParam("Value") InstanceInitiatedShutdownBehavior instanceInitiatedShutdownBehavior);

	/**
	* Sets the blockDeviceMapping used for an instance.
	* <p/>
	* The instance needs to be in a {@link InstanceState#STOPPED} state, which
	* implies two things:
	* <ol>
	* <li>The instance was launched from an EBS-backed AMI so that it can stop</li>
	* <li>You have stopped and waited for the instance to transition from
	* {@link InstanceState#STOPPING} to {@link InstanceState#STOPPED}</li>
	* </ol>
	* 
	* To create the instances of {@link BlockDevice}, the
	* constructor can be used with the following parameters:
	* {@link BlockDevice#EbsBlockDevice(String, String, boolean)}
	* , that are:
	* <ol>
	* <li>Volume id (required), for instance, "vol-blah"</li>
	* <li>Device name (optional), for instance, "/dev/sda1". To find out more
	* about device names, read the next paragraph.</li>
	* <li>Delete on termination flag (optional), which defines whether the
	* volume will be deleted upon instance's termination.</li>
	* </ol>
	* <p/>
	* Note that the device names between Linux and Windows differ. For Linux,
	* ensure that your device name is in the form /dev/sd[a-z] . For example,
	* /dev/sda , /dev/sdb and /dev/sdh are all valid device names.
	* <p/>
	* For Windows, the root device is still referred to as /dev/sda1 . For other
	* devices, ensure that they are in the form /xvd[c-p] . For example, /xvde ,
	* /xvdf and /xvdp are all valid Windows device names.
	* <p/>
	* <b>NOTE</b>: As of now 02/20/2010, this command only works to change the
	* DeleteOnTermination property of the device. The volume must be
	* <i>attached</i> to a stopped instance.
	* 
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to change the attribute of
	* @param blockDeviceMapping
	*           blockDeviceMapping used to start the instance
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-ModifyInstanceAttribute.html"
	*      />
	*/
	@Named("ModifyInstanceAttribute")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION }, values = { "ModifyInstanceAttribute" })
	void setBlockDeviceMappingForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId,
			@BinderParam(BindBlockDeviceMappingToIndexedFormParams.class) Map<String, BlockDevice> blockDeviceMapping);

	/**
	* Retrieves console output for the specified instance.
	*
	* Instance console output is buffered and posted shortly after instance boot, reboot, and termination. Amazon EC2 preserves
	* the most recent 64 KB output which will be available for at least one hour after the most recent post.
	*
	* @param region
	*           Instances are tied to Availability Zones. However, the instance
	*           ID is tied to the Region.
	* @param instanceId
	*           which instance to retrieve console output for
	* @return The console output
	* @see <a href="http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/ApiReference-query-GetConsoleOutput.html">
	*       ApiReference query GetConsoleOutput</a>
	*/
	@Named("GetConsoleOutput")
	@POST
	@Path("/")
	@FormParams(keys = { ACTION }, values = { "GetConsoleOutput" })
	@XMLResponseParser(GetConsoleOutputResponseHandler.class)
	String getConsoleOutputForInstanceInRegion(
			@EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
			@FormParam("InstanceId") String instanceId);
}
