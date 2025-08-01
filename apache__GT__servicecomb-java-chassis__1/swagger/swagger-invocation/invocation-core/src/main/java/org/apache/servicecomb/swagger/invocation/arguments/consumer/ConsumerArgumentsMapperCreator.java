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

package org.apache.servicecomb.swagger.invocation.arguments.consumer;

import static org.apache.servicecomb.swagger.generator.SwaggerGeneratorUtils.collectParameterName;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.LambdaMetafactoryUtils;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.apache.servicecomb.swagger.generator.core.model.SwaggerOperation;
import org.apache.servicecomb.swagger.invocation.arguments.AbstractArgumentsMapperCreator;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapper;
import org.apache.servicecomb.swagger.invocation.arguments.ContextArgumentMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

public class ConsumerArgumentsMapperCreator extends AbstractArgumentsMapperCreator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerArgumentsMapperCreator.class);

	private int unknownConsumerParams = 0;

	public ConsumerArgumentsMapperCreator(SerializationConfig serializationConfig,
			Map<Class<?>, ContextArgumentMapperFactory> contextFactories, Class<?> consumerClass, Method consumerMethod,
			SwaggerOperation swaggerOperation) {
		super(serializationConfig, contextFactories, consumerClass, consumerMethod, swaggerOperation);
	}

	private boolean isAllSameMapper() {
		for (ArgumentMapper mapper : mappers) {
			if (mapper instanceof ConsumerArgumentSame && ((ConsumerArgumentSame) mapper).isSameMapping()) {
				continue;
			}

			return false;
		}

		return true;
	}

	public ArgumentsMapper createArgumentsMapper() {
		doCreateArgumentsMapper();

		// if all mappers are SameMapper, then no need any mapper
		if (unknownConsumerParams == 0 && mappers.size() == swaggerOperation.parameterCount() && isAllSameMapper()) {
			return new ArgumentsMapperDirectReuse();
		}

		return new ArgumentsMapperCommon(mappers);
	}

	@Override
	protected void processUnknownParameter(int providerParamIdx, java.lang.reflect.Parameter providerParameter,
			String parameterName) {
		LOGGER.warn("Consumer parameter({}) is not exist in contract, method={}:{}.", parameterName,
				providerMethod.getDeclaringClass().getName(), providerMethod.getName());
		unknownConsumerParams++;
	}

	@Override
	protected void processPendingSwaggerParameter(Parameter parameter) {

	}

	@Override
	protected void processPendingBodyParameter(RequestBody parameter) {

	}

	@Override
	protected ArgumentMapper createKnownParameterMapper(int providerParamIdx, String invocationArgumentName) {
		return new ConsumerArgumentSame(this.providerMethod.getParameters()[providerParamIdx].getName(),
				invocationArgumentName);
	}

	@Override
	protected ArgumentMapper createSwaggerBodyFieldMapper(int consumerParamIdx, String parameterName) {
		return new ConsumerArgumentToBodyField(this.providerMethod.getParameters()[consumerParamIdx].getName(),
				(String) this.bodyParameter.getExtensions().get(SwaggerConst.EXT_BODY_NAME), parameterName);
	}

	@Override
	protected boolean processBeanParameter(int consumerParamIdx, java.lang.reflect.Parameter consumerParameter) {
		JavaType consumerType = TypeFactory.defaultInstance().constructType(consumerParameter.getParameterizedType());
		if (!SwaggerUtils.isBean(consumerType)) {
			return false;
		}
		boolean result = false;
		ConsumerBeanParamMapper mapper = new ConsumerBeanParamMapper(
				this.providerMethod.getParameters()[consumerParamIdx].getName());
		for (BeanPropertyDefinition propertyDefinition : serializationConfig.introspect(consumerType)
				.findProperties()) {
			String parameterName = collectParameterName(providerMethod, propertyDefinition);
			if (!parameterNameExistsInSwagger(parameterName)) {
				continue;
			}

			mapper.addField(parameterName, LambdaMetafactoryUtils.createObjectGetter(propertyDefinition));
			processedSwaggerParameters.add(parameterName);
			result = true;
		}
		mappers.add(mapper);
		return result;
	}
}
