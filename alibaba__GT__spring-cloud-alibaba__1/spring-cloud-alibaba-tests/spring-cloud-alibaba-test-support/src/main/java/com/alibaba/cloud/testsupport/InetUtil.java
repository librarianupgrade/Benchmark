/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.testsupport;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author freeman
 * @since 2021.0.1.0
 */
public final class InetUtil {

	/**
	 * Get host ip.
	 *
	 * <p>
	 * It's very useful for the container test.
	 *
	 * @return host ip
	 */
	public static String getHostIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get host name.
	 *
	 * @return host name
	 */
	public static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

}
