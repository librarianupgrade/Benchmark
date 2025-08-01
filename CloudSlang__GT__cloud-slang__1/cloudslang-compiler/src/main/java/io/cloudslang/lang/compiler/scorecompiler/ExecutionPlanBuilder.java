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

import io.cloudslang.lang.compiler.modeller.model.Decision;
import io.cloudslang.lang.compiler.modeller.model.ExternalStep;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.NavigationOptions;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectFirst;
import static org.hamcrest.Matchers.equalTo;
import static io.cloudslang.lang.compiler.utils.SlangSourceUtils.getNavigationStepName;
import static io.cloudslang.lang.entities.ScoreLangConstants.STEP_NAVIGATION_OPTIONS_KEY;

/*
 * Created by orius123 on 11/11/14.
 */
public class ExecutionPlanBuilder {

	private ExecutionStepFactory stepFactory;

	private ExternalExecutionStepFactory externalStepFactory;

	private static final String CLOUDSLANG_NAME = "CloudSlang";
	private static final int NUMBER_OF_STEP_EXECUTION_STEPS = 3; // setWorkerGroupStep + beginStep + endStep
	private static final int NUMBER_OF_PARALLEL_LOOP_EXECUTION_STEPS = 2; // beginStep + endStep
	private static final long FLOW_END_STEP_ID = 0L;
	private static final long FLOW_PRECONDITION_STEP_ID = 1L;
	private static final long FLOW_START_STEP_ID = 2L;

	public ExecutionPlan createOperationExecutionPlan(Operation compiledOp) {
		ExecutionPlan executionPlan = new ExecutionPlan();
		executionPlan.setName(compiledOp.getName());
		executionPlan.setLanguage(CLOUDSLANG_NAME);
		executionPlan.setFlowUuid(compiledOp.getId());

		executionPlan.setBeginStep(1L);

		executionPlan.addStep(stepFactory.createStartStep(1L, compiledOp.getPreExecActionData(), compiledOp.getInputs(),
				compiledOp.getName(), ExecutableType.OPERATION));
		executionPlan.addStep(stepFactory.createActionStep(2L, compiledOp.getAction().getActionData()));
		executionPlan.addStep(stepFactory.createEndStep(3L, compiledOp.getPostExecActionData(), compiledOp.getOutputs(),
				compiledOp.getResults(), compiledOp.getName(), ExecutableType.OPERATION));
		return executionPlan;
	}

	public ExecutionPlan createDecisionExecutionPlan(Decision compiledDecision) {
		ExecutionPlan executionPlan = new ExecutionPlan();
		executionPlan.setName(compiledDecision.getName());
		executionPlan.setLanguage(CLOUDSLANG_NAME);
		executionPlan.setFlowUuid(compiledDecision.getId());

		executionPlan.setBeginStep(1L);

		executionPlan.addStep(stepFactory.createStartStep(1L, compiledDecision.getPreExecActionData(),
				compiledDecision.getInputs(), compiledDecision.getName(), ExecutableType.DECISION));
		executionPlan.addStep(
				stepFactory.createEndStep(2L, compiledDecision.getPostExecActionData(), compiledDecision.getOutputs(),
						compiledDecision.getResults(), compiledDecision.getName(), ExecutableType.DECISION));
		return executionPlan;
	}

