/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Action;
import io.cloudslang.lang.compiler.modeller.model.Decision;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.ExternalStep;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.SeqStep;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.compiler.modeller.model.Workflow;
import io.cloudslang.lang.compiler.modeller.result.ActionModellingResult;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.result.StepModellingResult;
import io.cloudslang.lang.compiler.modeller.result.WorkflowModellingResult;
import io.cloudslang.lang.compiler.modeller.transformers.ResultsTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SensitivityLevel;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static io.cloudslang.lang.compiler.SlangTextualKeys.DO_EXTERNAL_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.DO_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.FOR_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.INPUTS_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.MAX_THROTTLE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.NAVIGATION_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.ON_FAILURE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PARALLEL_LOOP_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.PYTHON_ACTION_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.ROBOT_GROUP;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_SETTINGS_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEPS_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.WORKER_GROUP;
import static io.cloudslang.lang.compiler.SlangTextualKeys.WORKFLOW_KEY;
import static io.cloudslang.lang.compiler.utils.SlangSourceUtils.getNavigationStepName;
import static io.cloudslang.lang.compiler.utils.SlangSourceUtils.getNavigationTarget;
import static io.cloudslang.lang.entities.ScoreLangConstants.FAILURE_RESULT;
import static io.cloudslang.lang.entities.ScoreLangConstants.LOOP_KEY;
import static io.cloudslang.lang.entities.ScoreLangConstants.NAMESPACE_DELIMITER;
import static io.cloudslang.lang.entities.ScoreLangConstants.SUCCESS_RESULT;
import static io.cloudslang.lang.entities.ScoreLangConstants.WARNING_RESULT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

public class ExecutableBuilder {

	public static final String UNIQUE_STEP_NAME_MESSAGE_SUFFIX = "Each step name in the workflow must be unique";

	private List<Transformer> transformers;

	private TransformersHandler transformersHandler;

	private DependenciesHelper dependenciesHelper;

	private SystemPropertiesHelper systemPropertiesHelper;

	private PreCompileValidator preCompileValidator;

	private ResultsTransformer resultsTransformer;

	private ExecutableValidator executableValidator;

	private List<Transformer> preExecTransformers;
	private List<Transformer> postExecTransformers;

	private List<String> executableAdditionalKeywords = singletonList(SlangTextualKeys.EXECUTABLE_NAME_KEY);
	private List<String> operationAdditionalKeywords = asList(SlangTextualKeys.JAVA_ACTION_KEY,
			SlangTextualKeys.PYTHON_ACTION_KEY, SlangTextualKeys.SEQ_ACTION_KEY);
	private List<String> flowAdditionalKeywords = asList(WORKFLOW_KEY, WORKER_GROUP);
	private List<String> allExecutableAdditionalKeywords;

	private List<Transformer> actionTransformers;
	private List<List<String>> executableConstraintGroups;

	private List<Transformer> preStepTransformers;
	private List<Transformer> postStepTransformers;

	private List<Transformer> externalPreStepTransformers;
	private List<Transformer> externalPostStepTransformers;

	private List<String> stepAdditionalKeyWords = asList(LOOP_KEY, DO_KEY, DO_EXTERNAL_KEY, NAVIGATION_KEY,
			WORKER_GROUP, ROBOT_GROUP);
	private List<String> parallelLoopValidKeywords = asList(DO_KEY, DO_EXTERNAL_KEY, FOR_KEY, WORKER_GROUP,
			MAX_THROTTLE_KEY);
	private List<String> parallelLoopConstructKeywords = asList(FOR_KEY, MAX_THROTTLE_KEY);

	private List<String> seqSupportedResults = asList(SUCCESS_RESULT, WARNING_RESULT, FAILURE_RESULT);

