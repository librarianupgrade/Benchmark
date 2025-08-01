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
package org.jclouds.dynect.v3.domain.rdata;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.ConstructorProperties;
import java.util.Map;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;

/**
 * Corresponds to the binary representation of the {@code SOA} (Start of Authority) RData
 * 
 * <h4>Example</h4>
 * 
 * <pre>
 * SOAData rdata = SOAData.builder()
 *                        .rname(&quot;foo.com.&quot;)
 *                        .mname(&quot;admin.foo.com.&quot;)
 *                        .serial(1)
 *                        .refresh(3600)
 *                        .retry(600)
 *                        .expire(604800)
 *                        .minimum(60).build()
 * </pre>
 * 
 * @see <a href="http://www.ietf.org/rfc/rfc1035.txt">RFC 1035</a>
 */
public class SOAData extends ForwardingMap<String, Object> {
	private final String mname;
	private final String rname;
	private final int serial;
	private final int refresh;
	private final int retry;
	private final int expire;
	private final int minimum;

	@ConstructorProperties({ "mname", "rname", "serial", "refresh", "retry", "expire", "minimum" })
	private SOAData(String mname, String rname, int serial, int refresh, int retry, int expire, int minimum) {
		this.mname = checkNotNull(mname, "mname");
		this.rname = checkNotNull(rname, "rname of %s", mname);
		checkArgument(serial >= 0, "serial of %s must be unsigned", mname);
		this.serial = serial;
		checkArgument(refresh >= 0, "refresh of %s must be unsigned", mname);
		this.refresh = refresh;
		checkArgument(retry >= 0, "retry of %s must be unsigned", mname);
		this.retry = retry;
		checkArgument(expire >= 0, "expire of %s must be unsigned", mname);
		this.expire = expire;
		checkArgument(minimum >= 0, "minimum of %s must be unsigned", mname);
		this.minimum = minimum;
		this.delegate = ImmutableMap.<String, Object>builder().put("mname", checkNotNull(mname, "mname"))
				.put("rname", checkNotNull(rname, "rname of %s", mname)).put("serial", serial).put("refresh", refresh)
				.put("retry", retry).put("expire", expire).put("minimum", minimum).build();
	}

	/**
	* domain-name of the name server that was the original or primary source of
	* data for this zone
	*/
	public String getMname() {
		return mname;
	}

	/**
	* domain-name which specifies the mailbox of the person responsible for this
	* zone.
	*/
	public String getRname() {
		return rname;
	}

	/**
	* version number of the original copy of the zone.
	*/
	public int getSerial() {
		return serial;
	}

	/**
	* time interval before the zone should be refreshed
	*/
	public int getRefresh() {
		return refresh;
	}

	/**
	* time interval that should elapse before a failed refresh should be retried
	*/
	public int getRetry() {
		return retry;
	}

	/**
	* time value that specifies the upper limit on the time interval that can
	* elapse before the zone is no longer authoritative.
	*/
	public int getExpire() {
		return expire;
	}

	/**
	* minimum TTL field that should be exported with any RR from this zone.
	*/
	public int getMinimum() {
		return minimum;
	}

	private final transient ImmutableMap<String, Object> delegate;

	protected Map<String, Object> delegate() {
		return delegate;
	}

	public static SOAData.Builder builder() {
		return new Builder();
	}

	public SOAData.Builder toBuilder() {
		return builder().from(this);
	}

	public static final class Builder {
		private String mname;
		private String rname;
		private int serial = -1;
		private int refresh = -1;
		private int retry = -1;
		private int expire = -1;
		private int minimum = -1;

		/**
		 * @see SOAData#getMname()
		 */
		public SOAData.Builder mname(String mname) {
			this.mname = mname;
			return this;
		}

		/**
		 * @see SOAData#getRname()
		 */
		public SOAData.Builder rname(String rname) {
			this.rname = rname;
			return this;
		}

		/**
		 * @see SOAData#getSerial()
		 */
		public SOAData.Builder serial(int serial) {
			this.serial = serial;
			return this;
		}

		/**
		 * @see SOAData#getRefresh()
		 */
		public SOAData.Builder refresh(int refresh) {
			this.refresh = refresh;
			return this;
		}

		/**
		 * @see SOAData#getRetry()
		 */
		public SOAData.Builder retry(int retry) {
			this.retry = retry;
			return this;
		}

		/**
		 * @see SOAData#getExpire()
		 */
		public SOAData.Builder expire(int expire) {
			this.expire = expire;
			return this;
		}

		/**
		 * @see SOAData#getMinimum()
		 */
		public SOAData.Builder minimum(int minimum) {
			this.minimum = minimum;
			return this;
		}

		public SOAData build() {
			return new SOAData(mname, rname, serial, refresh, retry, expire, minimum);
		}

		public SOAData.Builder from(SOAData in) {
			return this.mname(in.getMname()).rname(in.getRname()).serial(in.getSerial()).refresh(in.getRefresh())
					.expire(in.getExpire()).minimum(in.getMinimum());
		}
	}
}
