/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.consistency;

import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.common.model.RestResultUtils;

import java.util.Map;

/**
 * Operation and maintenance command interface.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface CommandOperations {

	/**
	 * Operation and maintenance interface operation entry.
	 *
	 * @param commands commands
	 * @return execute success
	 */
	default RestResult<String> execute(Map<String, String> commands) {
		return RestResultUtils.success();
	}

}
