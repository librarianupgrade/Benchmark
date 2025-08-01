/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.scorecompiler;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Action;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.compiler.modeller.model.Workflow;
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionPlanBuilderTest {

	@InjectMocks
	private ExecutionPlanBuilder executionPlanBuilder;

	@Mock
	private ExecutionStepFactory stepFactory;

	private Set<String> systemPropertyDependencies = Collections.emptySet();

	private Step createSimpleCompiledParallelStep(String stepName) {
		return createSimpleCompiledStep(stepName, true);
	}

	private Step createSimpleCompiledStep(String stepName) {
		return createSimpleCompiledStep(stepName, false);
	}

	private Step createSimpleCompiledStep(String stepName, List<Map<String, Serializable>> navigationStrings) {
		return createSimpleCompiledStep(stepName, false, navigationStrings);
	}

	private Step createSimpleCompiledStep(String stepName, boolean isParallelLoop) {
		List<Map<String, Serializable>> navigationStrings = new ArrayList<>();
		Map<String, Serializable> successMap = new HashMap<>();
		successMap.put(ScoreLangConstants.SUCCESS_RESULT, ScoreLangConstants.SUCCESS_RESULT);
		Map<String, Serializable> failureMap = new HashMap<>();
		failureMap.put(ScoreLangConstants.FAILURE_RESULT, ScoreLangConstants.FAILURE_RESULT);
		navigationStrings.add(successMap);
		navigationStrings.add(failureMap);

		return createSimpleCompiledStep(stepName, isParallelLoop, navigationStrings);
	}

	private Step createSimpleCompiledStep(String stepName, boolean isParallelLoop,
			List<Map<String, Serializable>> navigationStrings) {
		Map<String, Serializable> preStepActionData = new HashMap<>();

		if (isParallelLoop) {
			preStepActionData.put(SlangTextualKeys.PARALLEL_LOOP_KEY, "value in values");
		}

		Map<String, Serializable> postStepActionData = new HashMap<>();
		String refId = "refId";
		return new Step(stepName, preStepActionData, postStepActionData, null, navigationStrings, refId, null, null,
				isParallelLoop, false);
	}

	private List<Result> defaultFlowResults() {
		List<Result> results = new ArrayList<>();
		results.add(new Result(ScoreLangConstants.SUCCESS_RESULT, null));
		results.add(new Result(ScoreLangConstants.FAILURE_RESULT, null));
		return results;
	}

	private void mockStartStep(Executable executable) {
		Map<String, Serializable> preExecActionData = executable.getPreExecActionData();
		String execName = executable.getName();
		List<Input> inputs = executable.getInputs();
		when(stepFactory.createStartStep(eq(2L), same(preExecActionData), same(inputs), same(execName),
				eq(ExecutableType.FLOW))).thenReturn(new ExecutionStep(2L));
		when(stepFactory.createStartStep(eq(1L), same(preExecActionData), same(inputs), same(execName),
				eq(ExecutableType.OPERATION))).thenReturn(new ExecutionStep(1L));
	}

	private void mockPreconditionStep(Executable executable) {
		String execName = executable.getName();
		when(stepFactory.createPreconditionStep(eq(1L), same(execName))).thenReturn(new ExecutionStep(1L));
	}

	private void mockEndStep(Long stepId, Executable executable, ExecutableType executableType) {
		Map<String, Serializable> postExecActionData = executable.getPostExecActionData();
		List<Output> outputs = executable.getOutputs();
		List<Result> results = executable.getResults();
		String execName = executable.getName();
		when(stepFactory.createEndStep(eq(stepId), same(postExecActionData), same(outputs), same(results),
				same(execName), same(executableType))).thenReturn(new ExecutionStep(stepId));
	}

	private void mockFinishStep(Long stepId, Step step) {
		mockFinishStep(stepId, step, false);
	}

	private void mockFinishStep(Long stepId, Step step, boolean isParallelLoop) {
		Map<String, Serializable> postStepActionData = step.getPostStepActionData();
		String stepName = step.getName();
		String group = step.getWorkerGroup();
		when(stepFactory.createFinishStepStep(eq(stepId), eq(postStepActionData),
				anyMapOf(String.class, ResultNavigation.class), eq(stepName), eq(group), eq(isParallelLoop)))
				.thenReturn(new ExecutionStep(stepId));
	}

	private void mockFinishParallelLoopStep(Long stepId, Step step) {
		mockFinishStep(stepId, step, true);
	}

	private void mockBeginStep(Long stepId, Step step) {
		Map<String, Serializable> preStepActionData = step.getPreStepActionData();
		String refId = step.getRefId();
		String name = step.getName();
		String group = step.getWorkerGroup();
		when(stepFactory.createBeginStepStep(eq(stepId), anyListOf(Argument.class), eq(preStepActionData), eq(refId),
				eq(name), eq(group))).thenReturn(new ExecutionStep(stepId));
	}

	private void mockWorkerStep(Long stepId, Step step) {
		Map<String, Serializable> preStepActionData = step.getPreStepActionData();
		String name = step.getName();
		String group = step.getWorkerGroup();
		String robotGroup = step.getRobotGroup();
		when(stepFactory.createWorkerGroupStep(eq(stepId), eq(preStepActionData), eq(name), eq(group), eq(robotGroup)))
				.thenReturn(new ExecutionStep(stepId));
	}

	private void mockAddBranchesStep(Long stepId, Long nextStepId, Long branchBeginStepId, Step step, Flow flow) {
		Map<String, Serializable> preStepActionData = step.getPreStepActionData();
		String refId = flow.getId();
		String name = step.getName();
		when(stepFactory.createAddBranchesStep(eq(stepId), eq(nextStepId), eq(branchBeginStepId), eq(preStepActionData),
				eq(refId), eq(name))).thenReturn(new ExecutionStep(stepId));
	}

	private void mockJoinBranchesStep(Long stepId, Step step) {
		Map<String, Serializable> postStepActionData = step.getPostStepActionData();
		String stepName = step.getName();
		when(stepFactory.createJoinBranchesStep(eq(stepId), eq(postStepActionData),
				anyMapOf(String.class, ResultNavigation.class), eq(stepName))).thenReturn(new ExecutionStep(stepId));
	}

	@Test
	public void testCreateOperationExecutionPlan() throws Exception {
		Map<String, Serializable> preOpActionData = new HashMap<>();
		Map<String, Serializable> postOpActionData = new HashMap<>();
		Map<String, Serializable> actionData = new HashMap<>();
		Action action = new Action(actionData);
		String operationName = "operationName";
		String opNamespace = "user.flows";
		List<Input> inputs = new ArrayList<>();
		List<Output> outputs = new ArrayList<>();
		List<Result> results = new ArrayList<>();

		Operation compiledOperation = new Operation(preOpActionData, postOpActionData, action, opNamespace,
				operationName, inputs, outputs, results, null, systemPropertyDependencies);

		mockStartStep(compiledOperation);
		when(stepFactory.createActionStep(eq(2L), same(actionData))).thenReturn(new ExecutionStep(2L));
		mockEndStep(3L, compiledOperation, ExecutableType.OPERATION);

		ExecutionPlan executionPlan = executionPlanBuilder.createOperationExecutionPlan(compiledOperation);

		assertEquals("different number of execution steps than expected", 3, executionPlan.getSteps().size());
		assertEquals("operation name is different than expected", operationName, executionPlan.getName());
		assertEquals("language name is different than expected", "CloudSlang", executionPlan.getLanguage());
		assertEquals("begin step is different than expected", Long.valueOf(1), executionPlan.getBeginStep());
	}

	@Test
	public void createSimpleFlow() throws Exception {
		Map<String, Serializable> preFlowActionData = new HashMap<>();
		Map<String, Serializable> postFlowActionData = new HashMap<>();
		Deque<Step> steps = new LinkedList<>();
		Step step = createSimpleCompiledStep("stepName");
		steps.add(step);
		Workflow workflow = new Workflow(steps);
		String flowName = "flowName";
		String flowNamespace = "user.flows";
		List<Input> inputs = new ArrayList<>();
		List<Output> outputs = new ArrayList<>();
		List<Result> results = defaultFlowResults();

		Flow compiledFlow = new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, null,
				inputs, outputs, results, null, systemPropertyDependencies);

		mockStartStep(compiledFlow);
		mockPreconditionStep(compiledFlow);
		mockEndStep(0L, compiledFlow, ExecutableType.FLOW);
		mockWorkerStep(3L, step);
		mockBeginStep(4L, step);
		mockFinishStep(5L, step);
		ExecutionPlan executionPlan = executionPlanBuilder.createFlowExecutionPlan(compiledFlow);

		assertEquals("different number of execution steps than expected", 6, executionPlan.getSteps().size());
		assertEquals("flow name is different than expected", flowName, executionPlan.getName());
		assertEquals("language name is different than expected", "CloudSlang", executionPlan.getLanguage());
		assertEquals("begin step is different than expected", Long.valueOf(1), executionPlan.getBeginStep());
	}

	@Test
	public void createSimpleFlowWithParallelLoop() throws Exception {
		Map<String, Serializable> preFlowActionData = new HashMap<>();
		Map<String, Serializable> postFlowActionData = new HashMap<>();
		Deque<Step> steps = new LinkedList<>();
		Step step = createSimpleCompiledParallelStep("stepName");
		steps.add(step);
		Workflow workflow = new Workflow(steps);
		String flowName = "flowName";
		String flowNamespace = "user.flows";
		List<Input> inputs = new ArrayList<>();
		List<Output> outputs = new ArrayList<>();
		List<Result> results = defaultFlowResults();

		Flow compiledFlow = new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, null,
				inputs, outputs, results, null, systemPropertyDependencies);

		mockPreconditionStep(compiledFlow);
		mockStartStep(compiledFlow);
		mockEndStep(0L, compiledFlow, ExecutableType.FLOW);
		mockWorkerStep(3L, step);
		mockAddBranchesStep(4L, 7L, 5L, step, compiledFlow);
		mockBeginStep(5L, step);
		mockFinishParallelLoopStep(6L, step);
		mockJoinBranchesStep(7L, step);
		final ExecutionPlan executionPlan = executionPlanBuilder.createFlowExecutionPlan(compiledFlow);

		verify(stepFactory).createAddBranchesStep(eq(4L), eq(7L), eq(5L), eq(step.getPreStepActionData()),
				eq(compiledFlow.getId()), eq(step.getName()));
		verify(stepFactory).createBeginStepStep(eq(5L), anyListOf(Argument.class), eq(step.getPreStepActionData()),
				eq(step.getRefId()), eq(step.getName()), eq(step.getWorkerGroup()));
		verify(stepFactory).createFinishStepStep(eq(6L), eq(step.getPostStepActionData()),
				anyMapOf(String.class, ResultNavigation.class), eq(step.getName()), eq(step.getWorkerGroup()),
				eq(step.isParallelLoop()));
		verify(stepFactory).createJoinBranchesStep(eq(7L), eq(step.getPostStepActionData()),
				anyMapOf(String.class, ResultNavigation.class), eq(step.getName()));

		assertEquals("different number of execution steps than expected", 8, executionPlan.getSteps().size());
		assertEquals("flow name is different than expected", flowName, executionPlan.getName());
		assertEquals("language name is different than expected", "CloudSlang", executionPlan.getLanguage());
		assertEquals("begin step is different than expected", Long.valueOf(1), executionPlan.getBeginStep());
	}

	@Test
	public void createFlowWithTwoSteps() throws Exception {
		final Deque<Step> steps = new LinkedList<>();
		String secondStepName = "2ndStep";
		List<Map<String, Serializable>> navigationStrings = new ArrayList<>();
		Map<String, Serializable> successMap = new HashMap<>();
		successMap.put(ScoreLangConstants.SUCCESS_RESULT, secondStepName);
		Map<String, Serializable> failureMap = new HashMap<>();
		failureMap.put(ScoreLangConstants.FAILURE_RESULT, ScoreLangConstants.FAILURE_RESULT);
		navigationStrings.add(successMap);
		navigationStrings.add(failureMap);
		Step firstStep = createSimpleCompiledStep("firstStepName", navigationStrings);
		Step secondStep = createSimpleCompiledStep(secondStepName);
		steps.add(firstStep);
		steps.add(secondStep);
		Map<String, Serializable> preFlowActionData = new HashMap<>();
		Map<String, Serializable> postFlowActionData = new HashMap<>();

		Workflow workflow = new Workflow(steps);
		String flowName = "flowName";
		String flowNamespace = "user.flows";
		List<Input> inputs = new ArrayList<>();
		List<Output> outputs = new ArrayList<>();
		List<Result> results = defaultFlowResults();

		Flow compiledFlow = new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, null,
				inputs, outputs, results, null, systemPropertyDependencies);

		mockPreconditionStep(compiledFlow);
		mockStartStep(compiledFlow);
		mockEndStep(0L, compiledFlow, ExecutableType.FLOW);

		mockWorkerStep(3L, firstStep);
		mockBeginStep(4L, firstStep);
		mockFinishStep(5L, firstStep);
		mockWorkerStep(6L, secondStep);
		mockBeginStep(7L, secondStep);
		mockFinishStep(8L, secondStep);
		ExecutionPlan executionPlan = executionPlanBuilder.createFlowExecutionPlan(compiledFlow);

		assertEquals("different number of execution steps than expected", 9, executionPlan.getSteps().size());
		assertEquals("flow name is different than expected", flowName, executionPlan.getName());
		assertEquals("language name is different than expected", "CloudSlang", executionPlan.getLanguage());
		assertEquals("begin step is different than expected", Long.valueOf(1), executionPlan.getBeginStep());
	}

	@Test
	public void createFlowWithNoStepsShouldThrowException() throws Exception {
		Map<String, Serializable> preFlowActionData = new HashMap<>();
		Map<String, Serializable> postFlowActionData = new HashMap<>();
		Deque<Step> steps = new LinkedList<>();
		Workflow workflow = new Workflow(steps);
		String flowName = "flowName";
		String flowNamespace = "user.flows";
		List<Input> inputs = new ArrayList<>();
		List<Output> outputs = new ArrayList<>();
		List<Result> results = new ArrayList<>();

		Flow compiledFlow = new Flow(preFlowActionData, postFlowActionData, workflow, flowNamespace, flowName, null,
				inputs, outputs, results, null, systemPropertyDependencies);

		mockPreconditionStep(compiledFlow);
		mockStartStep(compiledFlow);
		mockEndStep(0L, compiledFlow, ExecutableType.FLOW);

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> executionPlanBuilder.createFlowExecutionPlan(compiledFlow));
		assertEquals("Flow: flowName has no steps", exception.getMessage());
	}
}