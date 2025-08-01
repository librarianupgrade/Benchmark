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

import com.hp.oo.sdk.content.annotations.Param;
import io.cloudslang.lang.entities.ListParallelLoopStatement;
import io.cloudslang.lang.entities.MapParallelLoopStatement;
import io.cloudslang.lang.entities.ParallelLoopStatement;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.RuntimeConstants;
import io.cloudslang.lang.runtime.bindings.OutputsBinding;
import io.cloudslang.lang.runtime.bindings.ParallelLoopBinding;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.api.EndBranchDataContainer;
import io.cloudslang.score.api.StatefulSessionStack;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.score.lang.SystemContext;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cloudslang.lang.entities.ScoreLangConstants.CURRENT_STEP_ID_KEY;
import static io.cloudslang.lang.entities.ScoreLangConstants.RUN_ENV;
import static io.cloudslang.lang.runtime.RuntimeConstants.BRANCHES_CONTEXT_KEY;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.DEFAULT_ROI_VALUE;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_TOTAL_ROI;
import static java.lang.Integer.parseInt;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
@Component
public class ParallelLoopExecutionData extends AbstractExecutionData {

	@Autowired
	private ParallelLoopBinding parallelLoopBinding;

	@Autowired
	private OutputsBinding outputsBinding;

	private static final Logger logger = LogManager.getLogger(ParallelLoopExecutionData.class);

