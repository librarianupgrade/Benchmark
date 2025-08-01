/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.services.ScriptsService;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonExecutorConfigurationDataService;
import io.cloudslang.runtime.api.python.PythonExecutorLifecycleManagerService;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.api.python.entities.PythonExecutorDetails;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonExecutor;
import io.cloudslang.runtime.impl.python.executor.ExternalPythonExecutorServiceImpl;
import io.cloudslang.runtime.impl.python.executor.PythonExecutorCommunicationServiceImpl;
import io.cloudslang.runtime.impl.python.executor.PythonExecutorLifecycleManagerServiceImpl;
import io.cloudslang.runtime.impl.python.external.ExternalPythonExecutionEngine;
import io.cloudslang.runtime.impl.python.external.ExternalPythonRuntimeServiceImpl;
import io.cloudslang.runtime.impl.python.external.StatefulRestEasyClientsHolder;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.python.core.PyDictionary;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.python.google.common.collect.Sets.newHashSet;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScriptEvaluatorTest.Config.class)
public class ScriptEvaluatorTest {
	static {
		System.setProperty("python.expressionsEval", "jython");
	}

	private static String LINE_SEPARATOR = "\n";
	private static final String SYSTEM_PROPERTIES_MAP = "sys_prop";
	private static final String ACCESS_MONITORING_METHOD_NAME = "accessed";
	private static final String GET_FUNCTION_DEFINITION = "def get(key, default_value=None):" + LINE_SEPARATOR
			+ "  try:" + LINE_SEPARATOR + "    value = get_from_smaller_context(key)" + LINE_SEPARATOR
			+ "  except NameError:" + LINE_SEPARATOR + "    value = globals().get(key)" + LINE_SEPARATOR
			+ "  return default_value if value is None else value";
	private static final String GET_SP_FUNCTION_DEFINITION = "def get_sp(key, default_value=None):" + LINE_SEPARATOR
			+ "  " + ACCESS_MONITORING_METHOD_NAME + "(key)" + LINE_SEPARATOR + "  property_value = "
			+ SYSTEM_PROPERTIES_MAP + ".get(key)" + LINE_SEPARATOR
			+ "  return default_value if property_value is None else property_value";
	private static final String CHECK_EMPTY_FUNCTION_DEFINITION = "def check_empty(value_to_check, default_value=None):"
			+ LINE_SEPARATOR + "  return default_value if value_to_check is None else value_to_check";
	private static final String BACKWARD_COMPATIBLE_ACCESS_METHOD = "def " + ACCESS_MONITORING_METHOD_NAME + "(key):"
			+ LINE_SEPARATOR + "  pass";

	@Autowired
	private ScriptEvaluator scriptEvaluator;

	@Autowired
	private PythonInterpreter pythonInterpreter;

	@Resource(name = "jythonRuntimeService")
	private PythonRuntimeService pythonRuntimeService;

	@Test
	public void testEvalExpr() throws Exception {
		reset(pythonRuntimeService);
		when(pythonRuntimeService.eval(anyString(), anyString(), isA(Map.class)))
				.thenReturn(new PythonEvaluationResult("result", new HashMap<String, Serializable>()));
		scriptEvaluator.evalExpr("", new HashMap<String, Value>(), new HashSet<SystemProperty>(),
				new HashSet<ScriptFunction>());
		verify(pythonRuntimeService).eval(eq(""), anyString(), anyMap());
	}