	// @PostConstruct
	public void initScopedTransformersAndKeys() {
		//executable transformers
		preExecTransformers = filterTransformers(Transformer.Scope.BEFORE_EXECUTABLE);
		postExecTransformers = filterTransformers(Transformer.Scope.AFTER_EXECUTABLE);

		//action transformers and keys
		actionTransformers = filterTransformers(Transformer.Scope.ACTION);

		allExecutableAdditionalKeywords = new ArrayList<>(executableAdditionalKeywords.size()
				+ operationAdditionalKeywords.size() + flowAdditionalKeywords.size());
		allExecutableAdditionalKeywords.addAll(executableAdditionalKeywords);
		allExecutableAdditionalKeywords.addAll(operationAdditionalKeywords);
		allExecutableAdditionalKeywords.addAll(flowAdditionalKeywords);

		// keys excluding each other
		executableConstraintGroups = new ArrayList<>();
		executableConstraintGroups.add(ListUtils.union(singletonList(WORKFLOW_KEY), operationAdditionalKeywords));

		//step transformers
		preStepTransformers = filterTransformers(Transformer.Scope.BEFORE_STEP);
		postStepTransformers = filterTransformers(Transformer.Scope.AFTER_STEP);

		final List<Transformer> tempPreStepTransformers = filterTransformers(Transformer.Scope.BEFORE_STEP);
		final List<Transformer> tempPostStepTransformers = filterTransformers(Transformer.Scope.AFTER_STEP);

		preStepTransformers = tempPreStepTransformers.stream().filter(t -> t.getType() != Transformer.Type.EXTERNAL)
				.collect(Collectors.toList());

		postStepTransformers = tempPostStepTransformers.stream().filter(t -> t.getType() != Transformer.Type.EXTERNAL)
				.collect(Collectors.toList());

		externalPreStepTransformers = tempPreStepTransformers.stream()
				.filter(t -> t.getType() != Transformer.Type.INTERNAL).collect(Collectors.toList());

		externalPostStepTransformers = tempPostStepTransformers.stream()
				.filter(t -> t.getType() != Transformer.Type.INTERNAL).collect(Collectors.toList());

	}

	private List<Transformer> filterTransformers(Transformer.Scope scope) {
		return filter(having(on(Transformer.class).getScopes().contains(scope)), transformers);
	}

