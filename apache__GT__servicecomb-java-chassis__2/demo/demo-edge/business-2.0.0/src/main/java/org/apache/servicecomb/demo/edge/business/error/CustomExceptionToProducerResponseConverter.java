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

package org.apache.servicecomb.demo.edge.business.error;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionConverter;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

public class CustomExceptionToProducerResponseConverter implements ExceptionConverter<IllegalStateException> {
	@Override
	public boolean canConvert(Throwable throwable) {
		return throwable instanceof IllegalStateException;
	}

	@Override
	public int getOrder() {
		return 100;
	}

	@Override
	public InvocationException convert(@Nullable Invocation invocation, IllegalStateException e,
			StatusType genericStatus) {
		IllegalStateErrorData data = new IllegalStateErrorData();
		data.setId(500);
		data.setMessage(e.getMessage());
		data.setState(e.getMessage());
		return new InvocationException(Status.INTERNAL_SERVER_ERROR, data);
	}
}