	@Test
	public void testEvalExprError() throws Exception {
		reset(pythonRuntimeService);
		when(pythonRuntimeService.eval(anyString(), anyString(), anyMap()))
				.thenThrow(new RuntimeException("error from interpreter"));
		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> scriptEvaluator.evalExpr("input_expression", new HashMap<String, Value>(),
						new HashSet<SystemProperty>(), new HashSet<ScriptFunction>()));
		Assert.assertEquals("Error in evaluating expression: 'input_expression',\n" + "\terror from interpreter",
				exception.getMessage());
	}

	@Test
	public void testEvalFunctions() throws Exception {
		reset(pythonRuntimeService);
		Set<SystemProperty> props = new HashSet<>();
		SystemProperty systemProperty = new SystemProperty("a.b", "c.key", "value", "");
		props.add(systemProperty);
		Set<ScriptFunction> functionDependencies = newHashSet(ScriptFunction.GET, ScriptFunction.GET_SYSTEM_PROPERTY,
				ScriptFunction.CHECK_EMPTY);
		final ArgumentCaptor<String> scriptCaptor = ArgumentCaptor.forClass(String.class);

		Map<String, Serializable> scriptReturnContext = new HashMap<>();
		scriptReturnContext.put(SYSTEM_PROPERTIES_MAP, new PyDictionary());

		when(pythonRuntimeService.eval(anyString(), anyString(), isA(Map.class)))
				.thenReturn(new PythonEvaluationResult("result", scriptReturnContext));

		String expr = "";
		scriptEvaluator.evalExpr(expr, new HashMap<String, Value>(), props, functionDependencies);

		Map<String, Serializable> expectedContext = new HashMap<>();
		Map<String, Value> properties = new HashMap<>();
		properties.put("a.b.c.key", ValueFactory.createPyObjectValue("value", false, false));

		verify(pythonRuntimeService).eval(scriptCaptor.capture(), eq(expr), eq(expectedContext));

		final String actualScript = scriptCaptor.getValue();
		String[] actualFunctionsArray = actualScript.split(LINE_SEPARATOR + LINE_SEPARATOR);
		Set<String> actualFunctions = new HashSet<>();
		Collections.addAll(actualFunctions, actualFunctionsArray);
		assertThat(actualFunctions.size(), is(4));
		assertThat(actualFunctions, containsInAnyOrder(GET_FUNCTION_DEFINITION, GET_SP_FUNCTION_DEFINITION,
				CHECK_EMPTY_FUNCTION_DEFINITION, BACKWARD_COMPATIBLE_ACCESS_METHOD));
	}

	@Configuration
	static class Config {
		@Bean
		public ScriptExecutor scriptExecutor() {
			return new ScriptExecutor();
		}

		@Bean
		public ScriptEvaluator scriptEvaluator() {
			return new ScriptEvaluator();
		}

		@Bean
		public ScriptsService scriptsService() {
			return new ScriptsService();
		}

		@Bean
		public DependencyService mavenRepositoryService() {
			return new DependencyServiceImpl();
		}

		@Bean
		public MavenConfig mavenConfig() {
			return new MavenConfigImpl();
		}

		@Bean
		public PythonInterpreter pythonInterpreter() {
			return mock(PythonInterpreter.class);
		}

		@Bean(name = "jythonRuntimeService")
		public PythonRuntimeService pythonRuntimeService() {
			return mock(PythonRuntimeService.class);
		}

		@Bean(name = "jythonExecutionEngine")
		public PythonExecutionEngine pythonExecutionEngine() {
			return new PythonExecutionCachedEngine() {
				protected PythonExecutor createNewExecutor(Set<String> filePaths) {
					return new PythonExecutor(filePaths) {
						protected PythonInterpreter initInterpreter(Set<String> dependencies) {
							return pythonInterpreter();
						}
					};
				}
			};
		}

		@Bean(name = "pythonExecutorConfigurationDataService")
		public PythonExecutorConfigurationDataService pythonExecutorConfigurationDataService() {
			return PythonExecutorDetails::new;
		}

		@Bean(name = "pythonExecutorCommunicationService")
		public PythonExecutorCommunicationServiceImpl pythonExecutorCommunicationService() {
			return new PythonExecutorCommunicationServiceImpl(mock(StatefulRestEasyClientsHolder.class),
					mock(PythonExecutorConfigurationDataService.class));
		}

		@Bean(name = "pythonExecutorLifecycleManagerService")
		public PythonExecutorLifecycleManagerService pythonExecutorLifecycleManagerService() {
			return new PythonExecutorLifecycleManagerServiceImpl(pythonExecutorCommunicationService(),
					pythonExecutorConfigurationDataService());
		}

		@Bean(name = "externalPythonExecutorService")
		public PythonRuntimeService externalPythonExecutorService() {
			return new ExternalPythonExecutorServiceImpl(new Semaphore(100), new Semaphore(50));
		}

		@Bean(name = "externalPythonRuntimeService")
		public PythonRuntimeService externalPythonRuntimeService() {
			return new ExternalPythonRuntimeServiceImpl(new Semaphore(100), new Semaphore(50));
		}

		@Bean(name = "externalPythonExecutionEngine")
		public PythonExecutionEngine externalPythonExecutionEngine() {
			return new ExternalPythonExecutionEngine();
		}

		@Bean
		public EventBus eventBus() {
			return new EventBusImpl();
		}
	}
}
