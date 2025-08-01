/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.steps;

import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.lang.compiler.modeller.model.SeqStep;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.encryption.DummyEncryptor;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptExecutor;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.lang.runtime.services.ScriptsService;
import io.cloudslang.lang.runtime.steps.ContentTestActions.NonSerializableObject;
import io.cloudslang.runtime.api.java.JavaRuntimeService;
import io.cloudslang.runtime.api.python.PythonExecutorConfigurationDataService;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.api.python.entities.PythonExecutorDetails;
import io.cloudslang.runtime.api.sequential.SequentialExecutionService;
import io.cloudslang.runtime.impl.java.JavaExecutionCachedEngine;
import io.cloudslang.runtime.impl.java.JavaRuntimeServiceImpl;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import io.cloudslang.runtime.impl.python.executor.ExternalPythonExecutorServiceImpl;
import io.cloudslang.runtime.impl.python.executor.PythonExecutorCommunicationServiceImpl;
import io.cloudslang.runtime.impl.python.external.ExternalPythonExecutionEngine;
import io.cloudslang.runtime.impl.python.external.ExternalPythonRuntimeServiceImpl;
import io.cloudslang.runtime.impl.python.external.StatefulRestEasyClientsHolder;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static io.cloudslang.lang.entities.ActionType.JAVA;
import static io.cloudslang.lang.entities.ActionType.PYTHON;
import static io.cloudslang.lang.entities.ActionType.SEQUENTIAL;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * Date: 10/31/2014
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ActionStepsTest.Config.class)
public class ActionStepsTest {

	private static final long DEFAULT_TIMEOUT = 10000;
	private static final String NON_SERIALIZABLE_VARIABLE_NAME = "current_time";
	//    private static final String GAV_DEFAULT = "g:a:v";
	private static final String GAV_DEFAULT = "";
	//    private static final ArrayList<String> DEPENDENCIES_DEFAULT = Lists.newArrayList("dep1", "dep2");
	private static final List<String> DEPENDENCIES_DEFAULT = Collections.emptyList();
	private static final String GLOBAL_SESSION_OBJECT = "globalSessionObject";
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	ExecutionRuntimeServices executionRuntimeServicesMock = mock(ExecutionRuntimeServices.class);
	private Map<String, Map<String, Object>> nonSerializableExecutionData;
	private List<SeqStep> seqSteps;
	@Autowired
	private ActionExecutionData actionSteps;

	@Autowired
	private SequentialExecutionService seqExecutionService;

	@Before
	public void setUp() {
		nonSerializableExecutionData = new HashMap<>();
		seqSteps = new ArrayList<>();
		reset(seqExecutionService);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doActionJavaTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("name", ValueFactory.create("nameTest"));
		initialCallArguments.put("role", ValueFactory.create("roleTest"));
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "doJavaSampleAction", GAV_DEFAULT, null, true, DEPENDENCIES_DEFAULT,
				seqSteps, null, null);

		//construct expected outputs
		Map<String, Value> expectedOutputs = new HashMap<>();
		expectedOutputs.put("name", ValueFactory.create("nameTest"));
		expectedOutputs.put("role", ValueFactory.create("roleTest"));

		//extract actual outputs
		ReturnValues actualReturnValues = runEnv.removeReturnValues();
		Map<String, Value> actualOutputs = actualReturnValues.getOutputs();

		//verify matching
		assertEquals("Java action outputs are not as expected", expectedOutputs, actualOutputs);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doActionSetNextPositionTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("name", ValueFactory.create("nameTest"));
		initialCallArguments.put("role", ValueFactory.create("roleTest"));
		runEnv.putCallArguments(initialCallArguments);