	public void addBranches(
			@Param(ScoreLangConstants.PARALLEL_LOOP_STATEMENT_KEY) ParallelLoopStatement parallelLoopStatement,
			@Param(RUN_ENV) RunEnvironment runEnv,
			@Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
			@Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName,

			//CHECKSTYLE:OFF: checkstyle:parametername
			@Param(ExecutionParametersConsts.RUNNING_EXECUTION_PLAN_ID) Long RUNNING_EXECUTION_PLAN_ID,
			//CHECKSTYLE:ON

			@Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
			@Param(ScoreLangConstants.BRANCH_BEGIN_STEP_ID_KEY) Long branchBeginStep,
			@Param(ScoreLangConstants.REF_ID) String refId) {

		try {
			Context flowContext = runEnv.getStack().popContext();
			int parallelismLevel = executionRuntimeServices.getLevelParallelism() != null
					? (int) executionRuntimeServices.getLevelParallelism()
					: 0;
			executionRuntimeServices.setLevelParallelism(parallelismLevel + 1);
			List<Value> splitData = handleFirstIteration(parallelLoopStatement, runEnv, executionRuntimeServices,
					nodeName, flowContext);

			runEnv.putNextStepPosition(nextStepId);

			final Integer throttleSize = executionRuntimeServices.getThrottleSize();
			final int splitSize = splitData.size();
			final int lanesToStart = calculateNumberOfLanesToStart(splitSize, throttleSize);
			final List<Value> splitDataCurrentBulk = splitData.subList(0, lanesToStart);
			final List<Value> splitDataLeftoversSublist = splitData.subList(lanesToStart, splitSize);

			if (isNotEmpty(splitDataLeftoversSublist)) {
				executionRuntimeServices.setSplitData(new ArrayList<>(splitDataLeftoversSublist));
			} else {
				executionRuntimeServices.removeSplitData();
			}

			for (Value splitItem : splitDataCurrentBulk) {
				Context branchContext = (Context) SerializationUtils.clone(flowContext);

				// first fire event
				fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_BRANCH_START,
						"parallel loop branch created", runEnv.getExecutionPath().getCurrentPath(),
						LanguageEventData.StepType.STEP, nodeName, branchContext.getImmutableViewOfVariables(),
						Pair.of(ScoreLangConstants.REF_ID, refId), Pair.of(RuntimeConstants.SPLIT_ITEM_KEY, splitItem));
				// take path down one level
				runEnv.getExecutionPath().down();

				RunEnvironment branchRuntimeEnvironment = (RunEnvironment) SerializationUtils.clone(runEnv);
				branchRuntimeEnvironment.resetStacks();

				StatefulSessionStack branchStack = branchRuntimeEnvironment.getStatefulSessionsStack();
				branchStack.pushSessionsMap(new HashMap<>());

				if (parallelLoopStatement instanceof ListParallelLoopStatement) {
					branchContext.putVariable(((ListParallelLoopStatement) parallelLoopStatement).getVarName(),
							splitItem);
				} else if (parallelLoopStatement instanceof MapParallelLoopStatement) {
					MapParallelLoopStatement mapLoopStatement = (MapParallelLoopStatement) parallelLoopStatement;
					//noinspection unchecked
					ImmutablePair<Value, Value> pair = (ImmutablePair<Value, Value>) splitItem.get();
					branchContext.putVariable(mapLoopStatement.getKeyName(), pair.getLeft());
					branchContext.putVariable(mapLoopStatement.getValueName(), pair.getRight());
				}
				updateCallArgumentsAndPushContextToStack(branchRuntimeEnvironment, branchContext, new HashMap<>(),
						new HashMap<>());

				createBranch(branchRuntimeEnvironment, executionRuntimeServices, refId, branchBeginStep);

				// take path up level
				runEnv.getExecutionPath().up();

				// forward for next branch
				runEnv.getExecutionPath().forward();
			}

			updateCallArgumentsAndPushContextToStack(runEnv, flowContext, new HashMap<>(), new HashMap<>());
		} catch (RuntimeException e) {
			logger.error("There was an error running the add branches execution step of: \'" + nodeName
					+ "\'. Error is: " + e.getMessage());
			throw new RuntimeException("Error running: " + nodeName + ": " + e.getMessage(), e);
		}

	}

	public void joinBranches(@Param(RUN_ENV) RunEnvironment runEnv,
			@Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
			@Param(ScoreLangConstants.STEP_PUBLISH_KEY) List<Output> stepPublishValues,
			@Param(ScoreLangConstants.STEP_NAVIGATION_KEY) Map<String, ResultNavigation> stepNavigationValues,
			@Param(ScoreLangConstants.NODE_NAME_KEY) String nodeName, @Param(CURRENT_STEP_ID_KEY) Long currentStepId) {
		try {
			notNull(executionRuntimeServices.getLevelParallelism(), "Parallelism level can not be null");
			if ((int) executionRuntimeServices.getLevelParallelism() > 0) {
				executionRuntimeServices.setLevelParallelism((int) executionRuntimeServices.getLevelParallelism() - 1);
			}
			ArrayList<Map<String, Serializable>> temporaryBranchesContext = executionRuntimeServices
					.getParallelTemporaryContext();
			collectBranchesData(executionRuntimeServices, nodeName, temporaryBranchesContext);

			if (isLastIteration(executionRuntimeServices.getRemainingBranches())) {
				// bind step results, outputs and handle navigation
				handleLastIteration(runEnv, executionRuntimeServices, stepPublishValues, stepNavigationValues, nodeName,
						temporaryBranchesContext);
			} else {
				// fail only in the last iteration
				executionRuntimeServices.removeStepErrorKey();
				runEnv.putNextStepPosition(currentStepId);
			}

			runEnv.getExecutionPath().forward();
		} catch (RuntimeException e) {
			logger.error("There was an error running the joinBranches execution step of: \'" + nodeName
					+ "\'. Error is: " + e.getMessage());
			throw new RuntimeException("Error running: \'" + nodeName + "\': \n" + e.getMessage(), e);
		}
	}

	private List<Value> handleFirstIteration(ParallelLoopStatement parallelLoopStatement, RunEnvironment runEnv,
			ExecutionRuntimeServices executionRuntimeServices, String nodeName, Context flowContext) {

		List<Value> splitData = (ArrayList<Value>) executionRuntimeServices.getSplitData();

		// split data is not set for first iteration
		if (isEmpty(splitData)) {
			splitData = parallelLoopBinding.bindParallelLoopList(parallelLoopStatement, flowContext,
					runEnv.getSystemProperties(), nodeName);
			executionRuntimeServices.setSplitDataSize(splitData.size());
			executionRuntimeServices.setParallelTemporaryContext(Lists.newArrayList());

			int throttleSize = parallelLoopBinding.bindParallelLoopThrottle(parallelLoopStatement, flowContext,
					runEnv.getSystemProperties(), nodeName);
			executionRuntimeServices.setThrottleSize(throttleSize);

			fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_SPLIT_BRANCHES,
					"parallel loop expression bound", runEnv.getExecutionPath().getCurrentPath(),
					LanguageEventData.StepType.STEP, nodeName, flowContext.getImmutableViewOfVariables(),
					Pair.of(LanguageEventData.BOUND_PARALLEL_LOOP_EXPRESSION, (Serializable) splitData));
			runEnv.getExecutionPath().down();
		}

		return splitData;
	}

	private void handleLastIteration(RunEnvironment runEnv, ExecutionRuntimeServices executionRuntimeServices,
			List<Output> stepPublishValues, Map<String, ResultNavigation> stepNavigationValues, String nodeName,
			ArrayList<Map<String, Serializable>> temporaryBranchesContext) {

		clearExecutionRuntimeForNextStep(executionRuntimeServices);
		runEnv.getExecutionPath().up();

		if (isTrue(executionRuntimeServices.removeBranchErrorKey())) {
			throw new RuntimeException("Exception occurred during lane execution");
		}

		Context flowContext = runEnv.getStack().popContext();
		Map<String, Value> globalContext = flowContext.getImmutableViewOfMagicVariables();
		Map<String, Value> outputBindingContext = new HashMap<>();
		outputBindingContext.put(BRANCHES_CONTEXT_KEY, ValueFactory.create(temporaryBranchesContext));
		Map<String, Value> publishValues = bindPublishValues(runEnv, executionRuntimeServices, stepPublishValues,
				stepNavigationValues, nodeName, outputBindingContext, globalContext);

		flowContext.putVariables(publishValues);

		String parallelLoopResult = getParallelLoopResult(temporaryBranchesContext);
		handleNavigationAndReturnValues(runEnv, executionRuntimeServices, stepNavigationValues, nodeName, publishValues,
				parallelLoopResult);

		runEnv.getStack().pushContext(flowContext);
	}

	private void handleNavigationAndReturnValues(RunEnvironment runEnv,
			ExecutionRuntimeServices executionRuntimeServices, Map<String, ResultNavigation> stepNavigationValues,
			String nodeName, Map<String, Value> publishValues, String parallelLoopResult) {
		// set the position of the next step - for the use of the navigation
		// find in the navigation values the correct next step position, according to the parallel loop result,
		// and set it
		ResultNavigation navigation = stepNavigationValues.get(parallelLoopResult);
		if (navigation == null) {
			// should always have the executable response mapped to a navigation by the step, if not, it is an error
			throw new RuntimeException("Step: " + nodeName
					+ " has no matching navigation for the parallel loop result: " + parallelLoopResult);
		}
		Long nextStepPosition = navigation.getNextStepId();
		String presetResult = navigation.getPresetResult();

		HashMap<String, Value> outputs = new HashMap<>(publishValues);
		ReturnValues returnValues = new ReturnValues(outputs, presetResult != null ? presetResult : parallelLoopResult);

		fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_JOIN_BRANCHES_END,
				"Parallel loop output binding finished", LanguageEventData.StepType.STEP, nodeName,
				new HashMap<String, Value>(), Pair.of(LanguageEventData.OUTPUTS, (Serializable) publishValues),
				Pair.of(LanguageEventData.RESULT, returnValues.getResult()),
				Pair.of(LanguageEventData.NEXT_STEP_POSITION, nextStepPosition));

		runEnv.putReturnValues(returnValues);
		runEnv.putNextStepPosition(nextStepPosition);
	}

	private String getParallelLoopResult(List<Map<String, Serializable>> branchesContext) {
		// if one of the branches failed then return with FAILURE, otherwise return with SUCCESS
		String parallelLoopResult = ScoreLangConstants.SUCCESS_RESULT;
		for (Map<String, Serializable> branchContext : branchesContext) {
			String branchResult = (String) branchContext.get(ScoreLangConstants.BRANCH_RESULT_KEY);
			if (branchResult.equals(ScoreLangConstants.FAILURE_RESULT)) {
				parallelLoopResult = ScoreLangConstants.FAILURE_RESULT;
				break;
			}
		}
		return parallelLoopResult;
	}

	private Map<String, Value> bindPublishValues(RunEnvironment runEnv,
			ExecutionRuntimeServices executionRuntimeServices, List<Output> stepPublishValues,
			Map<String, ResultNavigation> stepNavigationValues, String nodeName, Map<String, Value> publishContext,
			Map<String, Value> globalContext) {

		fireEvent(executionRuntimeServices, runEnv, ScoreLangConstants.EVENT_JOIN_BRANCHES_START,
				"Parallel loop output binding started", LanguageEventData.StepType.STEP, nodeName,
				new HashMap<String, Value>(),
				Pair.of(ScoreLangConstants.STEP_PUBLISH_KEY, (Serializable) stepPublishValues),
				Pair.of(ScoreLangConstants.STEP_NAVIGATION_KEY, (Serializable) stepNavigationValues));

		ReadOnlyContextAccessor outputsBindingAccessor = new ReadOnlyContextAccessor(publishContext, globalContext);
		return outputsBinding.bindOutputs(outputsBindingAccessor, runEnv.getSystemProperties(), stepPublishValues);
	}

	private void collectBranchesData(ExecutionRuntimeServices executionRuntimeServices, String nodeName,
			List<Map<String, Serializable>> branchesContext) {

		List<EndBranchDataContainer> branches = executionRuntimeServices.getFinishedChildBranchesData();
		Double roiBeforeParallelLoop = executionRuntimeServices.getRoiValue();
		for (EndBranchDataContainer branch : branches) {
			boolean isBranchException = checkExceptionInBranch(branch);

			if (!isBranchException) {
				Map<String, Serializable> branchContext = branch.getContexts();
				RunEnvironment branchRuntimeEnvironment = (RunEnvironment) branchContext.get(RUN_ENV);
				Map<String, Value> initialBranchContext = branchRuntimeEnvironment.getStack().popContext()
						.getImmutableViewOfVariables();
				Map<String, Serializable> branchContextMap = convert(initialBranchContext);
				ReturnValues executableReturnValues = branchRuntimeEnvironment.removeReturnValues();
				String branchResult = executableReturnValues.getResult();
				branchContextMap.put(ScoreLangConstants.BRANCH_RESULT_KEY, branchResult);
				branchesContext.add(branchContextMap);

				// up branch path
				branchRuntimeEnvironment.getExecutionPath().up();

				// The ROI value for each branch does already contain any previous ROI value, so we need to subtract it
				Double branchRoi = (Double) branch.getSystemContext().getOrDefault(EXECUTION_TOTAL_ROI,
						DEFAULT_ROI_VALUE) - roiBeforeParallelLoop;
				executionRuntimeServices.addRoiValue(branchRoi);

				fireEvent(executionRuntimeServices, branchRuntimeEnvironment, ScoreLangConstants.EVENT_BRANCH_END,
						"Parallel loop branch ended", LanguageEventData.StepType.STEP, nodeName, initialBranchContext,
						Pair.of(RuntimeConstants.BRANCH_RETURN_VALUES_KEY, executableReturnValues));
			} else {
				executionRuntimeServices.setBranchErrorKey();
			}
		}
	}

	private boolean checkExceptionInBranch(EndBranchDataContainer branch) {
		//first we check that no exception was thrown during the execution of the branch
		String branchException = branch.getException();
		if (StringUtils.isNotEmpty(branchException)) {
			Map<String, Serializable> systemContextMap = branch.getSystemContext();
			String branchId = null;
			if (MapUtils.isNotEmpty(systemContextMap)) {
				ExecutionRuntimeServices branchExecutionRuntimeServices = new SystemContext(systemContextMap);
				branchId = branchExecutionRuntimeServices.getBranchId();
			}
			logger.error("There was an error running branch: " + branchId + " Error is: " + branchException);
			return true;
		}
		return false;
	}

	private void createBranch(RunEnvironment runEnv, ExecutionRuntimeServices executionRuntimeServices, String refId,
			Long branchBeginStep) {
		Map<String, Serializable> branchContext = new HashMap<>();
		branchContext.put(RUN_ENV, runEnv);
		executionRuntimeServices.addBranchForParallelLoop(branchBeginStep, refId, branchContext);
	}

	private Map<String, Serializable> convert(Map<String, Value> map) {
		Map<String, Serializable> result = new HashMap<>(map.size());
		for (Map.Entry<String, Value> entry : map.entrySet()) {
			result.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().get());
		}
		return result;
	}

	/**
	 * Returns the number of lanes to start executing from the system context, depending on the throttle size value.
	 */
	private int calculateNumberOfLanesToStart(int splitSize, Integer throttleSize) {
		return throttleSize == null ? splitSize
				: (splitSize % throttleSize == 0) ? throttleSize : (splitSize % throttleSize);
	}

	private boolean isLastIteration(String remainingBranches) {
		return isNotBlank(remainingBranches) && parseInt(remainingBranches) == 0;
	}

	private void clearExecutionRuntimeForNextStep(ExecutionRuntimeServices executionRuntimeServices) {
		executionRuntimeServices.removeRemainingBranches();
		executionRuntimeServices.removeParallelTemporaryContext();
		executionRuntimeServices.removeSplitData();
		executionRuntimeServices.removeSplitDataSize();
		executionRuntimeServices.removeThrottleSize();
	}
}
