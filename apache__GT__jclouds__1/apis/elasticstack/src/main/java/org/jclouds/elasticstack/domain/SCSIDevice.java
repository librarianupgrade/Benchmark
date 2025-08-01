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
package org.jclouds.elasticstack.domain;

import static com.google.common.base.Preconditions.checkArgument;

public class SCSIDevice extends Device {
	public static class Builder extends Device.Builder {
		private final int unit;

		public Builder(int unit) {
			this.unit = unit;
		}

		@Override
		public Device build() {
			return new SCSIDevice(uuid, mediaType, unit);
		}

	}

	private static final int bus = 0;
	private final int unit;

	public SCSIDevice(String driveUuid, MediaType mediaType, int unit) {
		super(driveUuid, mediaType);
		checkArgument(unit >= 0 && unit < 8, "unit must be between 0 and 7");
		this.unit = unit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bus;
		result = prime * result + unit;
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
		SCSIDevice other = (SCSIDevice) obj;
		if (unit != other.unit)
			return false;
		return true;
	}

	public int getBus() {
		return bus;
	}

	public int getUnit() {
		return unit;
	}

	@Override
	public String getId() {
		return String.format("scsi:%d:%d", bus, unit);
	}

	@Override
	public String toString() {
		return "[id=" + getId() + ", driveUuid=" + driveUuid + ", mediaType=" + mediaType + "]";
	}
}