	public ExecutionPlan createFlowExecutionPlan(Flow compiledFlow) {
		ExecutionPlan executionPlan = new ExecutionPlan();
		executionPlan.setName(compiledFlow.getName());
		executionPlan.setLanguage(CLOUDSLANG_NAME);
		executionPlan.setFlowUuid(compiledFlow.getId());
		executionPlan.setWorkerGroup(compiledFlow.getWorkerGroup());

		executionPlan.setBeginStep(FLOW_PRECONDITION_STEP_ID);
		executionPlan.addStep(stepFactory.createPreconditionStep(FLOW_PRECONDITION_STEP_ID, compiledFlow.getName()));
		//flow start step
		executionPlan.addStep(stepFactory.createStartStep(FLOW_START_STEP_ID, compiledFlow.getPreExecActionData(),
				compiledFlow.getInputs(), compiledFlow.getName(), ExecutableType.FLOW));
		//flow end step
		executionPlan.addStep(stepFactory.createEndStep(FLOW_END_STEP_ID, compiledFlow.getPostExecActionData(),
				compiledFlow.getOutputs(), compiledFlow.getResults(), compiledFlow.getName(), ExecutableType.FLOW));

		Map<String, Long> stepReferences = getStepReferences(compiledFlow);

		Deque<Step> steps = compiledFlow.getWorkflow().getSteps();

		if (CollectionUtils.isEmpty(steps)) {
			throw new RuntimeException("Flow: " + compiledFlow.getName() + " has no steps");
		}

		List<ExecutionStep> stepExecutionSteps = buildStepExecutionSteps(steps.getFirst(), stepReferences, steps,
				compiledFlow);
		executionPlan.addSteps(stepExecutionSteps);

		return executionPlan;
	}

	private Map<String, Long> getStepReferences(Flow compiledFlow) {
		Map<String, Long> stepReferences = new HashMap<>();
		for (Result result : compiledFlow.getResults()) {
			stepReferences.put(result.getName(), FLOW_END_STEP_ID);
		}
		return stepReferences;
	}

	private List<ExecutionStep> buildStepExecutionSteps(Step step, Map<String, Long> stepReferences, Deque<Step> steps,
			Flow compiledFlow) {

		List<ExecutionStep> stepExecutionSteps = new ArrayList<>();

		String stepName = step.getName();
		long currentId = getCurrentId(stepReferences, steps);
		boolean parallelLoop = step.isParallelLoop();

		//Begin Step
		stepReferences.put(stepName, currentId);

		ExecutionStep workerStep = createWorkerGroupStep(currentId++, step,
				inheritWorkerGroupFromFlow(step, compiledFlow), step.getRobotGroup());
		stepExecutionSteps.add(workerStep);
		if (parallelLoop) {
			Long joinStepId = currentId + NUMBER_OF_PARALLEL_LOOP_EXECUTION_STEPS + 1;
			stepExecutionSteps.add(stepFactory.createAddBranchesStep(currentId++, joinStepId, currentId,
					step.getPreStepActionData(), compiledFlow.getId(), stepName));
		}
		ExecutionStep executionStep = createBeginStep(currentId++, step,
				inheritWorkerGroupFromFlow(step, compiledFlow));
		stepExecutionSteps.add(executionStep);

		//End Step
		Map<String, ResultNavigation> navigationValues = new HashMap<>();
		for (Map<String, Serializable> map : step.getNavigationStrings()) {
			Map.Entry<String, Serializable> entry = map.entrySet().iterator().next();
			String nextStepName = getNavigationStepName(entry.getValue());
			if (stepReferences.get(nextStepName) == null) {
				Step nextStepToCompile = selectFirst(steps, having(on(Step.class).getName(), equalTo(nextStepName)));
				stepExecutionSteps
						.addAll(buildStepExecutionSteps(nextStepToCompile, stepReferences, steps, compiledFlow));
			}
			long nextStepId = stepReferences.get(nextStepName);
			String presetResult = (FLOW_END_STEP_ID == nextStepId) ? nextStepName : null;
			String navigationKey = entry.getKey();
			if (!navigationValues.containsKey(navigationKey)) {
				navigationValues.put(navigationKey, new ResultNavigation(nextStepId, presetResult));
			}
			addStepNavigationOptions(executionStep, entry);
		}
		if (parallelLoop) {
			stepExecutionSteps.add(createFinishStepStep(currentId++, step, new HashMap<>(),
					inheritWorkerGroupFromFlow(step, compiledFlow), true));
			stepExecutionSteps.add(stepFactory.createJoinBranchesStep(currentId, step.getPostStepActionData(),
					navigationValues, stepName));
		} else {
			stepExecutionSteps.add(createFinishStepStep(currentId, step, navigationValues,
					inheritWorkerGroupFromFlow(step, compiledFlow), false));
		}
		return stepExecutionSteps;
	}

