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

package org.apache.servicecomb.tracing.zipkin;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import org.apache.servicecomb.tracing.Span;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brave.Tracing;

@Aspect
class ZipkinSpanAspect {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ZipkinTracingAdviser adviser;

	ZipkinSpanAspect(Tracing tracing) {
		this.adviser = new ZipkinTracingAdviser(tracing.tracer());
	}

	@Around("execution(@org.apache.servicecomb.tracing.Span * *(..)) && @annotation(spanAnnotation)")
	public Object advise(ProceedingJoinPoint joinPoint, Span spanAnnotation) throws Throwable {
		String spanName = spanAnnotation.spanName();
		String callPath = spanAnnotation.callPath();
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		LOG.debug("Generating zipkin span for method {}", method.toString());
		if ("".equals(spanName)) {
			spanName = method.getName();
		}
		if ("".equals(callPath)) {
			callPath = method.toString();
		}

		return adviser.invoke(spanName, callPath, joinPoint::proceed);
	}
}