		Long nextStepPosition = 2L;

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, nextStepPosition, JAVA,
				ContentTestActions.class.getName(), "doJavaSampleAction", GAV_DEFAULT, null, true, DEPENDENCIES_DEFAULT,
				seqSteps, null, null);

		//verify matching
		assertEquals(nextStepPosition, runEnv.removeNextStepPosition());
	}

	@Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
	public void doJavaActionWrongClassTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

		//invoke doAction
		actionSteps.doAction(runtimeServices, runEnv, nonSerializableExecutionData, 2L, JAVA, "MissingClassName",
				"doJavaSampleAction", GAV_DEFAULT, null, true, DEPENDENCIES_DEFAULT, seqSteps, null, null);

		Collection<ScoreEvent> events = runtimeServices.getEvents();

		Assert.assertFalse(events.isEmpty());
		ScoreEvent actionErrorEvent = null;
		ScoreEvent actionEndEvent = null;
		for (ScoreEvent event : events) {
			if (event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_ERROR)) {
				actionErrorEvent = event;
			} else if (event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_END)) {
				actionEndEvent = event;
			}
		}
		Assert.assertNotNull(actionErrorEvent);
		Assert.assertNotNull(actionEndEvent);
	}

	@Test
	public void doActionPythonActionCheckCallArgumentsOnEvent() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

		Map<String, Value> callArguments = new HashMap<>();
		callArguments.put("index", ValueFactory.create(1));
		runEnv.putCallArguments(callArguments);

		String userPythonScript = "var= \"hello\"";

		//invoke doAction
		actionSteps.doAction(runtimeServices, runEnv, nonSerializableExecutionData, 2L, PYTHON, "", "", GAV_DEFAULT,
				userPythonScript, true, DEPENDENCIES_DEFAULT, seqSteps, null, null);

		Collection<ScoreEvent> events = runtimeServices.getEvents();

		Assert.assertFalse(events.isEmpty());
		ScoreEvent eventActionStart = null;
		for (ScoreEvent event : events) {
			if (event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_START)) {
				eventActionStart = event;
				break;
			}
		}

		Assert.assertNotNull(eventActionStart);
		LanguageEventData data = (LanguageEventData) eventActionStart.getData();
		Map<String, Serializable> actualCallArguments = data.getCallArguments();
		assertEquals(callArguments.size(), actualCallArguments.size());
		for (Map.Entry<String, Value> entry : callArguments.entrySet()) {
			assertEquals("Python action call arguments are not as expected", entry.getValue().get(),
					actualCallArguments.get(entry.getKey()));
		}
	}

	@Test
	public void doActionJavaActionCheckCallArgumentsOnEvent() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

		Map<String, Value> callArguments = new HashMap<>();
		callArguments.put("index", ValueFactory.create(1));
		runEnv.putCallArguments(callArguments);

		//invoke doAction
		actionSteps.doAction(runtimeServices, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "doJavaSampleAction", GAV_DEFAULT, null, true, DEPENDENCIES_DEFAULT,
				seqSteps, null, null);

		Collection<ScoreEvent> events = runtimeServices.getEvents();

		Assert.assertFalse(events.isEmpty());
		ScoreEvent eventActionStart = null;
		for (ScoreEvent event : events) {
			if (event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_START)) {
				eventActionStart = event;
				break;
			}
		}

		Assert.assertNotNull(eventActionStart);
		LanguageEventData data = (LanguageEventData) eventActionStart.getData();
		Map<String, Serializable> actualCallArguments = data.getCallArguments();
		assertEquals(callArguments.size(), actualCallArguments.size());
		for (Map.Entry<String, Value> entry : callArguments.entrySet()) {
			assertEquals("Python action call arguments are not as expected", entry.getValue().get(),
					actualCallArguments.get(entry.getKey()));
		}
	}

	@Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
	public void doJavaActionWrongMethodTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

		//invoke doAction
		actionSteps.doAction(runtimeServices, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "wrongMethodName", GAV_DEFAULT, null, true, DEPENDENCIES_DEFAULT,
				seqSteps, null, null);

		Collection<ScoreEvent> events = runtimeServices.getEvents();

		Assert.assertFalse(events.isEmpty());
		ScoreEvent actionErrorEvent = null;
		ScoreEvent actionEndEvent = null;
		for (ScoreEvent event : events) {
			if (event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_ERROR)) {
				actionErrorEvent = event;
			} else if (event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_END)) {
				actionEndEvent = event;
			}
		}
		Assert.assertNotNull(actionErrorEvent);
		Assert.assertNotNull(actionEndEvent);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doJavaActionWithExceptionTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

		boolean exceptionThrown = false;
		try {
			//invoke doAction
			actionSteps.doAction(runtimeServices, runEnv, nonSerializableExecutionData, 2L, JAVA,
					ContentTestActions.class.getName(), "doJavaActionExceptionMethod", GAV_DEFAULT, null, true,
					DEPENDENCIES_DEFAULT, seqSteps, null, null);
		} catch (RuntimeException ex) {
			exceptionThrown = true;
		}

		Assert.assertTrue(exceptionThrown);

		Collection<ScoreEvent> events = runtimeServices.getEvents();

		Assert.assertFalse(events.isEmpty());
		ScoreEvent actionErrorEvent = null;
		ScoreEvent actionEndEvent = null;
		for (ScoreEvent event : events) {
			if (event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_ERROR)) {
				actionErrorEvent = event;
			} else if (event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_END)) {
				actionEndEvent = event;
			}
		}
		Assert.assertNotNull(actionErrorEvent);
		Assert.assertNull(actionEndEvent);
	}

	@Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
	public void doJavaActionWrongReturnTypeTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

		//invoke doAction
		actionSteps.doAction(runtimeServices, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "doJavaActionWrongReturnType", GAV_DEFAULT, null, true,
				DEPENDENCIES_DEFAULT, seqSteps, null, null);

		Collection<ScoreEvent> events = runtimeServices.getEvents();

		Assert.assertFalse(events.isEmpty());
		ScoreEvent actionErrorEvent = null;
		ScoreEvent actionEndEvent = null;
		for (ScoreEvent event : events) {
			if (event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_ERROR)) {
				actionErrorEvent = event;
			} else if (event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_END)) {
				actionEndEvent = event;
			}
		}
		Assert.assertNotNull(actionErrorEvent);
		Assert.assertNotNull(actionEndEvent);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doActionJavaMissingInputTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("name", ValueFactory.create("nameTest"));
		// missing role
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "doJavaSampleAction", GAV_DEFAULT, null, true, DEPENDENCIES_DEFAULT,
				seqSteps, null, null);

		//construct expected outputs
		Map<String, Serializable> expectedOutputs = new HashMap<>();
		expectedOutputs.put("name", ValueFactory.create("nameTest"));
		expectedOutputs.put("role", ValueFactory.create(null));

		//extract actual outputs
		ReturnValues actualReturnValues = runEnv.removeReturnValues();
		Map<String, Value> actualOutputs = actualReturnValues.getOutputs();

		//verify matching
		assertEquals("Java action outputs are not as expected", expectedOutputs, actualOutputs);
	}

	@Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
	public void doJavaActionParameterTypeMismatch() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("port", ValueFactory.create(5)); //should be string
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "doJavaNumberAsString", GAV_DEFAULT, null, true,
				DEPENDENCIES_DEFAULT, seqSteps, null, null);
	}

	@Test
	public void doJavaActionParameterAndReturnTypeInteger() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("port", ValueFactory.create(5));
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "doJavaNumbersAction", GAV_DEFAULT, null, true,
				DEPENDENCIES_DEFAULT, seqSteps, null, null);
		ReturnValues returnValues = runEnv.removeReturnValues();
		assertEquals(5, returnValues.getOutputs().get("port").get());
	}

	@Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
	public void doActionJavaMissingActionTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("name", ValueFactory.create("nameTest"));
		initialCallArguments.put("role", ValueFactory.create("roleTest"));
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "doJavaSampleAction_NOT_FOUND", GAV_DEFAULT, null, true,
				DEPENDENCIES_DEFAULT, seqSteps, null, null);

		//construct expected outputs
		Map<String, Serializable> expectedOutputs = new HashMap<>();

		//extract actual outputs
		ReturnValues actualReturnValues = runEnv.removeReturnValues();
		Map<String, Value> actualOutputs = actualReturnValues.getOutputs();

		//verify matching
		assertEquals("Java action output should be empty map", expectedOutputs, actualOutputs);
	}

	@Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
	public void doActionJavaMissingActionAnnotationTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("name", ValueFactory.create("nameTest"));
		initialCallArguments.put("role", ValueFactory.create("roleTest"));
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "doJavaActionMissingAnnotation", GAV_DEFAULT, null, true,
				DEPENDENCIES_DEFAULT, seqSteps, null, null);

		//construct expected outputs
		Map<String, Serializable> expectedOutputs = new HashMap<>();

		//extract actual outputs
		ReturnValues actualReturnValues = runEnv.removeReturnValues();
		Map<String, Value> actualOutputs = actualReturnValues.getOutputs();

		//verify matching
		assertEquals("Java action output should be empty map", expectedOutputs, actualOutputs);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doJavaActionGetKeyFromNonSerializableSessionTest() {
		//prepare doAction arguments
		HashMap<String, Map<String, Object>> nonSerializableExecutionData = new HashMap<>();
		HashMap<String, Object> globalSessionObject = new HashMap<>();
		GlobalSessionObject<NonSerializableObject> sessionObject = new GlobalSessionObject<>();
		NonSerializableObject employee = new NonSerializableObject("John");
		sessionObject.setResource(new ContentTestActions.NonSerializableSessionResource(employee));
		globalSessionObject.put("name", sessionObject);
		nonSerializableExecutionData.put(GLOBAL_SESSION_OBJECT, globalSessionObject);
		//invoke doAction
		RunEnvironment runEnv = new RunEnvironment();
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "getNameFromNonSerializableSession", GAV_DEFAULT, null, true,
				DEPENDENCIES_DEFAULT, seqSteps, null, null);

		Map<String, Value> outputs = runEnv.removeReturnValues().getOutputs();
		Assert.assertTrue(outputs.containsKey("name"));
		assertEquals("John", outputs.get("name").get());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doJavaActionSetKeyOnNonSerializableSessionTest() {
		//prepare doAction arguments
		final RunEnvironment runEnv = new RunEnvironment();
		HashMap<String, Map<String, Object>> nonSerializableExecutionData = new HashMap<>();
		HashMap<String, Object> globalSessionObjectData = new HashMap<>();

		GlobalSessionObject<NonSerializableObject> sessionObject = new GlobalSessionObject<>();
		NonSerializableObject employee = new NonSerializableObject("John");
		sessionObject.setResource(new ContentTestActions.NonSerializableSessionResource(employee));

		globalSessionObjectData.put("name", sessionObject);
		nonSerializableExecutionData.put(ExecutionParametersConsts.GLOBAL_SESSION_OBJECT, globalSessionObjectData);

		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("value", ValueFactory.create("David"));
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "setNameOnNonSerializableSession", GAV_DEFAULT, null, true,
				DEPENDENCIES_DEFAULT, seqSteps, null, null);

		Map<String, Object> globalSessionObject = nonSerializableExecutionData.get(GLOBAL_SESSION_OBJECT);
		Assert.assertTrue(globalSessionObject.containsKey("name"));

		@SuppressWarnings("unchecked")
		GlobalSessionObject<NonSerializableObject> updatedSessionObject = (GlobalSessionObject<NonSerializableObject>) globalSessionObject
				.get("name");
		NonSerializableObject nonSerializableObject = updatedSessionObject.get();
		String actualName = nonSerializableObject.getName();
		assertEquals("David", actualName);
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doJavaActionGetNonExistingKeyFromNonSerializableSessionTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "getNameFromNonSerializableSession", GAV_DEFAULT, null, true,
				DEPENDENCIES_DEFAULT, seqSteps, null, null);

		Map<String, Value> outputs = runEnv.removeReturnValues().getOutputs();
		Assert.assertTrue(outputs.containsKey("name"));
		Assert.assertNull(outputs.get("name").get());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doJavaActionGetKeyFromSerializableSessionTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		final Map<String, Value> initialCallArguments = new HashMap<>();
		HashMap<String, SerializableSessionObject> serializableExecutionData = new HashMap<>();
		SerializableSessionObject sessionObject = new SerializableSessionObject();
		sessionObject.setName("John");
		serializableExecutionData.put("name", sessionObject);
		runEnv.getSerializableDataMap().putAll(serializableExecutionData);
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "getNameFromSerializableSession", GAV_DEFAULT, null, true,
				DEPENDENCIES_DEFAULT, seqSteps, null, null);

		Map<String, Value> outputs = runEnv.removeReturnValues().getOutputs();
		Assert.assertTrue(outputs.containsKey("name"));
		assertEquals("John", outputs.get("name").get());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doJavaActionGetNonExistingKeyFromSerializableSessionTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		HashMap<String, Object> serializableExecutionData = new HashMap<>();
		initialCallArguments.put(ExecutionParametersConsts.SERIALIZABLE_SESSION_CONTEXT,
				ValueFactory.create(serializableExecutionData));
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "getNameFromSerializableSession", GAV_DEFAULT, null, true,
				DEPENDENCIES_DEFAULT, seqSteps, null, null);

		Map<String, Value> outputs = runEnv.removeReturnValues().getOutputs();
		Assert.assertTrue(outputs.containsKey("name"));
		Assert.assertNull(outputs.get("name").get());
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void doJavaActionGetNonExistingKeyFromNonExistingSerializableSessionTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, JAVA,
				ContentTestActions.class.getName(), "getNameFromSerializableSession", GAV_DEFAULT, null, true,
				DEPENDENCIES_DEFAULT, seqSteps, null, null);

		Map<String, SerializableSessionObject> serializableSessionMap = runEnv.getSerializableDataMap();
		Assert.assertTrue(serializableSessionMap.containsKey("name"));
	}

	@Test
	public void doActionPythonTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("host", ValueFactory.create("localhost"));
		initialCallArguments.put("port", ValueFactory.create("8080"));
		runEnv.putCallArguments(initialCallArguments);

		Map<String, Map<String, Object>> nonSerializableExecutionData = new HashMap<>();

		String userPythonScript = "import os\n" + "print host\n" + "print port\n" + "os.system(\"ping -c 1 \" + host)\n"
				+ "url = 'http://' + host + ':' + str(port)\n" + "url2 = url + '/oo'\n" + "another = 'just a string'\n"
				+
				// we can also change..
				"port = 8081\n" + "condition = 1==1\n" + "print url";

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, PYTHON, "", "",
				GAV_DEFAULT, userPythonScript, true, DEPENDENCIES_DEFAULT, seqSteps, null, null);

		//construct expected outputs
		Map<String, Serializable> expectedOutputs = new HashMap<>();
		expectedOutputs.put("host", ValueFactory.create("localhost"));
		expectedOutputs.put("port", ValueFactory.create(8081));
		expectedOutputs.put("url", ValueFactory.create("http://localhost:8080"));
		expectedOutputs.put("url2", ValueFactory.create("http://localhost:8080/oo"));
		expectedOutputs.put("another", ValueFactory.create("just a string"));
		expectedOutputs.put("condition", ValueFactory.create(true));

		//extract actual outputs
		ReturnValues actualReturnValues = runEnv.removeReturnValues();
		Map<String, Value> actualOutputs = actualReturnValues.getOutputs();

		//verify matching
		assertEquals("Python action outputs are not as expected", expectedOutputs, actualOutputs);
	}

	@Test
	public void doActionInvalidReturnTypes() throws IOException {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		File file = folder.newFile();

		String fileAbsolutePathEscaped = file.getAbsolutePath().replace("\\", "\\\\");

		String userPythonScript = "valid = 1\n" + "with open('" + fileAbsolutePathEscaped + "', 'r') as f:\n"
				+ "  f.close()\n\n" + "import sys\n" + "import io\n" + "def a():\n" + "  print 'a'\n\n";

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, PYTHON, "", "",
				GAV_DEFAULT, userPythonScript, true, DEPENDENCIES_DEFAULT, seqSteps, null, null);

		//extract actual outputs
		ReturnValues actualReturnValues = runEnv.removeReturnValues();
		Map<String, Value> actualOutputs = actualReturnValues.getOutputs();

		//verify matching
		assertEquals("Invalid types passed exclusion", 1, actualOutputs.size());
	}

	@Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
	public void doActionPythonMissingInputsTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		//missing inputs
		Map<String, Value> initialCallArguments = new HashMap<>();
		runEnv.putCallArguments(initialCallArguments);

		String userPythonScript = "import os\n" + "print host\n" + "print port\n" + "os.system(\"ping -c 1 \" + host)\n"
				+ "url = 'http://' + host + ':' + str(port)\n" + "url2 = url + '/oo'\n" + "another = 'just a string'\n"
				+ "port = 8081\n" + "print url";

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, PYTHON, "", "",
				GAV_DEFAULT, userPythonScript, true, DEPENDENCIES_DEFAULT, seqSteps, null, null);
	}

	@Test
	public void doActionPythonImportRightIoPackage() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();

		String userPythonScript = "import io\n" + "if 'StringIO' not in dir(io):\n"
				+ "  raise Exception('cant find StringIO')";

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, PYTHON, "", "",
				GAV_DEFAULT, userPythonScript, true, DEPENDENCIES_DEFAULT, seqSteps, null, null);
	}

	@Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
	public void doActionPythonInputTypeMismatchTest() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		//missing inputs
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("port", ValueFactory.create(8080)); //should be string
		runEnv.putCallArguments(initialCallArguments);

		//expects port as string
		String userPythonScript = "print('localhost:' + port)";

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, PYTHON, "", "",
				GAV_DEFAULT, userPythonScript, true, DEPENDENCIES_DEFAULT, seqSteps, null, null);
	}

	@Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
	public void doActionPythonEmptyScript() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("host", ValueFactory.create("localhost"));
		initialCallArguments.put("port", ValueFactory.create("8080"));
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, PYTHON, "", "",
				GAV_DEFAULT, "", true, DEPENDENCIES_DEFAULT, seqSteps, null, null);
	}

	@Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
	public void doActionPythonMissingScript() {
		//prepare doAction arguments
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("host", ValueFactory.create("localhost"));
		initialCallArguments.put("port", ValueFactory.create("8080"));
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, PYTHON, "", "",
				GAV_DEFAULT, null, true, DEPENDENCIES_DEFAULT, seqSteps, null, null);
	}

	@Test
	public void testPythonOutputSerializationErrorMessage() {
		RunEnvironment runEnv = new RunEnvironment();
		runEnv.putCallArguments(new HashMap<String, Value>());
		final String userPythonScript = "from datetime import datetime\n" + NON_SERIALIZABLE_VARIABLE_NAME
				+ " = datetime.utcnow()";

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L,
						PYTHON, "", "", GAV_DEFAULT, userPythonScript, true, DEPENDENCIES_DEFAULT, seqSteps, null,
						null));
		Assert.assertTrue(exception.getMessage().contains(NON_SERIALIZABLE_VARIABLE_NAME));
	}

	@Test
	public void doActionSeqType() {
		RunEnvironment runEnv = new RunEnvironment();
		Map<String, Value> initialCallArguments = new HashMap<>();
		initialCallArguments.put("name", ValueFactory.create("nameTest"));
		initialCallArguments.put("role", ValueFactory.create("roleTest"));
		runEnv.putCallArguments(initialCallArguments);

		//invoke doAction
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, SEQUENTIAL,
				ContentTestActions.class.getName(), "doSeqSampleAction", GAV_DEFAULT, null, true, DEPENDENCIES_DEFAULT,
				seqSteps, null, null);

		verify(seqExecutionService).execute(eq(GAV_DEFAULT),
				any(CloudSlangSequentialExecutionParametersProviderImpl.class), eq(null));
	}

	@Test
	public void doActionExternalTrue() {
		RunEnvironment runEnv = new RunEnvironment();
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, SEQUENTIAL,
				ContentTestActions.class.getName(), "doSeqSampleAction", GAV_DEFAULT, null, true, DEPENDENCIES_DEFAULT,
				seqSteps, true, null);

		ArgumentCaptor<CloudSlangSequentialExecutionParametersProviderImpl> mockedProviderCaptor = ArgumentCaptor
				.forClass(CloudSlangSequentialExecutionParametersProviderImpl.class);

		verify(seqExecutionService).execute(any(String.class), mockedProviderCaptor.capture(), any(Serializable.class));

		CloudSlangSequentialExecutionParametersProviderImpl mockedProvider = mockedProviderCaptor.getValue();
		assertTrue(mockedProvider.getExternal());
	}

	@Test
	public void doActionExternalFalse() {
		RunEnvironment runEnv = new RunEnvironment();
		actionSteps.doAction(executionRuntimeServicesMock, runEnv, nonSerializableExecutionData, 2L, SEQUENTIAL,
				ContentTestActions.class.getName(), "doSeqSampleAction", GAV_DEFAULT, null, true, DEPENDENCIES_DEFAULT,
				seqSteps, null, null);

		ArgumentCaptor<CloudSlangSequentialExecutionParametersProviderImpl> mockedProviderCaptor = ArgumentCaptor
				.forClass(CloudSlangSequentialExecutionParametersProviderImpl.class);

		verify(seqExecutionService).execute(any(String.class), mockedProviderCaptor.capture(), any(Serializable.class));

		CloudSlangSequentialExecutionParametersProviderImpl mockedProvider = mockedProviderCaptor.getValue();
		assertFalse(mockedProvider.getExternal());
	}

	@Configuration
	static class Config {

		@Bean
		public ActionExecutionData actionSteps() {
			return new ActionExecutionData();
		}

		@Bean
		public ScriptExecutor scriptExecutor() {
			return new ScriptExecutor();
		}

		@Bean
		public ScriptsService scriptsService() {
			return new ScriptsService();
		}

		@Bean(name = "jythonRuntimeService")
		public PythonRuntimeService pythonRuntimeService() {
			return new PythonRuntimeServiceImpl();
		}

		@Bean(name = "jythonExecutionEngine")
		public PythonExecutionEngine pythonExecutionEngine() {
			return new PythonExecutionCachedEngine();
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
		public DependencyService mavenRepositoryService() {
			return new DependencyServiceImpl();
		}

		@Bean
		public MavenConfig mavenConfig() {
			return new MavenConfigImpl();
		}

		@Bean
		public JavaExecutionCachedEngine javaExecutionEngine() {
			return new JavaExecutionCachedEngine();
		}

		@Bean
		public JavaRuntimeService javaExecutionService() {
			return new JavaRuntimeServiceImpl();
		}

		@Bean
		public DummyEncryptor dummyEncryptor() {
			return new DummyEncryptor();
		}

		@Bean
		public EventBus eventBus() {
			return new EventBusImpl();
		}

		@Bean
		public SequentialExecutionService seqExecutionService() {
			return mock(SequentialExecutionService.class);
		}

	}
}