	public ExecutableModellingResult transformToExecutable(ParsedSlang parsedSlang,
			Map<String, Object> executableRawData, SensitivityLevel sensitivityLevel) {
		List<RuntimeException> errors = new ArrayList<>();
		String execName = preCompileValidator.validateExecutableRawData(parsedSlang, executableRawData, errors);
		String workerGroup = (String) executableRawData.get(SlangTextualKeys.WORKER_GROUP);
		errors.addAll(preCompileValidator.checkKeyWords(execName, "", executableRawData,
				ListUtils.union(preExecTransformers, postExecTransformers),
				ParsedSlang.Type.DECISION.equals(parsedSlang.getType()) ? executableAdditionalKeywords
						: allExecutableAdditionalKeywords,
				executableConstraintGroups));

		Map<String, Serializable> preExecutableActionData = new HashMap<>();
		Map<String, Serializable> postExecutableActionData = new HashMap<>();

		String errorMessagePrefix = "For " + parsedSlang.getType().toString().toLowerCase() + " '" + execName
				+ "' syntax is illegal.\n";
		preExecutableActionData.putAll(transformersHandler.runTransformers(executableRawData, preExecTransformers,
				errors, errorMessagePrefix, sensitivityLevel));
		postExecutableActionData.putAll(transformersHandler.runTransformers(executableRawData, postExecTransformers,
				errors, errorMessagePrefix, sensitivityLevel));

		@SuppressWarnings("unchecked")
		List<Input> inputs = (List<Input>) preExecutableActionData.remove(SlangTextualKeys.INPUTS_KEY);
		@SuppressWarnings("unchecked")
		List<Output> outputs = (List<Output>) postExecutableActionData.remove(SlangTextualKeys.OUTPUTS_KEY);

		@SuppressWarnings("unchecked")
		List<Result> results = (List<Result>) postExecutableActionData.remove(SlangTextualKeys.RESULTS_KEY);
		results = results == null ? new ArrayList<Result>() : results;

		String namespace = parsedSlang.getNamespace();
		Set<String> systemPropertyDependencies = new HashSet<>();

		Executable executable;
		boolean isSeqAction = false;

		switch (parsedSlang.getType()) {
		case FLOW:
			resultsTransformer.addDefaultResultsIfNeeded((List) executableRawData.get(SlangTextualKeys.RESULTS_KEY),
					ExecutableType.FLOW, results, errors);

			Map<String, String> imports = parsedSlang.getImports();

			List<Map<String, Map<String, Object>>> workFlowRawData = preCompileValidator
					.validateWorkflowRawData(parsedSlang, executableRawData.get(WORKFLOW_KEY), execName, errors);

			Workflow onFailureWorkFlow = getOnFailureWorkflow(workFlowRawData, imports, errors, namespace, execName,
					sensitivityLevel);

			WorkflowModellingResult workflowModellingResult = compileWorkFlow(workFlowRawData, imports,
					onFailureWorkFlow, false, namespace, sensitivityLevel);
			errors.addAll(workflowModellingResult.getErrors());
			Workflow workflow = workflowModellingResult.getWorkflow();

			preCompileValidator.validateResultsHaveNoExpression(results, execName, errors);

			Pair<Set<String>, Set<String>> pair = fetchDirectStepsDependencies(workflow);
			Set<String> executableDependencies = pair.getLeft();
			Set<String> externalExecutableDependencies = pair.getRight();
			try {
				systemPropertyDependencies = dependenciesHelper.getSystemPropertiesForFlow(inputs, outputs, results,
						workflow.getSteps());

			} catch (RuntimeException ex) {
				errors.add(ex);
			}
			executable = new Flow(preExecutableActionData, postExecutableActionData, workflow, namespace, execName,
					workerGroup, inputs, outputs, results, executableDependencies, externalExecutableDependencies,
					systemPropertyDependencies);

			break;

		case OPERATION:
			resultsTransformer.addDefaultResultsIfNeeded((List) executableRawData.get(SlangTextualKeys.RESULTS_KEY),
					ExecutableType.OPERATION, results, errors);

			Map<String, Object> actionRawData = getActionRawData(executableRawData, errors, parsedSlang, execName);
			ActionModellingResult actionModellingResult = compileAction(actionRawData, sensitivityLevel);
			errors.addAll(actionModellingResult.getErrors());
			final Action action = actionModellingResult.getAction();
			executableDependencies = new HashSet<>();

			isSeqAction = actionRawData.containsKey(SlangTextualKeys.SEQ_ACTION_KEY);
			List<SeqStep> seqSteps = new ArrayList<>();
			if (!isSeqAction) {
				preCompileValidator.validateResultTypes(results, execName, errors);
				preCompileValidator.validateDefaultResult(results, execName, errors);
			} else {
				preCompileValidator.validateResultsHaveNoExpression(results, execName, errors);
				preCompileValidator.validateResultsWithWhitelist(results, seqSupportedResults, execName, errors);
				seqSteps = (List) ((Map) actionRawData.get(SlangTextualKeys.SEQ_ACTION_KEY)).get(SEQ_STEPS_KEY);
				@SuppressWarnings("unchecked")
				Map<String, Object> settings = (Map<String, Object>) ((Map) actionRawData
						.get(SlangTextualKeys.SEQ_ACTION_KEY)).get(SEQ_SETTINGS_KEY);
				if (isNotEmpty(parsedSlang.getObjectRepository())) {
					Set<String> sysPropObjRepo = systemPropertiesHelper
							.getObjectRepositorySystemProperties(parsedSlang.getObjectRepository());
					systemPropertyDependencies.addAll(sysPropObjRepo);
				}
				if (isNotEmpty(settings)) {
					Set<String> sysPropSettings = systemPropertiesHelper.getSystemPropertiesFromSettings(settings);
					systemPropertyDependencies.addAll(sysPropSettings);
				}
			}

			try {
				systemPropertyDependencies
						.addAll(dependenciesHelper.getSystemPropertiesForOperation(inputs, outputs, results, seqSteps));
			} catch (RuntimeException ex) {
				errors.add(ex);
			}
			executable = new Operation(preExecutableActionData, postExecutableActionData, action, namespace, execName,
					inputs, outputs, results, executableDependencies, systemPropertyDependencies);

			break;

		case DECISION:
			resultsTransformer.addDefaultResultsIfNeeded((List) executableRawData.get(SlangTextualKeys.RESULTS_KEY),
					ExecutableType.DECISION, results, errors);

			preCompileValidator.validateResultTypes(results, execName, errors);
			preCompileValidator.validateDecisionResultsSection(executableRawData, execName, errors);
			preCompileValidator.validateDefaultResult(results, execName, errors);

			try {
				systemPropertyDependencies = dependenciesHelper.getSystemPropertiesForDecision(inputs, outputs,
						results);
			} catch (RuntimeException ex) {
				errors.add(ex);
			}
			executable = new Decision(preExecutableActionData, postExecutableActionData, namespace, execName, inputs,
					outputs, results, Collections.<String>emptySet(), systemPropertyDependencies);

			break;

		default:
			throw new RuntimeException(
					"Error compiling " + parsedSlang.getName() + ". It is not of flow, operations or decision type");
		}

		if (!isSeqAction && outputs != null) {
			errors.addAll(validateOutputs(outputs));
		}

		return preCompileValidator.validateResult(parsedSlang, execName,
				new ExecutableModellingResult(executable, errors));
	}

