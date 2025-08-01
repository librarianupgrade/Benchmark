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
package org.apache.servicecomb.swagger.generator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.generator.core.processor.response.DefaultResponseTypeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.core.util.Json;

@SuppressWarnings("rawtypes")
public final class SwaggerGeneratorUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerGeneratorUtils.class);

	// all static fields load from SPI and stateless
	private static final Set<JavaType> contextTypes = SPIServiceUtils
			.getOrLoadSortedService(SwaggerContextRegister.class).stream().map(swaggerContextRegister -> TypeFactory
					.defaultInstance().constructType(swaggerContextRegister.getContextType()))
			.collect(Collectors.toSet());

	private static final Map<Type, ClassAnnotationProcessor<?>> classAnnotationProcessors = new HashMap<>();

	private static final Map<Type, MethodAnnotationProcessor<?>> methodAnnotationProcessors = new HashMap<>();

	private static final Map<Type, ResponseTypeProcessor> responseTypeProcessors = new HashMap<>();

	private static final DefaultResponseTypeProcessor defaultResponseTypeProcessor = new DefaultResponseTypeProcessor();

	private static final List<ParameterAnnotationProcessor> parameterAnnotationProcessors = SPIServiceUtils
			.getOrLoadSortedService(ParameterAnnotationProcessor.class);

	private static final List<ParameterTypeProcessor> parameterTypeProcessors = SPIServiceUtils
			.getOrLoadSortedService(ParameterTypeProcessor.class);

	static {
		// low order value has high priority
		for (ClassAnnotationProcessor<?> processor : SPIServiceUtils
				.getOrLoadSortedService(ClassAnnotationProcessor.class)) {
			if (classAnnotationProcessors.putIfAbsent(processor.getProcessType(), processor) != null) {
				LOGGER.info("ignore duplicated ClassAnnotationProcessor, type={}, processor={}.",
						processor.getProcessType().getTypeName(), processor.getClass().getName());
			}
		}

		for (MethodAnnotationProcessor<?> processor : SPIServiceUtils
				.getOrLoadSortedService(MethodAnnotationProcessor.class)) {
			if (methodAnnotationProcessors.putIfAbsent(processor.getProcessType(), processor) != null) {
				LOGGER.info("ignore duplicated MethodAnnotationProcessor, type={}, processor={}.",
						processor.getProcessType().getTypeName(), processor.getClass().getName());
			}
		}

		for (ResponseTypeProcessor processor : SPIServiceUtils.getOrLoadSortedService(ResponseTypeProcessor.class)) {
			if (responseTypeProcessors.putIfAbsent(processor.getProcessType(), processor) != null) {
				LOGGER.info("ignore duplicated ResponseTypeProcessor, type={}, processor={}.",
						processor.getProcessType().getTypeName(), processor.getClass().getName());
			}
		}

		List<Module> modules = SPIServiceUtils.getOrLoadSortedService(Module.class);
		Json.mapper().registerModules(modules.toArray(new Module[0]));
	}

	private SwaggerGeneratorUtils() {
	}

	@SuppressWarnings("unchecked")
	public static <ANNOTATION> ClassAnnotationProcessor<ANNOTATION> findClassAnnotationProcessor(Type type) {
		return (ClassAnnotationProcessor<ANNOTATION>) classAnnotationProcessors.get(type);
	}

	@SuppressWarnings("unchecked")
	public static <ANNOTATION> MethodAnnotationProcessor<ANNOTATION> findMethodAnnotationProcessor(Type type) {
		return (MethodAnnotationProcessor<ANNOTATION>) methodAnnotationProcessors.get(type);
	}

	@SuppressWarnings("unchecked")
	public static ParameterAnnotationProcessor<Annotation> findParameterAnnotationProcessor(Type type) {
		for (ParameterAnnotationProcessor<Annotation> processor : parameterAnnotationProcessors) {
			if (processor.getProcessType() == type) {
				return processor;
			}
		}
		return null;
	}

	public static ParameterTypeProcessor findParameterTypeProcessor(Type type) {
		for (ParameterTypeProcessor processor : parameterTypeProcessors) {
			if (processor.getProcessType() == type) {
				return processor;
			}
		}
		return null;
	}

	public static ResponseTypeProcessor findResponseTypeProcessor(Type type) {
		ResponseTypeProcessor processor = responseTypeProcessors.get(type);
		if (processor != null) {
			return processor;
		}

		if (type instanceof ParameterizedType) {
			return responseTypeProcessors.getOrDefault(((ParameterizedType) type).getRawType(),
					defaultResponseTypeProcessor);
		}

		return defaultResponseTypeProcessor;
	}

	public static boolean isContextParameter(JavaType type) {
		return contextTypes.contains(type);
	}

	public static Annotation[] collectAnnotations(BeanPropertyDefinition propertyDefinition) {
		List<Annotation> annotations = new ArrayList<>();
		if (propertyDefinition.getField() != null) {
			Collections.addAll(annotations, propertyDefinition.getField().getAnnotated().getAnnotations());
		}
		if (propertyDefinition.getGetter() != null) {
			Collections.addAll(annotations, propertyDefinition.getGetter().getAnnotated().getAnnotations());
		}
		if (propertyDefinition.getSetter() != null) {
			Collections.addAll(annotations, propertyDefinition.getSetter().getAnnotated().getAnnotations());
		}
		return annotations.toArray(new Annotation[0]);
	}

	public static String collectParameterName(java.lang.reflect.Parameter methodParameter) {
		return collectParameterName(methodParameter.getDeclaringExecutable(), methodParameter.getAnnotations(),
				methodParameter.isNamePresent() ? methodParameter.getName() : null);
	}

	public static String collectParameterName(Method method, BeanPropertyDefinition propertyDefinition) {
		Annotation[] annotations = collectAnnotations(propertyDefinition);
		return collectParameterName(method, annotations, propertyDefinition.getName());
	}

	public static String collectParameterName(Executable executable, Annotation[] annotations, String defaultName) {
		// 1.annotations
		//   it's ambiguous to use different name in different annotation
		//   so we only read the first available name
		for (Annotation annotation : annotations) {
			ParameterAnnotationProcessor<Annotation> processor = findParameterAnnotationProcessor(
					annotation.annotationType());
			if (processor == null) {
				continue;
			}

			String name = processor.getParameterName(annotation);
			if (StringUtils.isNotEmpty(name)) {
				return name;
			}
		}

		// 2.use signature name
		// ensure present parameter name
		if (StringUtils.isNotEmpty(defaultName)) {
			return defaultName;
		}

		String msg = String.format("""
				parameter name is not present, method=%s:%s
				solution:
				  change pom.xml, add compiler argument: -parameters, for example:
				    <plugin>
				      <groupId>org.apache.maven.plugins</groupId>
				      <artifactId>maven-compiler-plugin</artifactId>
				      <configuration>
				        <compilerArgument>-parameters</compilerArgument>
				      </configuration>
				    </plugin>""", executable.getDeclaringClass().getName(), executable.getName());
		throw new IllegalStateException(msg);
	}

	public static List<Annotation> collectParameterAnnotations(Annotation[] parameterAnnotations,
			Map<String, List<Annotation>> methodAnnotationMap, String parameterName) {
		List<Annotation> methodAnnotations = methodAnnotationMap.remove(parameterName);
		if (methodAnnotations == null) {
			methodAnnotations = Collections.emptyList();
		}

		List<Annotation> annotations = new ArrayList<>();
		Collections.addAll(annotations, parameterAnnotations);
		annotations.addAll(methodAnnotations);

		return annotations;
	}
}
