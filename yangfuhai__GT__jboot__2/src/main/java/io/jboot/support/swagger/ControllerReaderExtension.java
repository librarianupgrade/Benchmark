/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.support.swagger;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.jfinal.log.Log;
import io.swagger.annotations.*;
import io.swagger.converter.ModelConverters;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.util.BaseReaderUtils;
import io.swagger.util.ParameterProcessor;
import io.swagger.util.PathUtils;
import io.swagger.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class ControllerReaderExtension {

	private static final Log LOGGER = Log.getLog(ControllerReaderExtension.class);
	private static final String SUCCESSFUL_OPERATION = "successful operation";

	private static <T> List<T> parseAnnotationValues(String str, Function<String, T> processor) {
		final List<T> result = new ArrayList<T>();
		for (String item : Splitter.on(",").trimResults().omitEmptyStrings().split(str)) {
			result.add(processor.apply(item));
		}
		return result;
	}

	private static List<String> parseStringValues(String str) {
		return parseAnnotationValues(str, new Function<String, String>() {

			@Override
			public String apply(String value) {
				return value;
			}
		});
	}

	private static List<Scheme> parseSchemes(String schemes) {
		final List<Scheme> result = new ArrayList<Scheme>();
		for (String item : StringUtils.trimToEmpty(schemes).split(",")) {
			final Scheme scheme = Scheme.forValue(StringUtils.trimToNull(item));
			if (scheme != null && !result.contains(scheme)) {
				result.add(scheme);
			}
		}
		return result;
	}

	private static List<SecurityRequirement> parseAuthorizations(Authorization[] authorizations) {
		final List<SecurityRequirement> result = new ArrayList<SecurityRequirement>();
		for (Authorization auth : authorizations) {
			if (StringUtils.isNotEmpty(auth.value())) {
				final SecurityRequirement security = new SecurityRequirement();
				security.setName(auth.value());
				for (AuthorizationScope scope : auth.scopes()) {
					if (StringUtils.isNotEmpty(scope.scope())) {
						security.addScope(scope.scope());
					}
				}
				result.add(security);
			}
		}
		return result;
	}

	private static Map<String, Property> parseResponseHeaders(Swagger swagger, ReaderContext context,
			ResponseHeader[] headers) {
		Map<String, Property> responseHeaders = null;
		for (ResponseHeader header : headers) {
			final String name = header.name();
			if (StringUtils.isNotEmpty(name)) {
				if (responseHeaders == null) {
					responseHeaders = new HashMap<String, Property>();
				}
				final Class<?> cls = header.response();
				if (!ReflectionUtils.isVoid(cls)) {
					final Property property = ModelConverters.getInstance().readAsProperty(cls);
					if (property != null) {
						final Property responseProperty = ContainerWrapper.wrapContainer(header.responseContainer(),
								property, ContainerWrapper.ARRAY, ContainerWrapper.LIST, ContainerWrapper.SET);
						responseProperty.setDescription(header.description());
						responseHeaders.put(name, responseProperty);
						appendModels(swagger, cls);
					}
				}
			}
		}
		return responseHeaders;
	}

	private static void appendModels(Swagger swagger, Type type) {
		final Map<String, Model> models = ModelConverters.getInstance().readAll(type);
		for (Map.Entry<String, Model> entry : models.entrySet()) {
			swagger.model(entry.getKey(), entry.getValue());
		}
	}

	private static boolean isValidResponse(Type type) {
		final JavaType javaType = TypeFactory.defaultInstance().constructType(type);
		return !ReflectionUtils.isVoid(javaType);
	}

	private static Type getResponseType(Method method) {
		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
		if (apiOperation != null && !ReflectionUtils.isVoid(apiOperation.response())) {
			return apiOperation.response();
		} else {
			return method.getGenericReturnType();
		}
	}

	private static String getResponseContainer(ApiOperation apiOperation) {
		return apiOperation == null ? null : StringUtils.defaultIfBlank(apiOperation.responseContainer(), null);
	}

	public int getPriority() {
		return 0;
	}

	public boolean isReadable(ReaderContext context) {
		final Api apiAnnotation = context.getCls().getAnnotation(Api.class);
		return apiAnnotation != null && (context.isReadHidden() || !apiAnnotation.hidden());
	}

	public void applyConsumes(ReaderContext context, Operation operation, Method method) {
		final List<String> consumes = new ArrayList<String>();
		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);

		if (apiOperation != null) {
			consumes.addAll(parseStringValues(apiOperation.consumes()));
		}

		if (consumes.isEmpty()) {
			final Api apiAnnotation = context.getCls().getAnnotation(Api.class);
			if (apiAnnotation != null) {
				consumes.addAll(parseStringValues(apiAnnotation.consumes()));
			}
			consumes.addAll(context.getParentConsumes());
		}

		for (String consume : consumes) {
			operation.consumes(consume);
		}
	}

	public void applyProduces(ReaderContext context, Operation operation, Method method) {
		final List<String> produces = new ArrayList<String>();
		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);

		if (apiOperation != null) {
			produces.addAll(parseStringValues(apiOperation.produces()));
		}

		if (produces.isEmpty()) {
			final Api apiAnnotation = context.getCls().getAnnotation(Api.class);
			if (apiAnnotation != null) {
				produces.addAll(parseStringValues(apiAnnotation.produces()));
			}
			produces.addAll(context.getParentProduces());
		}

		for (String produce : produces) {
			operation.produces(produce);
		}
	}

	public String getHttpMethod(ReaderContext context, Method method) {
		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
		return apiOperation == null || StringUtils.isEmpty(apiOperation.httpMethod()) ? context.getParentHttpMethod()
				: apiOperation.httpMethod();
	}

	public String getPath(ReaderContext context, Method method) {
		final Api apiAnnotation = context.getCls().getAnnotation(Api.class);
		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
		final String operationPath = apiOperation == null ? null : apiOperation.nickname();
		return PathUtils.collectPath(context.getParentPath(), apiAnnotation == null ? null : apiAnnotation.value(),
				StringUtils.isBlank(operationPath) ? method.getName() : operationPath);
	}

	public void applyOperationId(Operation operation, Method method) {
		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
		if (apiOperation != null && StringUtils.isNotBlank(apiOperation.nickname())) {
			operation.operationId(apiOperation.nickname());
		} else {
			operation.operationId(method.getName());
		}
	}

	public void applySummary(Operation operation, Method method) {
		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
		if (apiOperation != null && StringUtils.isNotBlank(apiOperation.value())) {
			operation.summary(apiOperation.value());
		}
	}

	public void applyDescription(Operation operation, Method method) {
		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
		if (apiOperation != null && StringUtils.isNotBlank(apiOperation.notes())) {
			operation.description(apiOperation.notes());
		}
	}

	public void applySchemes(ReaderContext context, Operation operation, Method method) {
		final List<Scheme> schemes = new ArrayList<Scheme>();
		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
		final Api apiAnnotation = context.getCls().getAnnotation(Api.class);

		if (apiOperation != null) {
			schemes.addAll(parseSchemes(apiOperation.protocols()));
		}

		if (schemes.isEmpty() && apiAnnotation != null) {
			schemes.addAll(parseSchemes(apiAnnotation.protocols()));
		}

		for (Scheme scheme : schemes) {
			operation.scheme(scheme);
		}
	}

	public void setDeprecated(Operation operation, Method method) {
		operation.deprecated(ReflectionUtils.getAnnotation(method, Deprecated.class) != null);
	}

	public void applySecurityRequirements(ReaderContext context, Operation operation, Method method) {
		final List<SecurityRequirement> securityRequirements = new ArrayList<SecurityRequirement>();
		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
		final Api apiAnnotation = context.getCls().getAnnotation(Api.class);

		if (apiOperation != null) {
			securityRequirements.addAll(parseAuthorizations(apiOperation.authorizations()));
		}

		if (securityRequirements.isEmpty() && apiAnnotation != null) {
			securityRequirements.addAll(parseAuthorizations(apiAnnotation.authorizations()));
		}

		for (SecurityRequirement securityRequirement : securityRequirements) {
			operation.security(securityRequirement);
		}
	}

	public void applyTags(ReaderContext context, Operation operation, Method method) {
		final List<String> tags = new ArrayList<String>();

		final Api apiAnnotation = context.getCls().getAnnotation(Api.class);
		if (apiAnnotation != null) {
			tags.addAll(Collections2.filter(Arrays.asList(apiAnnotation.tags()), new Predicate<String>() {

				@Override
				public boolean apply(String input) {
					return StringUtils.isNotBlank(input);
				}
			}));
		}
		tags.addAll(context.getParentTags());

		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
		if (apiOperation != null) {
			tags.addAll(Collections2.filter(Arrays.asList(apiOperation.tags()), new Predicate<String>() {

				@Override
				public boolean apply(String input) {
					return StringUtils.isNotBlank(input);
				}
			}));
		}

		for (String tag : tags) {
			operation.tag(tag);
		}
	}

	public void applyResponses(Swagger swagger, ReaderContext context, Operation operation, Method method) {
		final Map<Integer, Response> result = new HashMap<Integer, Response>();

		final ApiOperation apiOperation = ReflectionUtils.getAnnotation(method, ApiOperation.class);
		if (apiOperation != null && StringUtils.isNotBlank(apiOperation.responseReference())) {
			final Response response = new Response().description(SUCCESSFUL_OPERATION);
			response.schema(new RefProperty(apiOperation.responseReference()));
			result.put(apiOperation.code(), response);
		}

		final Type responseType = getResponseType(method);
		if (isValidResponse(responseType)) {
			final Property property = ModelConverters.getInstance().readAsProperty(responseType);
			if (property != null) {
				final Property responseProperty = ContainerWrapper.wrapContainer(getResponseContainer(apiOperation),
						property);
				final int responseCode = apiOperation == null ? 200 : apiOperation.code();
				final Map<String, Property> defaultResponseHeaders = apiOperation == null
						? Collections.<String, Property>emptyMap()
						: parseResponseHeaders(swagger, context, apiOperation.responseHeaders());
				final Response response = new Response().description(SUCCESSFUL_OPERATION).schema(responseProperty)
						.headers(defaultResponseHeaders);
				result.put(responseCode, response);
				appendModels(swagger, responseType);
			}
		}

		final ApiResponses responseAnnotation = ReflectionUtils.getAnnotation(method, ApiResponses.class);
		if (responseAnnotation != null) {
			for (ApiResponse apiResponse : responseAnnotation.value()) {
				final Map<String, Property> responseHeaders = parseResponseHeaders(swagger, context,
						apiResponse.responseHeaders());

				final Response response = new Response().description(apiResponse.message()).headers(responseHeaders);

				if (StringUtils.isNotEmpty(apiResponse.reference())) {
					response.schema(new RefProperty(apiResponse.reference()));
				} else if (!ReflectionUtils.isVoid(apiResponse.response())) {
					final Type type = apiResponse.response();
					final Property property = ModelConverters.getInstance().readAsProperty(type);
					if (property != null) {
						response.schema(ContainerWrapper.wrapContainer(apiResponse.responseContainer(), property));
						appendModels(swagger, type);
					}
				}
				result.put(apiResponse.code(), response);
			}
		}

		for (Map.Entry<Integer, Response> responseEntry : result.entrySet()) {
			if (responseEntry.getKey() == 0) {
				operation.defaultResponse(responseEntry.getValue());
			} else {
				operation.response(responseEntry.getKey(), responseEntry.getValue());
			}
		}
	}

	public void applyParameters(String httpMethod, ReaderContext context, Operation operation,
			Annotation[] annotations) {

		for (Annotation annotation : annotations) {
			if (annotation instanceof ApiParam) {
				ApiParam apiParam = (ApiParam) annotation;

				if ("get".equalsIgnoreCase(httpMethod)) {

					QueryParameter parameter = new QueryParameter();
					parameter.setAccess(apiParam.access());
					parameter.setName(apiParam.name());
					parameter.setAllowEmptyValue(apiParam.allowEmptyValue());
					parameter.setRequired(apiParam.required());
					parameter.setDefault(apiParam.value());
					parameter.setDefaultValue(apiParam.defaultValue());
					parameter.setExample(apiParam.example());
					parameter.setFormat(apiParam.format());
					parameter.setCollectionFormat(apiParam.collectionFormat());
					parameter.setDescription(apiParam.value());
					operation.addParameter(parameter);

				}

				/**
				 * post 请求
				 */

				else if ("post".equalsIgnoreCase(httpMethod)) {
					BodyParameter parameter = new BodyParameter();
					parameter.setAccess(apiParam.access());
					parameter.setName(apiParam.name());
					//                    parameter.setAllowEmptyValue(apiParam.allowEmptyValue());
					parameter.setRequired(apiParam.required());
					parameter.setDescription(apiParam.value());

					Map<String, String> examples = new HashMap<>();
					Example example = apiParam.examples();
					if (example != null) {
						ExampleProperty[] exampleProperties = example.value();
						for (ExampleProperty ep : exampleProperties) {
							examples.put(ep.mediaType(), ep.value());
						}
					}
					parameter.setExamples(examples);
					operation.addParameter(parameter);
				}

			}
		}
	}

	public void applyImplicitParameters(Swagger swagger, ReaderContext context, Operation operation, Method method) {
		final ApiImplicitParams implicitParams = method.getAnnotation(ApiImplicitParams.class);
		if (implicitParams != null && implicitParams.value().length > 0) {
			for (ApiImplicitParam param : implicitParams.value()) {
				final Parameter p = readImplicitParam(swagger, param);
				if (p != null) {
					operation.parameter(p);
				}
			}
		}
	}

	public void applyExtensions(ReaderContext context, Operation operation, Method method) {
		final ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
		if (apiOperation != null) {
			operation.getVendorExtensions().putAll(BaseReaderUtils.parseExtensions(apiOperation.extensions()));
		}
	}

	private Parameter readImplicitParam(Swagger swagger, ApiImplicitParam param) {
		final Parameter p = ParameterFactory.createParam(param.paramType());
		if (p == null) {
			return null;
		}
		final Type type = ReflectionUtils.typeFromString(param.dataType());
		return ParameterProcessor.applyAnnotations(swagger, p, type == null ? String.class : type,
				Collections.<Annotation>singletonList(param));
	}

	enum ParameterFactory {
		PATH("path") {
			@Override
			protected Parameter create() {
				return new PathParameter();
			}
		},
		QUERY("query") {
			@Override
			protected Parameter create() {
				return new QueryParameter();
			}
		},
		FORM("form") {
			@Override
			protected Parameter create() {
				return new FormParameter();
			}
		},
		FORM_DATA("formData") {
			@Override
			protected Parameter create() {
				return new FormParameter();
			}
		},
		HEADER("header") {
			@Override
			protected Parameter create() {
				return new HeaderParameter();
			}
		},
		BODY("body") {
			@Override
			protected Parameter create() {
				return new BodyParameter();
			}
		};

		private final String paramType;

		ParameterFactory(String paramType) {
			this.paramType = paramType;
		}

		public static Parameter createParam(String paramType) {
			for (ParameterFactory item : values()) {
				if (item.paramType.equalsIgnoreCase(paramType)) {
					return item.create();
				}
			}
			LOGGER.warn("Unknown implicit parameter type: [" + paramType + "]");
			return null;
		}

		protected abstract Parameter create();
	}

	enum ContainerWrapper {
		LIST("list") {
			@Override
			protected Property doWrap(Property property) {
				return new ArrayProperty(property);
			}
		},
		ARRAY("array") {
			@Override
			protected Property doWrap(Property property) {
				return new ArrayProperty(property);
			}
		},
		MAP("map") {
			@Override
			protected Property doWrap(Property property) {
				return new MapProperty(property);
			}
		},
		SET("set") {
			@Override
			protected Property doWrap(Property property) {
				ArrayProperty arrayProperty = new ArrayProperty(property);
				arrayProperty.setUniqueItems(true);
				return arrayProperty;
			}
		};

		private final String container;

		ContainerWrapper(String container) {
			this.container = container;
		}

		public static Property wrapContainer(String container, Property property, ContainerWrapper... allowed) {
			final Set<ContainerWrapper> tmp = allowed.length > 0 ? EnumSet.copyOf(Arrays.asList(allowed))
					: EnumSet.allOf(ContainerWrapper.class);
			for (ContainerWrapper wrapper : tmp) {
				final Property prop = wrapper.wrap(container, property);
				if (prop != null) {
					return prop;
				}
			}
			return property;
		}

		public Property wrap(String container, Property property) {
			if (this.container.equalsIgnoreCase(container)) {
				return doWrap(property);
			}
			return null;
		}

		protected abstract Property doWrap(Property property);
	}

}
