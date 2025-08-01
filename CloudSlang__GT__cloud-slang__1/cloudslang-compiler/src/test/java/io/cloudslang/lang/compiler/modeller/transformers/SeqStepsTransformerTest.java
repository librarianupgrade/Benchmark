/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.modeller.DependenciesHelper;
import io.cloudslang.lang.compiler.modeller.model.SeqStep;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_ACTION_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_ARGS_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_DEFAULT_ARGS_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_HIGHLIGHT_ID_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_ID_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_PATH_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_SNAPSHOT_KEY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SeqStepsTransformerTest.Config.class })
public class SeqStepsTransformerTest extends TransformersTestParent {

	@Autowired
	private SeqStepsTransformer seqStepsTransformer;

	@Autowired
	private DependenciesHelper dependenciesHelper;

	@Test
	public void testTransformSimple() {
		List<Map<String, Map<String, String>>> steps = asList(
				newStep("1", "Browser", "Open", "www.google.com", "www.google.com", null, null),
				newStep("2", "Browser", "Open", "www.google.com", "www.google.com", "snapshot", "1234"),
				newStep("3", "AnotherBrowser", "Open", "www..com", "www..com", "snapshot", null),
				newStep("4", "Browser", "Close", "www.google.com", "www.google.com", null, "1234"));
		List<SeqStep> expectedSteps = asList(
				newSeqStep("1", "Browser", "Open", "www.google.com", "www.google.com", null, null),
				newSeqStep("2", "Browser", "Open", "www.google.com", "www.google.com", "snapshot", "1234"),
				newSeqStep("3", "AnotherBrowser", "Open", "www..com", "www..com", "snapshot", null),
				newSeqStep("4", "Browser", "Close", "www.google.com", "www.google.com", null, "1234"));
		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), is(empty()));
		assertEquals(expectedSteps, transform.getTransformedData());
	}

	@Test
	public void testTransformStepWithMissingReqKeys() {
		List<Map<String, Map<String, String>>> steps = singletonList(newStep(null, null, null, null, null, "a", "b"));
		List<SeqStep> expectedSteps = new ArrayList<>();
		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), hasSize(1));
		assertEquals(transform.getErrors().get(0).getMessage(),
				"Sequential operation step has the following missing tags: [object_path, action, id]");
		assertEquals(expectedSteps, transform.getTransformedData());
	}

	@Test
	public void testTransformStepWithEmptyReqKeys() {
		List<Map<String, Map<String, String>>> steps = singletonList(newStep("", "", "", "", "", "a", "b"));
		List<SeqStep> expectedSteps = new ArrayList<>();
		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), hasSize(1));
		assertEquals(transform.getErrors().get(0).getMessage(),
				"Sequential operation step has the following empty tags: [object_path, action, id]");
		assertEquals(expectedSteps, transform.getTransformedData());
	}

	@Test
	public void testTransformStepWithIllegalKeys() {
		List<Map<String, Map<String, String>>> steps = singletonList(
				newStep("1", "Browser", "Open", "www.google.com", "www.google.com", null, null));
		steps.get(0).get("step").put("illegal-key", "value-for-illegal-key");
		List<SeqStep> expectedSteps = new ArrayList<>();
		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), hasSize(1));
		assertEquals(transform.getErrors().get(0).getMessage(),
				"Sequential operation step has the following illegal tags: [illegal-key]. "
						+ "Please take a look at the supported features per versions link");
		assertEquals(expectedSteps, transform.getTransformedData());
	}

	@Test
	public void testTransformStepWithDuplicateIds() {
		List<Map<String, Map<String, String>>> steps = asList(
				newStep("1", "Browser", "Open", "www.google.com", "www.google.com", null, null),
				newStep("1", "Tab", "Close", null, null, null, null));
		List<SeqStep> expectedSteps = singletonList(
				newSeqStep("1", "Browser", "Open", "www.google.com", "www.google.com", null, null));
		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), hasSize(1));
		assertEquals(transform.getErrors().get(0).getMessage(),
				"Found duplicate step with id '1' for sequential operation step.");
		assertEquals(expectedSteps, transform.getTransformedData());
	}

	@Test
	public void testTransformStepWithAssignmentAction() {
		List<Map<String, Map<String, String>>> steps = singletonList(
				newStep("1", "Parameter(\"param1\")", "=", "12", "12", null, null));
		List<SeqStep> expectedSteps = singletonList(
				newSeqStep("1", "Parameter(\"param1\")", "=", "12", "12", null, null));
		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), is(empty()));
		assertEquals(expectedSteps, transform.getTransformedData());
	}

	@Test
	public void testTransformStepWithAssignmentActionMissingArg() {
		List<Map<String, Map<String, String>>> steps = singletonList(
				newStep("1", "Parameter(\"param1\")", "=", null, null, null, null));
		List<SeqStep> expectedSteps = new ArrayList<>();
		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), hasSize(1));
		assertEquals(transform.getErrors().get(0).getMessage(),
				"Found invalid assignment operation for sequential operation step with id '1'.");
		assertEquals(expectedSteps, transform.getTransformedData());
	}

	@Test
	public void testTransformWaitSteps() {
		List<Map<String, Map<String, String>>> steps = new ArrayList<>();
		steps.add(newStep("1", null, "Wait", null, "\"2\"", null, null));
		steps.add(newStep("2", null, "Wait", null, "\"2 , 5\"", null, null));
		steps.add(newStep("3", null, "Wait", "\"33\"", "\"44\"", null, null));

		List<SeqStep> expectedSteps = new ArrayList<>();
		expectedSteps.add(newSeqStep("1", null, "Wait", null, "\"2\"", null, null));
		expectedSteps.add(newSeqStep("2", null, "Wait", null, "\"2 , 5\"", null, null));
		expectedSteps.add(newSeqStep("3", null, "Wait", "\"33\"", "\"44\"", null, null));

		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), is(empty()));
		assertEquals(expectedSteps, transform.getTransformedData());
	}

	@Test
	public void testTransformWaitStepInvalidSyntaxNoParam() {
		List<Map<String, Map<String, String>>> steps = new ArrayList<>();
		steps.add(newStep("1", null, "Wait", null, null, null, null));

		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), hasSize(1));
		assertEquals(transform.getErrors().get(0).getMessage(), "Parameter required for 'Wait'.");
		assertEquals(new ArrayList<>(), transform.getTransformedData());
	}

	@Test
	public void testTransformWaitStepInvalidArg() {
		List<Map<String, Map<String, String>>> steps = new ArrayList<>();
		steps.add(newStep("1", null, "Wait", "\"\"", "12", null, null));

		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), hasSize(1));
		assertEquals(transform.getErrors().get(0).getMessage(), "Parameter required for 'Wait'.");
		assertEquals(new ArrayList<>(), transform.getTransformedData());
	}

	@Test
	public void testTransformWaitStepInappropriateArg() {
		List<Map<String, Map<String, String>>> steps = new ArrayList<>();
		steps.add(newStep("1", null, "Wait", "\"-1\"", "1", null, null));
		steps.add(newStep("2", null, "Wait", "\"100000\"", "1", null, null));

		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), hasSize(2));
		assertEquals(transform.getErrors().get(0).getMessage(),
				"'Wait' parameter is invalid. It should be between 1 and 86400.");
		assertEquals(transform.getErrors().get(1).getMessage(),
				"'Wait' parameter is invalid. It should be between 1 and 86400.");
		assertEquals(new ArrayList<>(), transform.getTransformedData());
	}

	@Test
	public void testTransformSimpleWithSysProps() {
		List<Map<String, Map<String, String>>> steps = asList(
				newStep("1", "Browser", "Set", "${get_sp('step_weather')}", "weather", null, "1234"),
				newStep("2", "Browser", "Click", "", "www.google.com", "snapshot", "1234"),
				newStep("3", "Browser", "Click", "${get_sp('title')}", "", null, "1234"));
		Set<String> systemProperties = newHashSet("step_weather", "title");

		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		assertThat(transform.getErrors(), is(empty()));
		Set<String> systemPropertiesForOperation = dependenciesHelper.getSystemPropertiesForOperation(new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), transform.getTransformedData());
		assertEquals(systemPropertiesForOperation, systemProperties);
	}

	@Test
	public void testTransformSimpleWithInvalidSysProperty() {
		List<Map<String, Map<String, String>>> steps = asList(
				newStep("1", "Browser", "Set", "${get_sp('step_weather')}", "weather", null, "1234"),
				newStep("2", "Browser", "Click", "${get_ssp('title')}", "", null, "1234"));

		TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

		Set<String> systemPropertiesForOperation = dependenciesHelper.getSystemPropertiesForOperation(new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), transform.getTransformedData());
		assertEquals(systemPropertiesForOperation, Sets.newHashSet("step_weather"));
	}

	private Map<String, Map<String, String>> newStep(String id, String objPath, String action, String args,
			String defaultArgs, String snapshot, String highlightId) {
		Map<String, String> stepDetails = new HashMap<>();

		putIfValueNotNull(stepDetails, SEQ_STEP_ID_KEY, id);
		putIfValueNotNull(stepDetails, SEQ_STEP_PATH_KEY, objPath);
		putIfValueNotNull(stepDetails, SEQ_STEP_ACTION_KEY, action);
		putIfValueNotNull(stepDetails, SEQ_STEP_ARGS_KEY, args);
		putIfValueNotNull(stepDetails, SEQ_STEP_DEFAULT_ARGS_KEY, defaultArgs);
		putIfValueNotNull(stepDetails, SEQ_STEP_SNAPSHOT_KEY, snapshot);
		putIfValueNotNull(stepDetails, SEQ_STEP_HIGHLIGHT_ID_KEY, highlightId);

		Map<String, Map<String, String>> step = new HashMap<>();
		step.put("step", stepDetails);

		return step;
	}

	private SeqStep newSeqStep(String id, String objPath, String action, String args, String defaultArgs,
			String snapshot, String highlightId) {
		SeqStep seqStep = new SeqStep();

		seqStep.setId(id);
		seqStep.setObjectPath(objPath);
		seqStep.setAction(action);
		seqStep.setArgs(args);
		seqStep.setDefaultArgs(defaultArgs);
		seqStep.setSnapshot(snapshot);
		seqStep.setHighlightId(highlightId);

		return seqStep;
	}

	private void putIfValueNotNull(Map<String, String> stepDetails, String key, String value) {
		if (value != null) {
			stepDetails.put(key, value);
		}
	}

	public static class Config {

		@Bean
		public SeqStepsTransformer seqStepsTransformer() {
			return new SeqStepsTransformer();
		}

		@Bean
		public DependenciesHelper dependenciesHelper() {
			return new DependenciesHelper();
		}
	}
}