	private List<RuntimeException> validateOutputs(List<Output> outputs) {

		Function<Output, RuntimeException> map = output -> new RuntimeException(
				"'" + SlangTextualKeys.SEQ_OUTPUT_ROBOT_KEY + "' property allowed only for outputs of "
						+ SlangTextualKeys.SEQ_ACTION_KEY + ". Encountered at output " + output.getName());

		return outputs.stream().filter(output -> output.hasRobotProperty()).map(map).collect(Collectors.toList());
	}

	private Map<String, Object> getActionRawData(Map<String, Object> executableRawData, List<RuntimeException> errors,
			ParsedSlang parsedSlang, String execName) {
		Map<String, Object> actionRawData = new HashMap<>();
		Object javaActionRawData = executableRawData.get(SlangTextualKeys.JAVA_ACTION_KEY);
		Object pythonActionRawData = executableRawData.get(SlangTextualKeys.PYTHON_ACTION_KEY);
		Object seqActionRawData = executableRawData.get(SlangTextualKeys.SEQ_ACTION_KEY);
		if (javaActionRawData != null) {
			actionRawData.put(SlangTextualKeys.JAVA_ACTION_KEY,
					executableRawData.get(SlangTextualKeys.JAVA_ACTION_KEY));
		}
		if (pythonActionRawData != null) {
			Object pythonActionObject = executableRawData.get(PYTHON_ACTION_KEY);
			if (pythonActionObject instanceof Map) {
				Map<String, Object> pythonAction = (Map<String, Object>) executableRawData.get(PYTHON_ACTION_KEY);
				pythonAction.put(SlangTextualKeys.INPUTS_KEY, executableRawData.get(INPUTS_KEY));
			}
			actionRawData.put(PYTHON_ACTION_KEY, pythonActionObject);
		}
		if (seqActionRawData != null) {
			actionRawData.put(SlangTextualKeys.SEQ_ACTION_KEY, executableRawData.get(SlangTextualKeys.SEQ_ACTION_KEY));
		}
		if (MapUtils.isEmpty(actionRawData)) {
			errors.add(new RuntimeException(
					"Error compiling " + parsedSlang.getName() + ". Operation: " + execName + " has no action data"));
		}
		return actionRawData;
	}

	private Workflow getOnFailureWorkflow(List<Map<String, Map<String, Object>>> workFlowRawData,
			Map<String, String> imports, List<RuntimeException> errors, String namespace, String execName,
			SensitivityLevel sensitivityLevel) {

		Map<String, Map<String, Object>> onFailureStepData = preCompileValidator
				.validateOnFailurePosition(workFlowRawData, execName, errors);

		Workflow onFailureWorkFlow = null;
		if (isNotEmpty(onFailureStepData)) {
			List<Map<String, Map<String, Object>>> onFailureData;
			try {
				//noinspection unchecked
				onFailureData = (List<Map<String, Map<String, Object>>>) onFailureStepData.values().iterator().next();
			} catch (ClassCastException ex) {
				onFailureData = new ArrayList<>();
				errors.add(new RuntimeException(
						"Flow: '" + execName + "' syntax is illegal.\nBelow 'on_failure' property there "
								+ "should be a list of steps and not a map"));
			}
			if (CollectionUtils.isNotEmpty(onFailureData)) {
				if (onFailureData.size() > 1) {
					errors.add(new RuntimeException("Flow: '" + execName
							+ "' syntax is illegal.\nBelow 'on_failure' property " + "there should be only one step"));
				}
				handleOnFailureStepNavigationSection(onFailureData, execName, errors);

				WorkflowModellingResult workflowModellingResult = compileWorkFlow(onFailureData, imports, null, true,
						namespace, sensitivityLevel);
				errors.addAll(workflowModellingResult.getErrors());
				onFailureWorkFlow = workflowModellingResult.getWorkflow();
			} else if (onFailureData == null) {
				errors.add(new RuntimeException("Flow: '" + execName
						+ "' syntax is illegal.\nThere is no step below the 'on_failure' property."));
			}
		}
		return onFailureWorkFlow;
	}

	private void handleOnFailureStepNavigationSection(List<Map<String, Map<String, Object>>> onFailureData,
			String execName, List<RuntimeException> errors) {
		Map.Entry<String, Map<String, Object>> onFailureStep = getFirstOnFailureStep(onFailureData);
		if (onFailureStep.getValue().containsKey(NAVIGATION_KEY)) {
			errors.add(new RuntimeException(
					"Flow: '" + execName + "' syntax is illegal.\nThe step below 'on_failure' property should "
							+ "not contain a \"navigate\" section."));
		}
	}

