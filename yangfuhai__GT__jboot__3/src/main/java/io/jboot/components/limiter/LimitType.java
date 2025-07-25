/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.components.limiter;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 限制类型
 */
public class LimitType {

	/**
	 * 令牌桶，通过 guava 的 RateLimiter 来实现
	 * 时间有关，每秒钟允许有多少个请求
	 */
	public static final String TOKEN_BUCKET = "tb";

	/**
	 * IP 并发量限制，通过 RateLimiter 来实现
	 * 每个 ip 在每 1 秒内允许请求的数量
	 */
	public static final String IP_TOKEN_BUCKET = "iptb";

	/**
	 * 并发量，通过 Semaphore 来实现
	 * 和并发有关，和请求时间无关
	 */
	public static final String CONCURRENCY = "cc";

	/**
	 * IP 并发量限制，通过 Semaphore 来实现
	 * 和并发有关，和请求时间无关
	 */
	public static final String IP_CONCURRENCY = "ipcc";

	public static Set<String> types = Sets.newHashSet(TOKEN_BUCKET, IP_TOKEN_BUCKET, CONCURRENCY, IP_CONCURRENCY);

}
