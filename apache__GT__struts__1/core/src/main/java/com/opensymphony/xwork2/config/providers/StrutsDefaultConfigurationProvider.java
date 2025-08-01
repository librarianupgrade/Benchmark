/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.opensymphony.xwork2.config.providers;

import com.opensymphony.xwork2.ActionProxyFactory;
import com.opensymphony.xwork2.DefaultActionProxyFactory;
import com.opensymphony.xwork2.DefaultLocaleProviderFactory;
import com.opensymphony.xwork2.LocaleProviderFactory;
import com.opensymphony.xwork2.StrutsTextProviderFactory;
import com.opensymphony.xwork2.TextProviderFactory;
import com.opensymphony.xwork2.factory.DefaultUnknownHandlerFactory;
import com.opensymphony.xwork2.factory.UnknownHandlerFactory;
import com.opensymphony.xwork2.ognl.BeanInfoCacheFactory;
import com.opensymphony.xwork2.ognl.ExpressionCacheFactory;
import com.opensymphony.xwork2.ognl.accessor.HttpParametersPropertyAccessor;
import com.opensymphony.xwork2.ognl.accessor.ParameterPropertyAccessor;
import com.opensymphony.xwork2.security.AcceptedPatternsChecker;
import com.opensymphony.xwork2.security.DefaultAcceptedPatternsChecker;
import com.opensymphony.xwork2.security.DefaultExcludedPatternsChecker;
import com.opensymphony.xwork2.DefaultTextProvider;
import com.opensymphony.xwork2.DefaultUnknownHandlerManager;
import com.opensymphony.xwork2.security.DefaultNotExcludedAcceptedPatternsChecker;
import com.opensymphony.xwork2.security.ExcludedPatternsChecker;
import com.opensymphony.xwork2.FileManager;
import com.opensymphony.xwork2.FileManagerFactory;
import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.UnknownHandlerManager;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.ConfigurationProvider;
import com.opensymphony.xwork2.conversion.ConversionAnnotationProcessor;
import com.opensymphony.xwork2.conversion.ConversionFileProcessor;
import com.opensymphony.xwork2.conversion.ConversionPropertiesProcessor;
import com.opensymphony.xwork2.conversion.NullHandler;
import com.opensymphony.xwork2.conversion.ObjectTypeDeterminer;
import com.opensymphony.xwork2.conversion.TypeConverterCreator;
import com.opensymphony.xwork2.conversion.TypeConverterHolder;
import com.opensymphony.xwork2.conversion.impl.ArrayConverter;
import com.opensymphony.xwork2.conversion.impl.CollectionConverter;
import com.opensymphony.xwork2.conversion.impl.DateConverter;
import com.opensymphony.xwork2.conversion.impl.DefaultConversionAnnotationProcessor;
import com.opensymphony.xwork2.conversion.impl.DefaultConversionFileProcessor;
import com.opensymphony.xwork2.security.NotExcludedAcceptedPatternsChecker;
import org.apache.struts2.components.date.DateFormatter;
import org.apache.struts2.components.date.DateTimeFormatterAdapter;
import org.apache.struts2.components.date.SimpleDateFormatAdapter;
import org.apache.struts2.conversion.StrutsConversionPropertiesProcessor;
import com.opensymphony.xwork2.conversion.impl.DefaultObjectTypeDeterminer;
import org.apache.struts2.conversion.StrutsTypeConverterCreator;
import org.apache.struts2.conversion.StrutsTypeConverterHolder;
import com.opensymphony.xwork2.conversion.impl.InstantiatingNullHandler;
import com.opensymphony.xwork2.conversion.impl.NumberConverter;
import com.opensymphony.xwork2.conversion.impl.StringConverter;
import com.opensymphony.xwork2.conversion.impl.XWorkBasicConverter;
import com.opensymphony.xwork2.conversion.impl.XWorkConverter;
import com.opensymphony.xwork2.factory.ActionFactory;
import com.opensymphony.xwork2.factory.ConverterFactory;
import com.opensymphony.xwork2.factory.DefaultActionFactory;
import com.opensymphony.xwork2.factory.StrutsConverterFactory;
import com.opensymphony.xwork2.factory.DefaultInterceptorFactory;
import com.opensymphony.xwork2.factory.DefaultResultFactory;
import com.opensymphony.xwork2.factory.InterceptorFactory;
import com.opensymphony.xwork2.factory.ResultFactory;
import com.opensymphony.xwork2.inject.ContainerBuilder;
import com.opensymphony.xwork2.inject.Scope;
import com.opensymphony.xwork2.ognl.ObjectProxy;
import com.opensymphony.xwork2.ognl.OgnlReflectionContextFactory;
import com.opensymphony.xwork2.ognl.OgnlReflectionProvider;
import com.opensymphony.xwork2.ognl.OgnlUtil;
import com.opensymphony.xwork2.ognl.OgnlValueStackFactory;
import com.opensymphony.xwork2.ognl.accessor.CompoundRootAccessor;
import com.opensymphony.xwork2.ognl.accessor.ObjectAccessor;
import com.opensymphony.xwork2.ognl.accessor.ObjectProxyPropertyAccessor;
import com.opensymphony.xwork2.ognl.accessor.XWorkCollectionPropertyAccessor;
import com.opensymphony.xwork2.ognl.accessor.XWorkEnumerationAccessor;
import com.opensymphony.xwork2.ognl.accessor.XWorkIteratorPropertyAccessor;
import com.opensymphony.xwork2.ognl.accessor.XWorkListPropertyAccessor;
import com.opensymphony.xwork2.ognl.accessor.XWorkMapPropertyAccessor;
import com.opensymphony.xwork2.ognl.accessor.XWorkMethodAccessor;
import com.opensymphony.xwork2.util.CompoundRoot;
import com.opensymphony.xwork2.LocalizedTextProvider;
import com.opensymphony.xwork2.ognl.DefaultOgnlBeanInfoCacheFactory;
import com.opensymphony.xwork2.ognl.DefaultOgnlExpressionCacheFactory;
import com.opensymphony.xwork2.util.StrutsLocalizedTextProvider;
import com.opensymphony.xwork2.util.OgnlTextParser;
import com.opensymphony.xwork2.util.PatternMatcher;
import com.opensymphony.xwork2.util.TextParser;
import com.opensymphony.xwork2.util.ValueStackFactory;
import com.opensymphony.xwork2.util.WildcardHelper;
import com.opensymphony.xwork2.util.fs.DefaultFileManager;
import com.opensymphony.xwork2.util.fs.DefaultFileManagerFactory;
import com.opensymphony.xwork2.util.location.LocatableProperties;
import com.opensymphony.xwork2.util.reflection.ReflectionContextFactory;
import com.opensymphony.xwork2.util.reflection.ReflectionProvider;
import com.opensymphony.xwork2.validator.ActionValidatorManager;
import com.opensymphony.xwork2.validator.AnnotationActionValidatorManager;
import com.opensymphony.xwork2.validator.DefaultActionValidatorManager;
import com.opensymphony.xwork2.validator.DefaultValidatorFactory;
import com.opensymphony.xwork2.validator.DefaultValidatorFileParser;
import com.opensymphony.xwork2.validator.ValidatorFactory;
import com.opensymphony.xwork2.validator.ValidatorFileParser;
import ognl.MethodAccessor;
import ognl.PropertyAccessor;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.dispatcher.HttpParameters;
import org.apache.struts2.dispatcher.Parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StrutsDefaultConfigurationProvider implements ConfigurationProvider {

	@Override
	public void destroy() {
	}

	@Override
	public void init(Configuration configuration) throws ConfigurationException {
	}

	@Override
	public void loadPackages() throws ConfigurationException {
	}

	@Override
	public boolean needsReload() {
		return false;
	}

	@Override
	public void register(ContainerBuilder builder, LocatableProperties props) throws ConfigurationException {

		builder.factory(ObjectFactory.class).factory(ActionFactory.class, DefaultActionFactory.class)
				.factory(ResultFactory.class, DefaultResultFactory.class)
				.factory(InterceptorFactory.class, DefaultInterceptorFactory.class)
				.factory(com.opensymphony.xwork2.factory.ValidatorFactory.class,
						com.opensymphony.xwork2.factory.DefaultValidatorFactory.class)
				.factory(ConverterFactory.class, StrutsConverterFactory.class)
				.factory(UnknownHandlerFactory.class, DefaultUnknownHandlerFactory.class)

				.factory(ActionProxyFactory.class, DefaultActionProxyFactory.class, Scope.SINGLETON)
				.factory(ObjectTypeDeterminer.class, DefaultObjectTypeDeterminer.class, Scope.SINGLETON)

				.factory(XWorkConverter.class, Scope.SINGLETON).factory(XWorkBasicConverter.class, Scope.SINGLETON)
				.factory(ConversionPropertiesProcessor.class, StrutsConversionPropertiesProcessor.class,
						Scope.SINGLETON)
				.factory(ConversionFileProcessor.class, DefaultConversionFileProcessor.class, Scope.SINGLETON)
				.factory(ConversionAnnotationProcessor.class, DefaultConversionAnnotationProcessor.class,
						Scope.SINGLETON)
				.factory(TypeConverterCreator.class, StrutsTypeConverterCreator.class, Scope.SINGLETON)
				.factory(TypeConverterHolder.class, StrutsTypeConverterHolder.class, Scope.SINGLETON)

				.factory(FileManager.class, "system", DefaultFileManager.class, Scope.SINGLETON)
				.factory(FileManagerFactory.class, DefaultFileManagerFactory.class, Scope.SINGLETON)
				.factory(ValueStackFactory.class, OgnlValueStackFactory.class, Scope.SINGLETON)
				.factory(ValidatorFactory.class, DefaultValidatorFactory.class, Scope.SINGLETON)
				.factory(ValidatorFileParser.class, DefaultValidatorFileParser.class, Scope.SINGLETON)
				.factory(PatternMatcher.class, WildcardHelper.class, Scope.SINGLETON)
				.factory(ReflectionProvider.class, OgnlReflectionProvider.class, Scope.SINGLETON)
				.factory(ReflectionContextFactory.class, OgnlReflectionContextFactory.class, Scope.SINGLETON)

				.factory(PropertyAccessor.class, CompoundRoot.class.getName(), CompoundRootAccessor.class,
						Scope.SINGLETON)
				.factory(PropertyAccessor.class, Object.class.getName(), ObjectAccessor.class, Scope.SINGLETON)
				.factory(PropertyAccessor.class, Iterator.class.getName(), XWorkIteratorPropertyAccessor.class,
						Scope.SINGLETON)
				.factory(PropertyAccessor.class, Enumeration.class.getName(), XWorkEnumerationAccessor.class,
						Scope.SINGLETON)

				.factory(UnknownHandlerManager.class, DefaultUnknownHandlerManager.class, Scope.SINGLETON)

				// silly workarounds for ognl since there is no way to flush its caches
				.factory(PropertyAccessor.class, List.class.getName(), XWorkListPropertyAccessor.class, Scope.SINGLETON)
				.factory(PropertyAccessor.class, ArrayList.class.getName(), XWorkListPropertyAccessor.class,
						Scope.SINGLETON)
				.factory(PropertyAccessor.class, HashSet.class.getName(), XWorkCollectionPropertyAccessor.class,
						Scope.SINGLETON)
				.factory(PropertyAccessor.class, Set.class.getName(), XWorkCollectionPropertyAccessor.class,
						Scope.SINGLETON)
				.factory(PropertyAccessor.class, HashMap.class.getName(), XWorkMapPropertyAccessor.class,
						Scope.SINGLETON)
				.factory(PropertyAccessor.class, Map.class.getName(), XWorkMapPropertyAccessor.class, Scope.SINGLETON)
				.factory(PropertyAccessor.class, Collection.class.getName(), XWorkCollectionPropertyAccessor.class,
						Scope.SINGLETON)
				.factory(PropertyAccessor.class, ObjectProxy.class.getName(), ObjectProxyPropertyAccessor.class,
						Scope.SINGLETON)
				.factory(PropertyAccessor.class, HttpParameters.class.getName(), HttpParametersPropertyAccessor.class,
						Scope.SINGLETON)
				.factory(PropertyAccessor.class, Parameter.class.getName(), ParameterPropertyAccessor.class,
						Scope.SINGLETON)

				.factory(MethodAccessor.class, Object.class.getName(), XWorkMethodAccessor.class, Scope.SINGLETON)
				.factory(MethodAccessor.class, CompoundRoot.class.getName(), CompoundRootAccessor.class,
						Scope.SINGLETON)

				.factory(TextParser.class, OgnlTextParser.class, Scope.SINGLETON)

				.factory(NullHandler.class, Object.class.getName(), InstantiatingNullHandler.class, Scope.SINGLETON)
				.factory(ActionValidatorManager.class, AnnotationActionValidatorManager.class, Scope.SINGLETON)
				.factory(ActionValidatorManager.class, "no-annotations", DefaultActionValidatorManager.class,
						Scope.SINGLETON)

				.factory(TextProvider.class, "system", DefaultTextProvider.class, Scope.SINGLETON)
				.factory(LocalizedTextProvider.class, StrutsLocalizedTextProvider.class, Scope.SINGLETON)
				.factory(TextProviderFactory.class, StrutsTextProviderFactory.class, Scope.SINGLETON)
				.factory(LocaleProviderFactory.class, DefaultLocaleProviderFactory.class, Scope.SINGLETON)

				.factory(ExpressionCacheFactory.class, "defaultOgnlExpressionCacheFactory",
						DefaultOgnlExpressionCacheFactory.class, Scope.SINGLETON)
				.factory(BeanInfoCacheFactory.class, "defaultOgnlBeanInfoCacheFactory",
						DefaultOgnlBeanInfoCacheFactory.class, Scope.SINGLETON)
				.factory(OgnlUtil.class, Scope.SINGLETON).factory(CollectionConverter.class, Scope.SINGLETON)
				.factory(ArrayConverter.class, Scope.SINGLETON).factory(DateConverter.class, Scope.SINGLETON)
				.factory(NumberConverter.class, Scope.SINGLETON).factory(StringConverter.class, Scope.SINGLETON)

				.factory(ExcludedPatternsChecker.class, DefaultExcludedPatternsChecker.class, Scope.PROTOTYPE)
				.factory(AcceptedPatternsChecker.class, DefaultAcceptedPatternsChecker.class, Scope.PROTOTYPE)
				.factory(NotExcludedAcceptedPatternsChecker.class, DefaultNotExcludedAcceptedPatternsChecker.class,
						Scope.SINGLETON)

				.factory(ValueSubstitutor.class, EnvsValueSubstitutor.class, Scope.SINGLETON)

				.factory(DateFormatter.class, "simpleDateFormatter", SimpleDateFormatAdapter.class, Scope.SINGLETON)
				.factory(DateFormatter.class, "dateTimeFormatter", DateTimeFormatterAdapter.class, Scope.SINGLETON);

		props.setProperty(StrutsConstants.STRUTS_ENABLE_DYNAMIC_METHOD_INVOCATION, Boolean.FALSE.toString());
		props.setProperty(StrutsConstants.STRUTS_I18N_RELOAD, Boolean.FALSE.toString());
		props.setProperty(StrutsConstants.STRUTS_DEVMODE, Boolean.FALSE.toString());
		props.setProperty(StrutsConstants.STRUTS_OGNL_LOG_MISSING_PROPERTIES, Boolean.FALSE.toString());
		props.setProperty(StrutsConstants.STRUTS_OGNL_ENABLE_EXPRESSION_CACHE, Boolean.TRUE.toString());
		props.setProperty(StrutsConstants.STRUTS_OGNL_ENABLE_EVAL_EXPRESSION, Boolean.FALSE.toString());
		props.setProperty(StrutsConstants.STRUTS_CONFIGURATION_XML_RELOAD, Boolean.FALSE.toString());
		props.setProperty(StrutsConstants.STRUTS_ALLOW_STATIC_FIELD_ACCESS, Boolean.TRUE.toString());
		props.setProperty(StrutsConstants.STRUTS_MATCHER_APPEND_NAMED_PARAMETERS, Boolean.TRUE.toString());
	}

}