	private Map.Entry<String, Map<String, Object>> getFirstOnFailureStep(
			List<Map<String, Map<String, Object>>> onFailureData) {
		Map<String, Map<String, Object>> onFailureStepMap = onFailureData.iterator().next();
		return onFailureStepMap.entrySet().iterator().next();
	}

	private ActionModellingResult compileAction(Map<String, Object> actionRawData, SensitivityLevel sensitivityLevel) {
		Map<String, Serializable> actionData = new HashMap<>();

		List<RuntimeException> errors = preCompileValidator.checkKeyWords("action data", "", actionRawData,
				actionTransformers, null, null);

		String errorMessagePrefix = "Action syntax is illegal.\n";
		actionData.putAll(transformersHandler.runTransformers(actionRawData, actionTransformers, errors,
				errorMessagePrefix, sensitivityLevel));

		Action action = new Action(actionData);
		return new ActionModellingResult(action, errors);
	}

	private WorkflowModellingResult compileWorkFlow(List<Map<String, Map<String, Object>>> workFlowRawData,
			Map<String, String> imports, Workflow onFailureWorkFlow, boolean onFailureSection, String namespace,
			SensitivityLevel sensitivityLevel) {

		List<RuntimeException> errors = new ArrayList<>();

		Deque<Step> steps = new LinkedList<>();
		Set<String> stepNames = new HashSet<>();
		Deque<Step> onFailureSteps = !(onFailureSection || onFailureWorkFlow == null) ? onFailureWorkFlow.getSteps()
				: new LinkedList<Step>();
		List<String> onFailureStepNames = getStepNames(onFailureSteps);
		boolean onFailureStepFound = onFailureStepNames.size() > 0;
		String defaultFailure = onFailureStepFound ? onFailureStepNames.get(0) : ScoreLangConstants.FAILURE_RESULT;

		PeekingIterator<Map<String, Map<String, Object>>> iterator = new PeekingIterator<>(workFlowRawData.iterator());
		while (iterator.hasNext()) {
			Map<String, Map<String, Object>> stepRawData = iterator.next();
			String stepName = getStepName(stepRawData);
			validateStepName(stepName, errors);
			if (stepNames.contains(stepName) || onFailureStepNames.contains(stepName)) {
				errors.add(new RuntimeException("Step name: \'" + stepName
						+ "\' appears more than once in the workflow. " + UNIQUE_STEP_NAME_MESSAGE_SUFFIX));
			}
			stepNames.add(stepName);
			Map<String, Object> stepRawDataValue;
			String message = "Step: " + stepName + " syntax is illegal.\nBelow step name, there should "
					+ "be a map of values in the format:\ndo:\n\top_name:";
			try {
				stepRawDataValue = stepRawData.values().iterator().next();
				if (isNotEmpty(stepRawDataValue)) {
					boolean loopKeyFound = stepRawDataValue.containsKey(LOOP_KEY);
					boolean parallelLoopKeyFound = stepRawDataValue.containsKey(PARALLEL_LOOP_KEY);
					if (loopKeyFound) {
						if (parallelLoopKeyFound) {
							errors.add(
									new RuntimeException("Step: " + stepName + " syntax is illegal.\nBelow step name, "
											+ "there can be either \'loop\' or \'aync_loop\' key."));
						}
						message = "Step: " + stepName + " syntax is illegal.\nBelow the 'loop' keyword, there "
								+ "should be a map of values in the format:\nfor:\ndo:\n\top_name:";
						@SuppressWarnings("unchecked")
						Map<String, Object> loopRawData = (Map<String, Object>) stepRawDataValue.remove(LOOP_KEY);
						stepRawDataValue.putAll(loopRawData);
					}
					if (parallelLoopKeyFound) {
						message = "Step: " + stepName + " syntax is illegal.\nBelow the 'parallel_loop' keyword, there "
								+ "should be a map of values in the format:\nfor:\ndo:\n\top_name:";
						@SuppressWarnings("unchecked")
						Map<String, Object> parallelLoopRawData = (Map<String, Object>) stepRawDataValue
								.remove(PARALLEL_LOOP_KEY);

						errors.addAll(preCompileValidator.checkKeyWords(stepName, SlangTextualKeys.PARALLEL_LOOP_KEY,
								parallelLoopRawData, Collections.emptyList(), parallelLoopValidKeywords, null));

						Map<String, Object> filteredParallelLoopData = newHashMapWithExpectedSize(
								parallelLoopConstructKeywords.size());
						for (String keyword : parallelLoopConstructKeywords) {
							filteredParallelLoopData.put(keyword, parallelLoopRawData.remove(keyword));
						}

						parallelLoopRawData.put(PARALLEL_LOOP_KEY, filteredParallelLoopData);
						stepRawDataValue.putAll(parallelLoopRawData);
					}
				}
			} catch (ClassCastException ex) {
				stepRawDataValue = new HashMap<>();
				errors.add(new RuntimeException(message));
			}

			String defaultSuccess;
			Map<String, Map<String, Object>> nextStepData = iterator.peek();
			if (nextStepData != null) {
				defaultSuccess = nextStepData.keySet().iterator().next();
			} else {
				defaultSuccess = onFailureSection ? ScoreLangConstants.FAILURE_RESULT : SUCCESS_RESULT;
			}

			String onFailureStepName = onFailureStepFound ? onFailureStepNames.get(0) : null;
			StepModellingResult stepModellingResult = compileStep(stepName, stepRawDataValue, defaultSuccess, imports,
					defaultFailure, namespace, onFailureStepName, onFailureSection, sensitivityLevel);

			errors.addAll(stepModellingResult.getErrors());
			steps.add(stepModellingResult.getStep());
		}

		if (onFailureStepFound) {
			steps.addAll(onFailureSteps);
		}

		return new WorkflowModellingResult(new Workflow(steps), errors);
	}

