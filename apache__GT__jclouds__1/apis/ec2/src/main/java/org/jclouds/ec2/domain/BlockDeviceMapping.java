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
package org.jclouds.ec2.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

import org.jclouds.javax.annotation.Nullable;

public class BlockDeviceMapping implements Comparable<BlockDeviceMapping> {
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String deviceName;
		private String virtualName;
		private String snapshotId;
		private Integer sizeInGib;
		private Boolean noDevice;
		private Boolean deleteOnTermination;
		private String volumeType;
		private Integer iops;
		private Boolean encrypted;

		public Builder deviceName(String deviceName) {
			this.deviceName = deviceName;
			return this;
		}

		public Builder virtualName(String virtualName) {
			this.virtualName = virtualName;
			return this;
		}

		public Builder snapshotId(String snapshotId) {
			this.snapshotId = snapshotId;
			return this;
		}

		public Builder sizeInGib(Integer sizeInGib) {
			this.sizeInGib = sizeInGib;
			return this;
		}

		public Builder noDevice(Boolean noDevice) {
			this.noDevice = noDevice;
			return this;
		}

		public Builder deleteOnTermination(Boolean deleteOnTermination) {
			this.deleteOnTermination = deleteOnTermination;
			return this;
		}

		public Builder volumeType(String volumeType) {
			this.volumeType = volumeType;
			return this;
		}

		public Builder iops(Integer iops) {
			this.iops = iops;
			return this;
		}

		public Builder encrypted(Boolean encrypted) {
			this.encrypted = encrypted;
			return this;
		}

		public BlockDeviceMapping build() {
			return new BlockDeviceMapping(deviceName, virtualName, snapshotId, sizeInGib, noDevice, deleteOnTermination,
					volumeType, iops, encrypted);
		}

