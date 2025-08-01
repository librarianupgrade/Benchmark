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

package org.apache.servicecomb.core.exception;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.TOO_MANY_REQUESTS;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.apache.servicecomb.core.exception.ExceptionCodes.GENERIC_SERVER;
import static org.apache.servicecomb.swagger.invocation.InvocationType.CONSUMER;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.servicecomb.config.inject.InjectProperties;
import org.apache.servicecomb.config.inject.InjectProperty;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.tracing.TraceIdLogger;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.ExceptionUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.ws.rs.core.Response.StatusType;

@InjectProperties(prefix = "servicecomb.invocation.exception")
public class DefaultExceptionProcessor implements ExceptionProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionProcessor.class);

	public static final int ORDER = Integer.MAX_VALUE;

	@JsonIgnore
	@SuppressWarnings("unchecked")
	private final List<ExceptionConverter<Throwable>> converters = SPIServiceUtils
			.getOrLoadSortedService(ExceptionConverter.class).stream()
			.map(converter -> (ExceptionConverter<Throwable>) converter).collect(Collectors.toList());

	@InjectProperty(keys = "print-stack-trace", defaultValue = "true")
	protected boolean printStackTrace;

	@InjectProperty(keys = "print-rate-limit", defaultValue = "false")
	protected boolean printRateLimit;

	private final Map<Class<?>, ExceptionConverter<Throwable>> converterCache = new ConcurrentHashMapEx<>();

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	public boolean isPrintStackTrace() {
		return printStackTrace;
	}

	public DefaultExceptionProcessor setPrintStackTrace(boolean printStackTrace) {
		this.printStackTrace = printStackTrace;
		return this;
	}

	public boolean isPrintRateLimit() {
		return printRateLimit;
	}

	public DefaultExceptionProcessor setPrintRateLimit(boolean printRateLimit) {
		this.printRateLimit = printRateLimit;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Autowired(required = false)
	public DefaultExceptionProcessor setConverters(List<ExceptionConverter<? extends Throwable>> converters) {
		converters.forEach(converter -> this.converters.add((ExceptionConverter<Throwable>) converter));
		this.converters.sort(Comparator.comparingInt(ExceptionConverter::getOrder));
		this.converterCache.clear();
		return this;
	}

	@Override
	public InvocationException convert(Invocation invocation, Throwable throwable) {
		StatusType genericStatus = CONSUMER.equals(invocation.getInvocationType()) ? BAD_REQUEST
				: INTERNAL_SERVER_ERROR;
		return convert(invocation, throwable, genericStatus);
	}

	@Override
	public InvocationException convert(Invocation invocation, Throwable throwable, StatusType genericStatus) {
		Throwable unwrapped = ExceptionFactory.unwrap(throwable);
		try {
			ExceptionConverter<Throwable> converter = converterCache.computeIfAbsent(unwrapped.getClass(),
					clazz -> findConverter(unwrapped));
			if (invocation == null) {
				LOGGER.warn("Convert unknown operation exception {}/{} using {}.", throwable.getClass().getSimpleName(),
						unwrapped.getClass().getSimpleName(), converter.getClass().getSimpleName());
			} else {
				invocation.getTraceIdLogger().warn("{} Convert operation {} exception {}/{} using {}.",
						TraceIdLogger.constructSource(DefaultExceptionProcessor.class.getSimpleName()),
						invocation.getMicroserviceQualifiedName(), throwable.getClass().getSimpleName(),
						unwrapped.getClass().getSimpleName(), converter.getClass().getSimpleName());
			}
			return converter.convert(invocation, unwrapped, genericStatus);
		} catch (Exception e) {
			LOGGER.error(
					"BUG: ExceptionConverter.convert MUST not throw exception, please fix it.\n"
							+ "original exception :{}" + "converter exception:{}",
					getStackTrace(throwable), getStackTrace(e));
			return new InvocationException(INTERNAL_SERVER_ERROR,
					new CommonExceptionData(GENERIC_SERVER, INTERNAL_SERVER_ERROR.getReasonPhrase()));
		}
	}

	private ExceptionConverter<Throwable> findConverter(Throwable throwable) {
		for (ExceptionConverter<Throwable> converter : converters) {
			if (converter.canConvert(throwable)) {
				return converter;
			}
		}

		throw new IllegalStateException("never happened: can not find converter for " + throwable.getClass().getName());
	}

	@Override
	public Response toConsumerResponse(Invocation invocation, Throwable throwable) {
		InvocationException exception = convert(invocation, throwable, BAD_REQUEST);
		logConsumerException(invocation, exception);
		return Response.failResp(exception);
	}

	@Override
	public void logConsumerException(Invocation invocation, InvocationException exception) {
		if (isIgnoreLog(invocation, exception)) {
			return;
		}

		if (isPrintStackTrace()) {
			invocation.getTraceIdLogger().error("{} Failed to invoke {}, endpoint={}.",
					TraceIdLogger.constructSource(DefaultExceptionProcessor.class.getSimpleName()),
					invocation.getMicroserviceQualifiedName(), invocation.getEndpoint(), exception);
			return;
		}

		invocation.getTraceIdLogger().error("{} Failed to invoke {}, endpoint={}, message={}.",
				TraceIdLogger.constructSource(DefaultExceptionProcessor.class.getSimpleName()),
				invocation.getMicroserviceQualifiedName(), invocation.getEndpoint(),
				ExceptionUtils.getExceptionMessageWithoutTrace(exception));
	}

	@Override
	public boolean isIgnoreLog(Invocation invocation, InvocationException exception) {
		if (!isPrintRateLimit() && exception.getStatusCode() == TOO_MANY_REQUESTS.getStatusCode()) {
			return true;
		}

		return false;
	}

	@Override
	public Response toProducerResponse(Invocation invocation, Throwable exception) {
		InvocationException invocationException = convert(invocation, exception, INTERNAL_SERVER_ERROR);
		if (invocation != null) {
			logProducerException(invocation, invocationException);
		}
		return Response.createFail(invocationException);
	}

	@Override
	public void logProducerException(Invocation invocation, InvocationException exception) {
		if (isIgnoreLog(invocation, exception)) {
			return;
		}

		if (isPrintStackTrace()) {
			invocation.getTraceIdLogger().error("{} Failed to process {} invocation, operation={}.",
					TraceIdLogger.constructSource(DefaultExceptionProcessor.class.getSimpleName()),
					invocation.getInvocationType(), invocation.getMicroserviceQualifiedName(), exception);
			return;
		}

		invocation.getTraceIdLogger().error("{} Failed to process {} invocation, operation={}, message={}.",
				TraceIdLogger.constructSource(DefaultExceptionProcessor.class.getSimpleName()),
				invocation.getInvocationType(), invocation.getMicroserviceQualifiedName(),
				ExceptionUtils.getExceptionMessageWithoutTrace(exception));
	}
}
