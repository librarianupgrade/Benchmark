/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;

/**
 * @author Clinton Begin
 */
public class SqlSourceBuilder extends BaseBuilder {

	private static final String parameterProperties = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

	public SqlSourceBuilder(Configuration configuration) {
		super(configuration);
	}

	public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
		ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType,
				additionalParameters);
		GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
		String sql = parser.parse(originalSql);
		return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
	}

	private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

		private List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
		private Class<?> parameterType;
		private MetaObject metaParameters;

		public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType,
				Map<String, Object> additionalParameters) {
			super(configuration);
			this.parameterType = parameterType;
			this.metaParameters = configuration.newMetaObject(additionalParameters);
		}

		public List<ParameterMapping> getParameterMappings() {
			return parameterMappings;
		}

		public String handleToken(String content) {
			parameterMappings.add(buildParameterMapping(content));
			return "?";
		}

		private ParameterMapping buildParameterMapping(String content) {
			Map<String, String> propertiesMap = parseParameterMapping(content);
			String property = propertiesMap.get("property");
			Class<?> propertyType;
			if (metaParameters.hasGetter(property)) { // issue #448 get type from additional params
				propertyType = metaParameters.getGetterType(property);
			} else if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
				propertyType = parameterType;
			} else if (JdbcType.CURSOR.name().equals(propertiesMap.get("jdbcType"))) {
				propertyType = java.sql.ResultSet.class;
			} else if (property != null) {
				MetaClass metaClass = MetaClass.forClass(parameterType);
				if (metaClass.hasGetter(property)) {
					propertyType = metaClass.getGetterType(property);
				} else {
					propertyType = Object.class;
				}
			} else {
				propertyType = Object.class;
			}
			ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
			Class<?> javaType = propertyType;
			String typeHandlerAlias = null;
			for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();
				if ("javaType".equals(name)) {
					javaType = resolveClass(value);
					builder.javaType(javaType);
				} else if ("jdbcType".equals(name)) {
					builder.jdbcType(resolveJdbcType(value));
				} else if ("mode".equals(name)) {
					builder.mode(resolveParameterMode(value));
				} else if ("numericScale".equals(name)) {
					builder.numericScale(Integer.valueOf(value));
				} else if ("resultMap".equals(name)) {
					builder.resultMapId(value);
				} else if ("typeHandler".equals(name)) {
					typeHandlerAlias = value;
				} else if ("jdbcTypeName".equals(name)) {
					builder.jdbcTypeName(value);
				} else if ("property".equals(name)) {
					// Do Nothing
				} else if ("expression".equals(name)) {
					throw new BuilderException("Expression based parameters are not supported yet");
				} else {
					throw new BuilderException("An invalid property '" + name + "' was found in mapping #{" + content
							+ "}.  Valid properties are " + parameterProperties);
				}
			}
			if (typeHandlerAlias != null) {
				builder.typeHandler(resolveTypeHandler(javaType, typeHandlerAlias));
			}
			return builder.build();
		}

		private Map<String, String> parseParameterMapping(String content) {
			try {
				return new ParameterExpression(content);
			} catch (BuilderException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new BuilderException("Parsing error was found in mapping #{" + content
						+ "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
			}
		}
	}

}