	private String inheritWorkerGroupFromFlow(Step step, Flow flow) {
		if (step.getWorkerGroup() != null) {
			return step.getWorkerGroup();
		} else if (flow.getWorkerGroup() != null) {
			return flow.getWorkerGroup();
		} else {
			return null;
		}
	}

	private long getCurrentId(Map<String, Long> stepReferences, Deque<Step> steps) {
		Long currentId;

		Map.Entry maxEntry = Collections.max(stepReferences.entrySet(), Map.Entry.comparingByValue());
		long max = (long) maxEntry.getValue();
		String referenceKey = (String) maxEntry.getKey();
		Step step = null;
		for (Step stepItem : steps) {
			if (stepItem.getName().equals(referenceKey)) {
				step = stepItem;
				break;
			}
		}

		if (step == null) {
			// the reference is not a step - usually this means this is the first begin step
			currentId = FLOW_START_STEP_ID + 1L;
		} else if (!step.isParallelLoop()) {
			// the reference is not a parallel loop step
			currentId = max + NUMBER_OF_STEP_EXECUTION_STEPS;
		} else {
			//async step
			currentId = max + NUMBER_OF_STEP_EXECUTION_STEPS + NUMBER_OF_PARALLEL_LOOP_EXECUTION_STEPS;
		}

		return currentId;
	}

	private ExecutionStep createFinishStepStep(long currentId, Step step,
			Map<String, ResultNavigation> navigationValues, String workerGroup, boolean parallelLoop) {
		if (step instanceof ExternalStep) {
			return externalStepFactory.createFinishExternalFlowStep(currentId, step.getPostStepActionData(),
					navigationValues, step.getName(), workerGroup, parallelLoop);
		}
		return stepFactory.createFinishStepStep(currentId, step.getPostStepActionData(), navigationValues,
				step.getName(), workerGroup, parallelLoop);
	}

	private ExecutionStep createBeginStep(Long id, Step step, String workerGroup) {
		if (step instanceof ExternalStep) {
			return externalStepFactory.createBeginExternalFlowStep(id, step.getArguments(), step.getPreStepActionData(),
					step.getRefId(), step.getName(), workerGroup);
		}
		return stepFactory.createBeginStepStep(id, step.getArguments(), step.getPreStepActionData(), step.getRefId(),
				step.getName(), workerGroup);
	}

	private ExecutionStep createWorkerGroupStep(Long id, Step step, String workerGroup, String robotGroup) {
		if (step instanceof ExternalStep) {
			return externalStepFactory.createWorkerGroupExternalFlowStep(id, step.getPreStepActionData(),
					step.getName(), workerGroup);
		}
		return stepFactory.createWorkerGroupStep(id, step.getPreStepActionData(), step.getName(), workerGroup,
				robotGroup);
	}

	public void setStepFactory(ExecutionStepFactory stepFactory) {
		this.stepFactory = stepFactory;
	}

	public void setExternalStepFactory(ExternalExecutionStepFactory externalStepFactory) {
		this.externalStepFactory = externalStepFactory;
	}

	private void addStepNavigationOptions(ExecutionStep executionStep, Map.Entry<String, Serializable> navigation) {
		if (navigation.getValue() instanceof Map) {
			Map<String, Serializable> navigationData = (Map<String, Serializable>) executionStep.getNavigationData();
			if (navigationData == null) {
				navigationData = new HashMap<>();
				executionStep.setNavigationData(navigationData);
			}
			List<NavigationOptions> stepNavigationOptions = (List<NavigationOptions>) navigationData
					.computeIfAbsent(STEP_NAVIGATION_OPTIONS_KEY, key -> new ArrayList<>());

			stepNavigationOptions.add(new NavigationOptions(navigation.getKey(), executionStep.getExecStepId(),
					(Map) navigation.getValue()));
		}
	}
}