	private String getStepName(Map<String, Map<String, Object>> stepRawData) {
		return stepRawData.keySet().iterator().next();
	}

	private String validateStepName(String stepName, List<RuntimeException> errors) {
		try {
			executableValidator.validateStepName(stepName);
		} catch (RuntimeException rex) {
			errors.add(rex);
		}
		return stepName;
	}

	private StepModellingResult compileStep(String stepName, Map<String, Object> stepRawData, String defaultSuccess,
			Map<String, String> imports, String defaultFailure, String namespace, String onFailureStepName,
			boolean onFailureSection, SensitivityLevel sensitivityLevel) {

		List<RuntimeException> errors = new ArrayList<>();
		if (MapUtils.isEmpty(stepRawData)) {
			stepRawData = new HashMap<>();
			errors.add(new RuntimeException("Step: " + stepName + " has no data"));
		}

		final boolean isExternal = stepRawData.containsKey(DO_EXTERNAL_KEY);
		final List<Transformer> localPreStepTransformers = isExternal ? externalPreStepTransformers
				: preStepTransformers;
		final List<Transformer> localPostStepTransformers = isExternal ? externalPostStepTransformers
				: postStepTransformers;

		Map<String, Serializable> preStepData = new HashMap<>();
		Map<String, Serializable> postStepData = new HashMap<>();

		errors.addAll(preCompileValidator.checkKeyWords(stepName, "", stepRawData,
				ListUtils.union(localPreStepTransformers, localPostStepTransformers), stepAdditionalKeyWords, null));

		String errorMessagePrefix = "For step '" + stepName + "' syntax is illegal.\n";
		preStepData.putAll(transformersHandler.runTransformers(stepRawData, localPreStepTransformers, errors,
				errorMessagePrefix, sensitivityLevel));
		final List<Argument> arguments = getArgumentsFromDoStep(preStepData);
		postStepData.putAll(transformersHandler.runTransformers(stepRawData, localPostStepTransformers, errors,
				errorMessagePrefix, sensitivityLevel, arguments));

		replaceOnFailureReference(postStepData, onFailureStepName);

		String workerGroup = computeWorkerGroupString(stepRawData);
		String robotGroup = (String) stepRawData.get(SlangTextualKeys.ROBOT_GROUP);

		String refId = "";
		final Map<String, Object> doRawData = getRawDataFromDoStep(stepRawData);

		if (isNotEmpty(doRawData)) {
			try {
				String refString = doRawData.keySet().iterator().next();
				refId = resolveReferenceId(refString, imports, namespace, preStepData);
			} catch (RuntimeException rex) {
				errors.add(rex);
			}
		}

		List<Map<String, Serializable>> navigationStrings = getNavigationStrings(postStepData, defaultSuccess,
				defaultFailure, errors);

		Step step = createStep(stepName, onFailureSection, preStepData, postStepData, arguments, workerGroup,
				robotGroup, refId, navigationStrings);
		return new StepModellingResult(step, errors);
	}

	private String computeWorkerGroupString(Map<String, Object> stepRawData) {
		String workerGroup = null;
		if (stepRawData.get(SlangTextualKeys.WORKER_GROUP) instanceof String) {
			workerGroup = (String) stepRawData.get(SlangTextualKeys.WORKER_GROUP);
		} else if (stepRawData.get(SlangTextualKeys.WORKER_GROUP) instanceof Map) {
			workerGroup = String.valueOf(
					((Map<String, Object>) stepRawData.get(SlangTextualKeys.WORKER_GROUP)).get(SlangTextualKeys.VALUE));
		}
		return workerGroup;
	}

