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
package org.apache.servicecomb.provider.pojo;

import java.lang.reflect.Method;

import org.apache.servicecomb.provider.pojo.definition.PojoConsumerMeta;
import org.apache.servicecomb.provider.pojo.definition.PojoConsumerOperationMeta;

public class PojoInvocationCreator {
	public PojoInvocation create(Method method, PojoConsumerMetaRefresher metaRefresher, Object[] args) {
		PojoConsumerMeta pojoConsumerMeta = metaRefresher.getLatestMeta();
		PojoConsumerOperationMeta consumerOperationMeta = pojoConsumerMeta.ensureFindOperationMeta(method);

		PojoInvocation invocation = new PojoInvocation(consumerOperationMeta);
		invocation.setSuccessResponseType(consumerOperationMeta.getResponsesType());
		invocation.setInvocationArguments(
				consumerOperationMeta.getSwaggerConsumerOperation().toInvocationArguments(args));
		invocation.setSync(consumerOperationMeta.isSync());

		return invocation;
	}
}
