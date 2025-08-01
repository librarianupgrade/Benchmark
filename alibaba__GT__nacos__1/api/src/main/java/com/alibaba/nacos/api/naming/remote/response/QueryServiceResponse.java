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

package com.alibaba.nacos.api.naming.remote.response;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ResponseCode;

/**
 * Nacos naming query request.
 *
 * @author xiweng.yy
 */
public class QueryServiceResponse extends Response {

	private ServiceInfo serviceInfo;

	public QueryServiceResponse() {
	}

	private QueryServiceResponse(ServiceInfo serviceInfo) {
		this.serviceInfo = serviceInfo;
	}

	/**
	 * Build Success response.
	 *
	 * @param serviceInfo service info
	 * @return service query response
	 */
	public static QueryServiceResponse buildSuccessResponse(ServiceInfo serviceInfo) {
		return new QueryServiceResponse(serviceInfo);
	}

	/**
	 * Build fail response.
	 *
	 * @param message message
	 * @return service query response
	 */
	public static QueryServiceResponse buildFailResponse(String message) {
		QueryServiceResponse queryServiceResponse = new QueryServiceResponse();
		queryServiceResponse.setResultCode(ResponseCode.FAIL.getCode());
		queryServiceResponse.setMessage(message);
		return queryServiceResponse;
	}

	public ServiceInfo getServiceInfo() {
		return serviceInfo;
	}

	public void setServiceInfo(ServiceInfo serviceInfo) {
		this.serviceInfo = serviceInfo;
	}
}