		public Builder clear() {
			this.deviceName = null;
			this.virtualName = null;
			this.snapshotId = null;
			this.sizeInGib = null;
			this.noDevice = null;
			this.deleteOnTermination = null;
			this.volumeType = null;
			this.iops = null;
			this.encrypted = null;
			return this;
		}
	}

	private final String deviceName;
	private final String virtualName;
	private final String snapshotId;
	private final Integer sizeInGib;
	private final Boolean noDevice;
	private final Boolean deleteOnTermination;
	private final String volumeType;
	private final Integer iops;
	private final Boolean encrypted;

	// values expressed in GB
	private static final Integer VOLUME_SIZE_MIN_VALUE = 1;
	private static final Integer VOLUME_SIZE_MAX_VALUE = 1000;

	BlockDeviceMapping(String deviceName, @Nullable String virtualName, @Nullable String snapshotId,
			@Nullable Integer sizeInGib, @Nullable Boolean noDevice, @Nullable Boolean deleteOnTermination,
			@Nullable String volumeType, @Nullable Integer iops, @Nullable Boolean encrypted) {

		checkNotNull(deviceName, "deviceName cannot be null");
		checkNotNull(emptyToNull(deviceName), "deviceName must be defined");

		if (sizeInGib != null) {
			checkArgument(sizeInGib >= VOLUME_SIZE_MIN_VALUE && sizeInGib <= VOLUME_SIZE_MAX_VALUE,
					"Size in Gib must be between %s and %s GB", VOLUME_SIZE_MIN_VALUE, VOLUME_SIZE_MAX_VALUE);
		}
		this.deviceName = deviceName;
		this.virtualName = virtualName;
		this.snapshotId = snapshotId;
		this.sizeInGib = sizeInGib;
		this.noDevice = noDevice;
		this.deleteOnTermination = deleteOnTermination;
		this.volumeType = volumeType;
		this.iops = iops;
		this.encrypted = encrypted;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public String getVirtualName() {
		return virtualName;
	}

	public String getEbsSnapshotId() {
		return snapshotId;
	}

	public Integer getEbsVolumeSize() {
		return sizeInGib;
	}

	public Boolean getEbsNoDevice() {
		return noDevice;
	}

	public Boolean getEbsDeleteOnTermination() {
		return deleteOnTermination;
	}

	public String getEbsVolumeType() {
		return volumeType;
	}

	public Integer getEbsIops() {
		return iops;
	}

	public Boolean getEbsEncrypted() {
		return encrypted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deleteOnTermination == null) ? 0 : deleteOnTermination.hashCode());
		result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result + ((noDevice == null) ? 0 : noDevice.hashCode());
		result = prime * result + ((sizeInGib == null) ? 0 : sizeInGib.hashCode());
		result = prime * result + ((snapshotId == null) ? 0 : snapshotId.hashCode());
		result = prime * result + ((virtualName == null) ? 0 : virtualName.hashCode());
		result = prime * result + ((volumeType == null) ? 0 : volumeType.hashCode());
		result = prime * result + ((iops == null) ? 0 : iops.hashCode());
		result = prime * result + ((encrypted == null) ? 0 : encrypted.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockDeviceMapping other = (BlockDeviceMapping) obj;
		if (deleteOnTermination == null) {
			if (other.deleteOnTermination != null)
				return false;
		} else if (!deleteOnTermination.equals(other.deleteOnTermination))
			return false;
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (noDevice == null) {
			if (other.noDevice != null)
				return false;
		} else if (!noDevice.equals(other.noDevice))
			return false;
		if (sizeInGib == null) {
			if (other.sizeInGib != null)
				return false;
		} else if (!sizeInGib.equals(other.sizeInGib))
			return false;
		if (snapshotId == null) {
			if (other.snapshotId != null)
				return false;
		} else if (!snapshotId.equals(other.snapshotId))
			return false;
		if (virtualName == null) {
			if (other.virtualName != null)
				return false;
		} else if (!virtualName.equals(other.virtualName))
			return false;
		if (volumeType == null) {
			if (other.volumeType != null)
				return false;
		} else if (!volumeType.equals(other.volumeType))
			return false;
		if (iops == null) {
			if (other.iops != null)
				return false;
		} else if (!iops.equals(other.iops))
			return false;
		if (encrypted == null) {
			if (other.encrypted != null)
				return false;
		} else if (!encrypted.equals(other.encrypted))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[deviceName=" + deviceName + ", virtualName=" + virtualName + ", snapshotId=" + snapshotId
				+ ", sizeInGib=" + sizeInGib + ", noDevice=" + noDevice + ", deleteOnTermination=" + deleteOnTermination
				+ ", volumeType=" + volumeType + ", iops=" + iops + ", encrypted=" + encrypted + "]";
	}

	public static class MapEBSSnapshotToDevice extends BlockDeviceMapping {
		public MapEBSSnapshotToDevice(String deviceName, String snapshotId, @Nullable Integer sizeInGib,
				@Nullable Boolean deleteOnTermination, @Nullable String volumeType, @Nullable Integer iops,
				@Nullable Boolean encrypted) {
			super(deviceName, null, snapshotId, sizeInGib, null, deleteOnTermination, volumeType, iops, encrypted);
			checkNotNull(emptyToNull(snapshotId), "snapshotId must be defined");
		}
	}

	public static class MapNewVolumeToDevice extends BlockDeviceMapping {
		public MapNewVolumeToDevice(String deviceName, Integer sizeInGib, @Nullable Boolean deleteOnTermination,
				@Nullable String volumeType, @Nullable Integer iops, @Nullable Boolean encrypted) {
			super(deviceName, null, null, sizeInGib, null, deleteOnTermination, volumeType, iops, encrypted);
			checkNotNull(sizeInGib, "sizeInGib cannot be null");
		}
	}

	public static class MapEphemeralDeviceToDevice extends BlockDeviceMapping {
		public MapEphemeralDeviceToDevice(String deviceName, String virtualName) {
			super(deviceName, virtualName, null, null, null, null, null, null, null);
			checkNotNull(emptyToNull(virtualName), "virtualName must be defined");
		}
	}

	public static class UnmapDeviceNamed extends BlockDeviceMapping {
		public UnmapDeviceNamed(String deviceName) {
			super(deviceName, null, null, null, true, null, null, null, null);
		}
	}

	@Override
	public int compareTo(BlockDeviceMapping arg0) {
		return deviceName.compareTo(arg0.deviceName);
	}
}
