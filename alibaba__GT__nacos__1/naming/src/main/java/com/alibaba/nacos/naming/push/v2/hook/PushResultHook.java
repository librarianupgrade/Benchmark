/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.naming.push.v2.hook;

/**
 * Nacos naming push result hook.
 *
 * @author xiweng.yy
 */
public interface PushResultHook {

	/**
	 * Push success.
	 *
	 * @param result push result
	 */
	void pushSuccess(PushResult result);

	/**
	 * Push failed.
	 *
	 * @param result push result
	 */
	void pushFailed(PushResult result);
}
