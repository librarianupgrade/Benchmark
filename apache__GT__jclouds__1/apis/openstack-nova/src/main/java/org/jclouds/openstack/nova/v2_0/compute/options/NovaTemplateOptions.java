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
package org.jclouds.openstack.nova.v2_0.compute.options;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.openstack.nova.v2_0.domain.Network;
import org.jclouds.scriptbuilder.domain.Statement;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

/**
 * Contains options supported in the {@code ComputeService#runNode} operation on the
 * "openstack-nova" provider. <h2>Usage</h2> The recommended way to instantiate a
 * NovaTemplateOptions object is to statically import NovaTemplateOptions.* and invoke a static
 * creation method followed by an instance mutator (if needed):
 * <p/>
 * <code>
 * import static org.jclouds.aws.ec2.compute.options.NovaTemplateOptions.Builder.*;
 * <p/>
 * ComputeService api = // get connection
 * templateBuilder.options(inboundPorts(22, 80, 8080, 443));
 * Set<? extends NodeMetadata> set = api.createNodesInGroup(tag, 2, templateBuilder.build());
 * <code>
 */
public class NovaTemplateOptions extends TemplateOptions implements Cloneable {
	@Override
	public NovaTemplateOptions clone() {
		NovaTemplateOptions options = new NovaTemplateOptions();
		copyTo(options);
		return options;
	}

	@Override
	public void copyTo(TemplateOptions to) {
		super.copyTo(to);
		if (to instanceof NovaTemplateOptions) {
			NovaTemplateOptions eTo = NovaTemplateOptions.class.cast(to);
			eTo.autoAssignFloatingIp(shouldAutoAssignFloatingIp());
			if (getFloatingIpPoolNames().isPresent())
				eTo.floatingIpPoolNames(getFloatingIpPoolNames().get());
			if (getSecurityGroupNames().isPresent())
				eTo.securityGroupNames(getSecurityGroupNames().get());
			eTo.generateKeyPair(shouldGenerateKeyPair());
			eTo.keyPairName(getKeyPairName());
			if (getUserData() != null) {
				eTo.userData(getUserData());
			}
			if (getDiskConfig() != null) {
				eTo.diskConfig(getDiskConfig());
			}

			eTo.configDrive(getConfigDrive());
			eTo.novaNetworks(getNovaNetworks());
			eTo.availabilityZone(getAvailabilityZone());
		}
	}

