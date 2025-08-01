/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;

/*
 * Created by orius123 on 05/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompileDependenciesTest {

	@Autowired
	private SlangCompiler compiler;

	@Test(expected = RuntimeException.class)
	public void emptyPathButThereAreImports() throws Exception {
		final URI flow = getClass().getResource("/basic_flow.yaml").toURI();
		Set<SlangSource> path = new HashSet<>();
		compiler.compile(SlangSource.fromFile(flow), path);
	}

	@Test
	public void referenceDoesNoExistInPath() throws Exception {
		final URI flow = getClass().getResource("/basic_flow.yaml").toURI();
		final URI operation = getClass().getResource("/operation_with_data.sl").toURI();
		Set<SlangSource> path = new HashSet<>();
		path.add(SlangSource.fromFile(operation));

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> compiler.compile(SlangSource.fromFile(flow), path));
		Assert.assertEquals("Reference: 'user.ops.test_op' in executable: 'basic_flow', wasn't found in path",
				exception.getMessage());
	}

	@Test
	public void importHasAKeyThatDoesNotExistInPath() throws Exception {
		final URI flow = getClass().getResource("/basic_flow.yaml").toURI();
		final URI operation = getClass().getResource("/flow_with_data.yaml").toURI();
		Set<SlangSource> path = new HashSet<>();
		path.add(SlangSource.fromFile(operation));

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> compiler.compile(SlangSource.fromFile(flow), path));
		Assert.assertEquals("Reference: 'user.ops.test_op' in executable: 'basic_flow', wasn't found in path",
				exception.getMessage());
	}

	@Test
	public void filesThatAreNotImportedShouldNotBeCompiled() throws Exception {
		final URI flow = getClass().getResource("/basic_flow.yaml").toURI();
		final URI notImportedOperation = getClass().getResource("/flow_with_data.yaml").toURI();
		final URI importedOperation = getClass().getResource("/test_op.sl").toURI();
		final URI importedOperation2 = getClass().getResource("/check_Weather.sl").toURI();
		Set<SlangSource> path = new HashSet<>();
		path.add(SlangSource.fromFile(notImportedOperation));
		path.add(SlangSource.fromFile(importedOperation));
		path.add(SlangSource.fromFile(importedOperation2));

		CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
		assertThat(compilationArtifact.getDependencies(), Matchers.hasKey("user.ops.test_op"));
		assertThat(compilationArtifact.getDependencies(), not(Matchers.hasKey("slang.sample.flows.SimpleFlow")));
	}

	@Test
	public void sourceFileIsADirectory() throws Exception {
		final URI dir = getClass().getResource("/").toURI();

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> compiler.compile(SlangSource.fromFile(dir), null));
		Assert.assertEquals("File content: test-classes doesn't lead to a file, directories are not supported",
				exception.getMessage());
	}

	@Test
	public void subFlowRefId() throws Exception {
		final URI flow = getClass().getResource("/circular-dependencies/parent_flow.yaml").toURI();
		final URI childFlow = getClass().getResource("/circular-dependencies/child_flow.yaml").toURI();
		final URI operation = getClass().getResource("/test_op.sl").toURI();
		Set<SlangSource> path = new HashSet<>();
		path.add(SlangSource.fromFile(childFlow));
		path.add(SlangSource.fromFile(operation));

		CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
		ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
		Assert.assertNotNull(executionPlan);
		assertEquals("different number of dependencies than expected", 2, compilationArtifact.getDependencies().size());
		ExecutionStep secondStepStartExecutionStep = executionPlan.getStep(7L);
		String refId = (String) secondStepStartExecutionStep.getActionData().get(ScoreLangConstants.REF_ID);
		assertEquals("refId is not as expected", "user.flows.circular.child_flow", refId);
	}

	@Test
	public void bothFileAreDependentOnTheSameFile() throws Exception {
		final URI flow = getClass().getResource("/circular-dependencies/parent_flow.yaml").toURI();
		final URI childFlow = getClass().getResource("/circular-dependencies/child_flow.yaml").toURI();
		final URI operation = getClass().getResource("/test_op.sl").toURI();
		Set<SlangSource> path = new HashSet<>();
		path.add(SlangSource.fromFile(childFlow));
		path.add(SlangSource.fromFile(operation));
		CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
		ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
		Assert.assertNotNull(executionPlan);
		assertEquals("different number of dependencies than expected", 2, compilationArtifact.getDependencies().size());
	}

	@Test
	public void circularDependencies() throws Exception {
		final URI flow = getClass().getResource("/circular-dependencies/circular_parent_flow.yaml").toURI();
		final URI childFlow = getClass().getResource("/circular-dependencies/circular_child_flow.yaml").toURI();
		final URI operation = getClass().getResource("/test_op.sl").toURI();
		Set<SlangSource> path = new HashSet<>();
		path.add(SlangSource.fromFile(childFlow));
		path.add(SlangSource.fromFile(operation));
		CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
		ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
		Assert.assertNotNull(executionPlan);
		assertEquals(3, compilationArtifact.getDependencies().size());
	}

	@Test
	public void sameSourceAsDependencyWorks() throws Exception {
		final URI flow = getClass().getResource("/basic_flow.yaml").toURI();
		final URI importedOperation = getClass().getResource("/test_op.sl").toURI();
		Set<SlangSource> path = new HashSet<>();
		path.add(SlangSource.fromFile(importedOperation));
		SlangSource flowSource = SlangSource.fromFile(flow);
		path.add(flowSource);

		Assert.assertTrue(path.contains(flowSource));
		CompilationArtifact compilationArtifact = compiler.compile(flowSource, path);
		Assert.assertNotNull(compilationArtifact);
	}

	@Test
	public void testCompileWorksForSingleSelfReference() throws Exception {
		final URI executable = getClass().getResource("/cornercases/selfreference.sl").toURI();
		SlangSource executableSource = SlangSource.fromFile(executable);

		CompilationArtifact compilationArtifact = compiler.compile(executableSource, new HashSet<SlangSource>());
		Assert.assertNotNull(compilationArtifact);
	}

}