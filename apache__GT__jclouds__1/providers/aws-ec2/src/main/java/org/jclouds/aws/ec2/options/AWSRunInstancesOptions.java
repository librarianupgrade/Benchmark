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
package org.jclouds.aws.ec2.options;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.jclouds.aws.ec2.domain.LaunchSpecification;
import org.jclouds.ec2.domain.BlockDeviceMapping;
import org.jclouds.ec2.options.RunInstancesOptions;
import org.jclouds.rest.annotations.SinceApiVersion;

import com.google.common.collect.ImmutableSet;

/**
 * Contains options supported in the Form API for the RunInstances operation. <h2>
 * Usage</h2> The recommended way to instantiate a RunInstancesOptions object is to statically
 * import RunInstancesOptions.Builder.* and invoke a static creation method followed by an instance
 * mutator (if needed):
 * <p/>
 * <code>
 * import static org.jclouds.aws.ec2.options.RunInstancesOptions.Builder.*
 * <p/>
 * EC2Api connection = // get connection
 * Future<ReservationInfo> instances = connection.runInstances(executableBy("123125").imageIds(1000, 1004));
 * <code>
 * 
 * @see <a href=
 *      "http://docs.amazonwebservices.com/AWSEC2/latest/APIReference/index.html?ApiReference-form-RunInstances.html"
 *      />
 */
public class AWSRunInstancesOptions extends RunInstancesOptions {
	private LaunchSpecification.Builder launchSpecificationBuilder = LaunchSpecification.builder();
	public static final AWSRunInstancesOptions NONE = new AWSRunInstancesOptions();

	/**
	* Specifies the name of an existing placement group you want to launch the instance into (for
	* cluster compute instances).
	* 
	* @param placementGroup
	*           name of an existing placement group
	*/
	public AWSRunInstancesOptions inPlacementGroup(String placementGroup) {
		formParameters.put("Placement.GroupName", checkNotNull(placementGroup, "placementGroup"));
		return this;
	}

	/**
	* Specifies the tenancy of the instance within which to launch the instance(s).
	*/
	public AWSRunInstancesOptions withTenancy(Tenancy tenancy) {
		formParameters.put("Placement.Tenancy", checkNotNull(tenancy, "tenancy").toString());
		return this;
	}

	/**
	* Specifies the ID of the dedicated host on which the instance should resist.
	*/
	public AWSRunInstancesOptions withDedicatedHostId(String hostId) {
		formParameters.put("Placement.HostId", checkNotNull(hostId, "hostId"));
		return this;
	}

	/**
	* Enables monitoring for the instance.
	*/
	public AWSRunInstancesOptions enableMonitoring() {
		formParameters.put("Monitoring.Enabled", "true");
		launchSpecificationBuilder.monitoringEnabled(true);
		return this;
	}

	/**
	* Specifies the subnet ID within which to launch the instance(s) for Amazon Virtual Private
	* Cloud.
	*/
	public AWSRunInstancesOptions withSubnetId(String subnetId) {
		formParameters.put("SubnetId", checkNotNull(subnetId, "subnetId"));
		return this;
	}

	public AWSRunInstancesOptions withSecurityGroupId(String securityGroup) {
		return withSecurityGroupIds(securityGroup);
	}

	public AWSRunInstancesOptions withSecurityGroupIds(Iterable<String> securityGroupIds) {
		launchSpecificationBuilder.securityGroupIds(securityGroupIds);
		indexFormValuesWithPrefix("SecurityGroupId", securityGroupIds);
		return this;
	}

	public AWSRunInstancesOptions withSecurityGroupIds(String... securityGroupIds) {
		return withSecurityGroupIds(ImmutableSet.copyOf(securityGroupIds));
	}

	/**
	* Amazon resource name (ARN) of the IAM Instance Profile (IIP) to associate with the instances.
	* 
	* @see org.jclouds.aws.ec2.domain.AWSRunningInstance#getIAMInstanceProfile()
	*/
	@SinceApiVersion("2012-06-01")
	public AWSRunInstancesOptions withIAMInstanceProfileArn(String arn) {
		formParameters.put("IamInstanceProfile.Arn", checkNotNull(arn, "arn"));
		return this;
	}

	/**
	* The name of the IAM Instance Profile (IIP) to associate with the instances.
	* 
	* @see org.jclouds.aws.ec2.domain.AWSRunningInstance#getIAMInstanceProfile()
	*/
	@SinceApiVersion("2012-06-01")
	public AWSRunInstancesOptions withIAMInstanceProfileName(String name) {
		formParameters.put("IamInstanceProfile.Name", checkNotNull(name, "name"));
		return this;
	}

	/**
	* The primary IP address for VPC instance. You must specify a value from the IP address range of the subnet.
	*
	* @see org.jclouds.aws.ec2.domain.AWSRunningInstance#getPrivateIpAddress()
	*/
	public AWSRunInstancesOptions withPrivateIpAddress(String address) {
		formParameters.put("PrivateIpAddress", checkNotNull(address, "address"));
		return this;
	}

	public static class Builder extends RunInstancesOptions.Builder {

		/**
		 * @see AWSRunInstancesOptions#withSecurityGroupId(String)
		 */
		public static AWSRunInstancesOptions withSecurityGroupId(String securityGroup) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withSecurityGroupId(securityGroup);
		}