	protected boolean autoAssignFloatingIp = false;
	protected Optional<Set<String>> floatingIpPoolNames = Optional.absent();
	protected boolean generateKeyPair = false;
	protected String keyPairName;
	protected byte[] userData;
	protected String diskConfig;
	protected boolean configDrive;
	protected Set<Network> novaNetworks;
	protected String availabilityZone;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		NovaTemplateOptions that = NovaTemplateOptions.class.cast(o);
		return super.equals(that) && equal(this.autoAssignFloatingIp, that.autoAssignFloatingIp)
				&& equal(this.floatingIpPoolNames, that.floatingIpPoolNames)
				&& equal(this.generateKeyPair, that.generateKeyPair) && equal(this.keyPairName, that.keyPairName)
				&& Arrays.equals(this.userData, that.userData) && equal(this.diskConfig, that.diskConfig)
				&& equal(this.configDrive, that.configDrive) && equal(this.novaNetworks, that.novaNetworks)
				&& equal(this.availabilityZone, that.availabilityZone);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), autoAssignFloatingIp, floatingIpPoolNames, generateKeyPair,
				keyPairName, Arrays.hashCode(userData), diskConfig, configDrive, novaNetworks, availabilityZone);
	}

	@Override
	public ToStringHelper string() {
		ToStringHelper toString = super.string();
		if (!autoAssignFloatingIp)
			toString.add("autoAssignFloatingIp", autoAssignFloatingIp);
		if (floatingIpPoolNames.isPresent())
			toString.add("floatingIpPoolNames", floatingIpPoolNames.get());
		if (generateKeyPair)
			toString.add("generateKeyPair", generateKeyPair);
		toString.add("keyPairName", keyPairName);
		toString.add("userData", userData);
		toString.add("diskConfig", diskConfig);
		toString.add("configDrive", configDrive);
		toString.add("novaNetworks", novaNetworks);
		toString.add("availabilityZone", availabilityZone);
		return toString;
	}

	public static final NovaTemplateOptions NONE = new NovaTemplateOptions();

	/**
	* @see #getFloatingIpPoolNames()
	*/
	public NovaTemplateOptions autoAssignFloatingIp(boolean enable) {
		this.autoAssignFloatingIp = enable;
		return this;
	}

	/**
	* @see #getFloatingIpPoolNames()
	*/
	public NovaTemplateOptions floatingIpPoolNames(String... floatingIpPoolNames) {
		return floatingIpPoolNames(ImmutableSet.copyOf(checkNotNull(floatingIpPoolNames, "floatingIpPoolNames")));
	}

	/**
	* @see #getFloatingIpPoolNames()
	*/
	public NovaTemplateOptions floatingIpPoolNames(Iterable<String> floatingIpPoolNames) {
		for (String groupName : checkNotNull(floatingIpPoolNames, "floatingIpPoolNames"))
			checkNotNull(emptyToNull(groupName), "all floating-ip-pool-names must be non-empty");
		this.floatingIpPoolNames = Optional.<Set<String>>of(ImmutableSet.copyOf(floatingIpPoolNames));
		return this;
	}

	/**
	* @see #shouldGenerateKeyPair()
	*/
	public NovaTemplateOptions generateKeyPair(boolean enable) {
		this.generateKeyPair = enable;
		return this;
	}

	/**
	* @see #shouldGenerateKeyPair()
	*/
	public NovaTemplateOptions keyPairName(String keyPairName) {
		this.keyPairName = keyPairName;
		return this;
	}

	/**
	*
	* @see org.jclouds.openstack.nova.v2_0.options.CreateServerOptions#getSecurityGroupNames
	* @deprecated Use @link {@link TemplateOptions#securityGroups(String...)} instead. To be removed in jclouds 2.0.
	*/
	@Deprecated
	public NovaTemplateOptions securityGroupNames(String... securityGroupNames) {
		return securityGroupNames(ImmutableSet.copyOf(checkNotNull(securityGroupNames, "securityGroupNames")));
	}

	/**
	* @see org.jclouds.openstack.nova.v2_0.options.CreateServerOptions#getSecurityGroupNames
	* @deprecated Use {@link TemplateOptions#securityGroups(Iterable)} instead. To be removed in jclouds 2.0.
	*/
	@Deprecated
	public NovaTemplateOptions securityGroupNames(Iterable<String> securityGroupNames) {
		for (String groupName : checkNotNull(securityGroupNames, "securityGroupNames"))
			checkNotNull(emptyToNull(groupName), "all security groups must be non-empty");
		securityGroups(securityGroupNames);
		return this;
	}

	/**
	* <h3>Note</h3>
	*
	* This requires that {@link NovaApi#getExtensionForRegion(String)} to return
	* {@link Optional#isPresent present}
	*
	* @return true if auto assignment of a floating ip to each vm is enabled
	*/
	public boolean shouldAutoAssignFloatingIp() {
		return autoAssignFloatingIp;
	}

	/**
	* @see CreateServerOptions#getAvailabilityZone()
	*/
	public NovaTemplateOptions availabilityZone(String availabilityZone) {
		this.availabilityZone = availabilityZone;
		return this;
	}

	/**
	* The floating IP pool name(s) to use when allocating a FloatingIP. Applicable
	* only if #shouldAutoAssignFloatingIp() returns true. If not set will attempt to
	* use whatever FloatingIP(s) can be found regardless of which pool they originated
	* from
	*
	* @return floating-ip-pool names to use
	*/
	public Optional<Set<String>> getFloatingIpPoolNames() {
		return floatingIpPoolNames;
	}

	/**
	* Specifies the keypair used to run instances with
	* @return the keypair to be used
	*/
	public String getKeyPairName() {
		return keyPairName;
	}

	/**
	* <h3>Note</h3>
	*
	* This requires that {@link NovaApi#getKeyPairExtensionApi(String)} to return
	* {@link Optional#isPresent present}
	*
	* @return true if auto generation of keypairs is enabled
	*/
	public boolean shouldGenerateKeyPair() {
		return generateKeyPair;
	}

	/**
	* if unset, generate a default group prefixed with {@link jclouds#} according
	* to {@link #getInboundPorts()}
	*
	* @see org.jclouds.openstack.nova.v2_0.options.CreateServerOptions#getSecurityGroupNames
	* @deprecated Use {@link TemplateOptions#getGroups()} instead. To be removed in jclouds 2.0.
	*/
	@Deprecated
	public Optional<Set<String>> getSecurityGroupNames() {
		return getGroups().isEmpty() ? Optional.<Set<String>>absent() : Optional.of(getGroups());
	}

	public byte[] getUserData() {
		return userData;
	}

	/**
	* @see CreateServerOptions#getDiskConfig()
	*/
	public String getDiskConfig() {
		return diskConfig;
	}

	/**
	* @see CreateServerOptions#getConfigDrive()
	*/
	public boolean getConfigDrive() {
		return configDrive;
	}

	/**
	* @see CreateServerOptions#getNetworks()
	*/
	public Set<Network> getNovaNetworks() {
		return novaNetworks;
	}

	/**
	* @see CreateServerOptions#getAvailabilityZone()
	*/
	public String getAvailabilityZone() {
		return availabilityZone;
	}

	public static class Builder {

		/**
		 * @see NovaTemplateOptions#shouldAutoAssignFloatingIp()
		 */
		public static NovaTemplateOptions autoAssignFloatingIp(boolean enable) {
			return new NovaTemplateOptions().autoAssignFloatingIp(enable);
		}

		/**
		 * @see #getFloatingIpPoolNames()
		 */
		public NovaTemplateOptions floatingIpPoolNames(String... floatingIpPoolNames) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.floatingIpPoolNames(floatingIpPoolNames));
		}

		/**
		 * @see #getFloatingIpPoolNames()
		 */
		public NovaTemplateOptions floatingIpPoolNames(Iterable<String> floatingIpPoolNames) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.floatingIpPoolNames(floatingIpPoolNames));
		}

		/**
		 * @see NovaTemplateOptions#shouldGenerateKeyPair()
		 */
		public static NovaTemplateOptions generateKeyPair(boolean enable) {
			return new NovaTemplateOptions().generateKeyPair(enable);
		}

		/**
		 * @see NovaTemplateOptions#getKeyPairName()
		 */
		public static NovaTemplateOptions keyPairName(String keyPairName) {
			return new NovaTemplateOptions().keyPairName(keyPairName);
		}

		/**
		 * @see org.jclouds.openstack.nova.v2_0.options.CreateServerOptions#getSecurityGroupNames
		 */
		public static NovaTemplateOptions securityGroupNames(String... groupNames) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.securityGroupNames(groupNames));
		}

		/**
		 * @see org.jclouds.openstack.nova.v2_0.options.CreateServerOptions#getSecurityGroupNames
		 */
		public static NovaTemplateOptions securityGroupNames(Iterable<String> groupNames) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.securityGroupNames(groupNames));
		}

		// methods that only facilitate returning the correct object type

		/**
		 * @see TemplateOptions#inboundPorts
		 */
		public static NovaTemplateOptions inboundPorts(int... ports) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.inboundPorts(ports));
		}

		/**
		 * @see TemplateOptions#port
		 */
		public static NovaTemplateOptions blockOnPort(int port, int seconds) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.blockOnPort(port, seconds));
		}

		/**
		 * @see TemplateOptions#installPrivateKey
		 */
		public static NovaTemplateOptions installPrivateKey(String rsaKey) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.installPrivateKey(rsaKey));
		}

		/**
		 * @see TemplateOptions#authorizePublicKey
		 */
		public static NovaTemplateOptions authorizePublicKey(String rsaKey) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.authorizePublicKey(rsaKey));
		}

		/**
		 * @see TemplateOptions#userMetadata
		 */
		public static NovaTemplateOptions userMetadata(Map<String, String> userMetadata) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.userMetadata(userMetadata));
		}

		/**
		 * @see TemplateOptions#nodeNames(Iterable)
		 */
		public static NovaTemplateOptions nodeNames(Iterable<String> nodeNames) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.nodeNames(nodeNames));
		}

		/**
		 * @see TemplateOptions#networks(Iterable)
		 */
		public static NovaTemplateOptions networks(Iterable<String> networks) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.networks(networks));
		}

		/**
		 * @see TemplateOptions#overrideLoginUser
		 */
		public static NovaTemplateOptions overrideLoginUser(String user) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return options.overrideLoginUser(user);
		}

		/**
		 * @see TemplateOptions#overrideLoginPassword
		 */
		public static NovaTemplateOptions overrideLoginPassword(String password) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return options.overrideLoginPassword(password);
		}

		/**
		 * @see TemplateOptions#overrideLoginPrivateKey
		 */
		public static NovaTemplateOptions overrideLoginPrivateKey(String privateKey) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return options.overrideLoginPrivateKey(privateKey);
		}

		/**
		 * @see TemplateOptions#overrideAuthenticateSudo
		 */
		public static NovaTemplateOptions overrideAuthenticateSudo(boolean authenticateSudo) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return options.overrideAuthenticateSudo(authenticateSudo);
		}

		/**
		 * @see TemplateOptions#overrideLoginCredentials
		 */
		public static NovaTemplateOptions overrideLoginCredentials(LoginCredentials credentials) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return options.overrideLoginCredentials(credentials);
		}

		/**
		 * @see TemplateOptions#blockUntilRunning
		 */
		public static NovaTemplateOptions blockUntilRunning(boolean blockUntilRunning) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return options.blockUntilRunning(blockUntilRunning);
		}

		/**
		 * @see NovaTemplateOptions#userData
		 */
		public static NovaTemplateOptions userData(byte[] userData) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.userData(userData));
		}

		/**
		 * @see org.jclouds.openstack.nova.v2_0.options.CreateServerOptions#getDiskConfig()
		 */
		public static NovaTemplateOptions diskConfig(String diskConfig) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.diskConfig(diskConfig));
		}

		/**
		 * @see org.jclouds.openstack.nova.v2_0.options.CreateServerOptions#getConfigDrive()
		 */
		public static NovaTemplateOptions configDrive(boolean configDrive) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.configDrive(configDrive));
		}

		/**
		 * @see org.jclouds.openstack.nova.v2_0.options.CreateServerOptions#getNetworks()
		 */
		public static NovaTemplateOptions novaNetworks(Set<Network> novaNetworks) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return NovaTemplateOptions.class.cast(options.novaNetworks(novaNetworks));
		}

		/**
		 * @see NovaTemplateOptions#getAvailabilityZone()
		 */
		public static NovaTemplateOptions availabilityZone(String availabilityZone) {
			NovaTemplateOptions options = new NovaTemplateOptions();
			return options.availabilityZone(availabilityZone);
		}
	}

	// methods that only facilitate returning the correct object type

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions blockOnPort(int port, int seconds) {
		return NovaTemplateOptions.class.cast(super.blockOnPort(port, seconds));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions inboundPorts(int... ports) {
		return NovaTemplateOptions.class.cast(super.inboundPorts(ports));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions authorizePublicKey(String publicKey) {
		return NovaTemplateOptions.class.cast(super.authorizePublicKey(publicKey));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions installPrivateKey(String privateKey) {
		return NovaTemplateOptions.class.cast(super.installPrivateKey(privateKey));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions blockUntilRunning(boolean blockUntilRunning) {
		return NovaTemplateOptions.class.cast(super.blockUntilRunning(blockUntilRunning));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions dontAuthorizePublicKey() {
		return NovaTemplateOptions.class.cast(super.dontAuthorizePublicKey());
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions nameTask(String name) {
		return NovaTemplateOptions.class.cast(super.nameTask(name));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions runAsRoot(boolean runAsRoot) {
		return NovaTemplateOptions.class.cast(super.runAsRoot(runAsRoot));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions runScript(Statement script) {
		return NovaTemplateOptions.class.cast(super.runScript(script));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions overrideLoginCredentials(LoginCredentials overridingCredentials) {
		return NovaTemplateOptions.class.cast(super.overrideLoginCredentials(overridingCredentials));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions overrideLoginPassword(String password) {
		return NovaTemplateOptions.class.cast(super.overrideLoginPassword(password));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions overrideLoginPrivateKey(String privateKey) {
		return NovaTemplateOptions.class.cast(super.overrideLoginPrivateKey(privateKey));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions overrideLoginUser(String loginUser) {
		return NovaTemplateOptions.class.cast(super.overrideLoginUser(loginUser));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions overrideAuthenticateSudo(boolean authenticateSudo) {
		return NovaTemplateOptions.class.cast(super.overrideAuthenticateSudo(authenticateSudo));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions userMetadata(Map<String, String> userMetadata) {
		return NovaTemplateOptions.class.cast(super.userMetadata(userMetadata));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions userMetadata(String key, String value) {
		return NovaTemplateOptions.class.cast(super.userMetadata(key, value));
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public NovaTemplateOptions nodeNames(Iterable<String> nodeNames) {
		return NovaTemplateOptions.class.cast(super.nodeNames(nodeNames));
	}

	/**
	* <br>Ensures NovaTemplateOptions can work with networks specified as Strings.
	* Also provides for compatibility with the abstraction layer.
	*/
	@Override
	public NovaTemplateOptions networks(Iterable<String> networks) {
		return NovaTemplateOptions.class.cast(super.networks(networks));
	}

	/**
	* <br>Ensures NovaTemplateOptions can work with networks specified as Strings.
	* Also provides for compatibility with the abstraction layer.
	*/
	@Override
	public NovaTemplateOptions networks(String... networks) {
		return NovaTemplateOptions.class.cast(super.networks(networks));
	}

	/**
	* User data as bytes (not base64-encoded)
	*/
	public NovaTemplateOptions userData(byte[] userData) {
		// This limit may not be needed for nova
		checkArgument(checkNotNull(userData, "userData").length <= 16 * 1024, "userData cannot be larger than 16kb");
		this.userData = userData;
		return this;
	}

	/**
	* @see CreateServerOptions#getDiskConfig()
	*/
	public NovaTemplateOptions diskConfig(String diskConfig) {
		this.diskConfig = diskConfig;
		return this;
	}

	/**
	* OpenStack can be configured to write metadata to a special configuration drive that will be
	* attached to the instance when it boots. The instance can retrieve any information that would
	* normally be available through the metadata service by mounting this disk and reading files from it.
	* To enable the config drive, set this parameter to "true".
	* This has to be enabled for user data cases.
	* @see CreateServerOptions#getConfigDrive()
	*/
	public NovaTemplateOptions configDrive(boolean configDrive) {
		this.configDrive = configDrive;
		return this;
	}

	/**
	* @param novaNetworks The list of network declarations.
	* Nova-specific network declarations allow for specifying network UUIDs, port UUIDs, and fixed IPs.
	* Unline {@link #networks(Iterable)} this supports setting additional network parameters and not just network UUIDs.
	* @see CreateServerOptions#getNetworks()
	*/
	public NovaTemplateOptions novaNetworks(Set<Network> novaNetworks) {
		this.novaNetworks = novaNetworks;
		return this;
	}
}