	private Step createStep(String stepName, boolean onFailureSection, Map<String, Serializable> preStepData,
			Map<String, Serializable> postStepData, List<Argument> arguments, String workerGroup, String robotGroup,
			String refId, List<Map<String, Serializable>> navigationStrings) {
		if (preStepData.containsKey(DO_EXTERNAL_KEY)) {
			return new ExternalStep(stepName, preStepData, postStepData, arguments, navigationStrings, refId,
					workerGroup, preStepData.containsKey(SlangTextualKeys.PARALLEL_LOOP_KEY), onFailureSection);
		} else {
			return new Step(stepName, preStepData, postStepData, arguments, navigationStrings, refId, workerGroup,
					robotGroup, preStepData.containsKey(SlangTextualKeys.PARALLEL_LOOP_KEY), onFailureSection);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Argument> getArgumentsFromDoStep(Map<String, Serializable> preStepData) {
		return (List<Argument>) preStepData.getOrDefault(DO_EXTERNAL_KEY, preStepData.get(DO_KEY));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getRawDataFromDoStep(Map<String, Object> stepRawData) {
		try {
			return (Map<String, Object>) stepRawData.getOrDefault(DO_EXTERNAL_KEY, stepRawData.get(DO_KEY));
		} catch (ClassCastException ex) {
			return Collections.emptyMap();
		}
	}

	private void replaceOnFailureReference(Map<String, Serializable> postStepData, String onFailureStepName) {
		Serializable navigationData = postStepData.get(NAVIGATION_KEY);
		if (navigationData != null) {
			@SuppressWarnings("unchecked") // from NavigateTransformer
			List<Map<String, Serializable>> navigationStrings = (List<Map<String, Serializable>>) navigationData;
			List<Map<String, Serializable>> transformedNavigationStrings = new ArrayList<>();

			for (Map<String, Serializable> navigation : navigationStrings) {
				Map.Entry<String, Serializable> navigationEntry = navigation.entrySet().iterator().next();
				Map<String, Serializable> transformedNavigation = new HashMap<>(navigation);
				if (getNavigationStepName(navigationEntry.getValue()).equals(ON_FAILURE_KEY)) {
					if (StringUtils.isEmpty(onFailureStepName)) {
						transformedNavigation.put(navigationEntry.getKey(),
								getNavigationTarget(navigationEntry.getValue(), ScoreLangConstants.FAILURE_RESULT));
					} else {
						transformedNavigation.put(navigationEntry.getKey(),
								getNavigationTarget(navigationEntry.getValue(), onFailureStepName));
					}
				} else {
					transformedNavigation.put(navigationEntry.getKey(), navigationEntry.getValue());
				}
				transformedNavigationStrings.add(transformedNavigation);
			}
			postStepData.put(NAVIGATION_KEY, (Serializable) transformedNavigationStrings);
		}
	}

	private List<Map<String, Serializable>> getNavigationStrings(Map<String, Serializable> postStepData,
			String defaultSuccess, String defaultFailure, List<RuntimeException> errors) {
		@SuppressWarnings("unchecked")
		List<Map<String, Serializable>> navigationStrings = (List<Map<String, Serializable>>) postStepData
				.get(NAVIGATION_KEY);

		//default navigation
		if (CollectionUtils.isEmpty(navigationStrings)) {
			navigationStrings = new ArrayList<>();
			Map<String, Serializable> successMap = new HashMap<>();
			successMap.put(SUCCESS_RESULT, defaultSuccess);
			Map<String, Serializable> failureMap = new HashMap<>();
			failureMap.put(ScoreLangConstants.FAILURE_RESULT, defaultFailure);
			navigationStrings.add(successMap);
			navigationStrings.add(failureMap);
			return navigationStrings;
		} else {
			return navigationStrings;
		}
	}

	private String resolveReferenceId(String rawReferenceId, Map<String, String> imports, String namespace,
			Map<String, Serializable> preStepData) {
		if (preStepData.containsKey(DO_EXTERNAL_KEY)) {
			return rawReferenceId;
		}
		return resolveDoReferenceId(rawReferenceId, imports, namespace);
	}

	private String resolveDoReferenceId(String rawReferenceId, Map<String, String> imports, String namespace) {

		int numberOfDelimiters = StringUtils.countMatches(rawReferenceId, NAMESPACE_DELIMITER);
		String resolvedReferenceId;

		if (numberOfDelimiters == 0) {
			// implicit namespace
			resolvedReferenceId = namespace + NAMESPACE_DELIMITER + rawReferenceId;
		} else {
			String prefix = StringUtils.substringBefore(rawReferenceId, NAMESPACE_DELIMITER);
			String suffix = StringUtils.substringAfter(rawReferenceId, NAMESPACE_DELIMITER);
			if (isNotEmpty(imports) && imports.containsKey(prefix)) {
				// expand alias
				resolvedReferenceId = imports.get(prefix) + NAMESPACE_DELIMITER + suffix;
			} else {
				// full path without alias expanding
				resolvedReferenceId = rawReferenceId;
			}
		}

		return resolvedReferenceId;
	}

	/**
	 * Fetch the first level of the dependencies of the executable (non recursively)
	 *
	 * @param workflow the workflow of the flow
	 * @return a Pair with two sets of dependencies. One set is for CloudSlang dependencies
	 *         and the other one is for external dependencies.
	 */
	private Pair<Set<String>, Set<String>> fetchDirectStepsDependencies(Workflow workflow) {
		Set<String> dependencies = new HashSet<>();
		Set<String> externalDependencies = new HashSet<>();
		Deque<Step> steps = workflow.getSteps();
		for (Step step : steps) {
			if (step instanceof ExternalStep) {
				externalDependencies.add(step.getRefId());
			} else {
				dependencies.add(step.getRefId());
			}
		}
		return Pair.of(dependencies, externalDependencies);
	}

	private List<String> getStepNames(Deque<Step> steps) {
		List<String> stepNames = new ArrayList<>();
		for (Step step : steps) {
			stepNames.add(step.getName());
		}
		return stepNames;
	}

	public void setTransformers(List<Transformer> transformers) {
		this.transformers = transformers;
	}

	public void setTransformersHandler(TransformersHandler transformersHandler) {
		this.transformersHandler = transformersHandler;
	}

	public void setDependenciesHelper(DependenciesHelper dependenciesHelper) {
		this.dependenciesHelper = dependenciesHelper;
	}

	public void setSystemPropertiesHelper(SystemPropertiesHelper systemPropertiesHelper) {
		this.systemPropertiesHelper = systemPropertiesHelper;
	}

	public void setPreCompileValidator(PreCompileValidator preCompileValidator) {
		this.preCompileValidator = preCompileValidator;
	}

	public void setResultsTransformer(ResultsTransformer resultsTransformer) {
		this.resultsTransformer = resultsTransformer;
	}

	public void setExecutableValidator(ExecutableValidator executableValidator) {
		this.executableValidator = executableValidator;
	}

	public void setPreExecTransformers(List<Transformer> preExecTransformers) {
		this.preExecTransformers = preExecTransformers;
	}

	public void setPostExecTransformers(List<Transformer> postExecTransformers) {
		this.postExecTransformers = postExecTransformers;
	}

	public void setExecutableAdditionalKeywords(List<String> executableAdditionalKeywords) {
		this.executableAdditionalKeywords = executableAdditionalKeywords;
	}

	public void setOperationAdditionalKeywords(List<String> operationAdditionalKeywords) {
		this.operationAdditionalKeywords = operationAdditionalKeywords;
	}

	public void setFlowAdditionalKeywords(List<String> flowAdditionalKeywords) {
		this.flowAdditionalKeywords = flowAdditionalKeywords;
	}

	public void setAllExecutableAdditionalKeywords(List<String> allExecutableAdditionalKeywords) {
		this.allExecutableAdditionalKeywords = allExecutableAdditionalKeywords;
	}

	public void setActionTransformers(List<Transformer> actionTransformers) {
		this.actionTransformers = actionTransformers;
	}

	public void setExecutableConstraintGroups(List<List<String>> executableConstraintGroups) {
		this.executableConstraintGroups = executableConstraintGroups;
	}

	public void setPreStepTransformers(List<Transformer> preStepTransformers) {
		this.preStepTransformers = preStepTransformers;
	}

	public void setPostStepTransformers(List<Transformer> postStepTransformers) {
		this.postStepTransformers = postStepTransformers;
	}

	public void setStepAdditionalKeyWords(List<String> stepAdditionalKeyWords) {
		this.stepAdditionalKeyWords = stepAdditionalKeyWords;
	}

	public void setParallelLoopValidKeywords(List<String> parallelLoopValidKeywords) {
		this.parallelLoopValidKeywords = parallelLoopValidKeywords;
	}
}
