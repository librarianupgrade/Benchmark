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

package com.alibaba.nacos.common.http.client.handler;

import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.response.HttpClientResponse;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.utils.JacksonUtils;

import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * bean response handler,
 * Mainly converter response type as bean type.
 *
 * @author mai.jh
 */
public class BeanResponseHandler<T> extends AbstractResponseHandler<T> {

	@Override
	public HttpRestResult<T> convertResult(HttpClientResponse response, Type responseType) throws Exception {
		final Header headers = response.getHeaders();
		InputStream body = response.getBody();
		T extractBody = JacksonUtils.toObj(body, responseType);
		return new HttpRestResult<>(headers, response.getStatusCode(), extractBody, null);
	}
}