		/**
		 * @see AWSRunInstancesOptions#inPlacementGroup(String)
		 */
		public static AWSRunInstancesOptions inPlacementGroup(String placementGroup) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.inPlacementGroup(placementGroup);
		}

		/**
		 * @see AWSRunInstancesOptions#withTenancy(Tenancy)
		 */
		public static AWSRunInstancesOptions withTenancy(Tenancy tenancy) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withTenancy(tenancy);
		}

		/**
		 * @see AWSRunInstancesOptions#withDedicatedHostId(String)
		 */
		public static AWSRunInstancesOptions withDedicatedHostId(String hostId) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withDedicatedHostId(hostId);
		}

		/**
		 * @see AWSRunInstancesOptions#enableMonitoring()
		 */
		public static AWSRunInstancesOptions enableMonitoring() {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.enableMonitoring();
		}

		/**
		 * @see AWSRunInstancesOptions#withSubnetId(String)
		 */
		public static AWSRunInstancesOptions withSubnetId(String subnetId) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withSubnetId(subnetId);
		}

		/**
		 * @see AWSRunInstancesOptions#withIAMInstanceProfileArn(String)
		 */
		public static AWSRunInstancesOptions withIAMInstanceProfileArn(String arn) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withIAMInstanceProfileArn(arn);
		}

		/**
		 * @see AWSRunInstancesOptions#withIAMInstanceProfileName(String)
		 */
		public static AWSRunInstancesOptions withIAMInstanceProfileName(String id) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withIAMInstanceProfileName(id);
		}

		/**
		 * @see AWSRunInstancesOptions#withKeyName(String)
		 */
		public static AWSRunInstancesOptions withKeyName(String keyName) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withKeyName(keyName);
		}

		/**
		 * @see AWSRunInstancesOptions#withSecurityGroup(String)
		 */
		public static AWSRunInstancesOptions withSecurityGroup(String securityGroup) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withSecurityGroup(securityGroup);
		}

		/**
		 * @see AWSRunInstancesOptions#withUserData(byte [])
		 */
		public static AWSRunInstancesOptions withUserData(byte[] unencodedData) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withUserData(unencodedData);
		}

		/**
		 * @see AWSRunInstancesOptions#asType(InstanceType)
		 */
		public static AWSRunInstancesOptions asType(String instanceType) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.asType(instanceType);
		}

		/**
		 * @see AWSRunInstancesOptions#withKernelId(String)
		 */
		public static AWSRunInstancesOptions withKernelId(String kernelId) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withKernelId(kernelId);
		}

		/**
		 * @see AWSRunInstancesOptions#withRamdisk(String)
		 */
		public static AWSRunInstancesOptions withRamdisk(String ramdiskId) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withRamdisk(ramdiskId);
		}

		/**
		 * @see AWSRunInstancesOptions#withBlockDeviceMappings(Set<BlockDeviceMapping> mappings)
		 */
		public static AWSRunInstancesOptions withBlockDeviceMappings(Set<? extends BlockDeviceMapping> mappings) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withBlockDeviceMappings(mappings);
		}

		/**
		 * @see AWSRunInstancesOptions#withPrivateIpAddress(String)
		 */
		public static AWSRunInstancesOptions withPrivateIpAdress(String address) {
			AWSRunInstancesOptions options = new AWSRunInstancesOptions();
			return options.withPrivateIpAddress(address);
		}

	}

	@Override
	public AWSRunInstancesOptions withBlockDeviceMappings(Set<? extends BlockDeviceMapping> mappings) {
		launchSpecificationBuilder.blockDeviceMappings(mappings);
		return AWSRunInstancesOptions.class.cast(super.withBlockDeviceMappings(mappings));
	}

	@Override
	public AWSRunInstancesOptions withKernelId(String kernelId) {
		launchSpecificationBuilder.kernelId(kernelId);
		return AWSRunInstancesOptions.class.cast(super.withKernelId(kernelId));
	}

	@Override
	public AWSRunInstancesOptions withKeyName(String keyName) {
		launchSpecificationBuilder.keyName(keyName);
		return AWSRunInstancesOptions.class.cast(super.withKeyName(keyName));
	}

	@Override
	public AWSRunInstancesOptions withRamdisk(String ramDiskId) {
		launchSpecificationBuilder.ramdiskId(ramDiskId);
		return AWSRunInstancesOptions.class.cast(super.withRamdisk(ramDiskId));
	}

	@Override
	public AWSRunInstancesOptions withSecurityGroup(String securityGroup) {
		launchSpecificationBuilder.securityGroupName(securityGroup);
		return AWSRunInstancesOptions.class.cast(super.withSecurityGroup(securityGroup));
	}

	@Override
	public AWSRunInstancesOptions withSecurityGroups(Iterable<String> securityGroups) {
		launchSpecificationBuilder.securityGroupNames(securityGroups);
		return AWSRunInstancesOptions.class.cast(super.withSecurityGroups(securityGroups));
	}

	@Override
	public AWSRunInstancesOptions withSecurityGroups(String... securityGroups) {
		launchSpecificationBuilder.securityGroupNames(ImmutableSet.copyOf(securityGroups));
		return AWSRunInstancesOptions.class.cast(super.withSecurityGroups(securityGroups));
	}

	@Override
	public AWSRunInstancesOptions withUserData(byte[] unencodedData) {
		launchSpecificationBuilder.userData(unencodedData);
		return AWSRunInstancesOptions.class.cast(super.withUserData(unencodedData));
	}

	@Override
	public AWSRunInstancesOptions asType(String type) {
		launchSpecificationBuilder.instanceType(type);
		return AWSRunInstancesOptions.class.cast(super.asType(type));
	}

	public synchronized LaunchSpecification.Builder getLaunchSpecificationBuilder() {
		try {
			return launchSpecificationBuilder.imageId("fake").build().toBuilder().imageId(null);
		} finally {
			launchSpecificationBuilder.imageId(null);
		}
	}
}
