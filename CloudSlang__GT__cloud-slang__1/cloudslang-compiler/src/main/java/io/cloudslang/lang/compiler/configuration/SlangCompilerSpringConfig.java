/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.configuration;

import static io.cloudslang.lang.compiler.SlangTextualKeys.OBJECT_REPOSITORY_KEY;

import com.google.common.collect.Lists;
import configuration.SlangEntitiesSpringConfig;
import io.cloudslang.lang.compiler.MetadataExtractor;
import io.cloudslang.lang.compiler.MetadataExtractorImpl;
import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangCompilerImpl;
import io.cloudslang.lang.compiler.caching.CachedPrecompileService;
import io.cloudslang.lang.compiler.caching.CachedPrecompileServiceImpl;
import io.cloudslang.lang.compiler.modeller.DependenciesHelper;
import io.cloudslang.lang.compiler.modeller.ExecutableBuilder;
import io.cloudslang.lang.compiler.modeller.MetadataModeller;
import io.cloudslang.lang.compiler.modeller.MetadataModellerImpl;
import io.cloudslang.lang.compiler.modeller.SlangModeller;
import io.cloudslang.lang.compiler.modeller.SlangModellerImpl;
import io.cloudslang.lang.compiler.modeller.TransformersHandler;
import io.cloudslang.lang.compiler.modeller.transformers.AbstractForTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.AbstractInputsTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.AbstractOutputsTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.BreakTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.DependencyFormatValidator;
import io.cloudslang.lang.compiler.modeller.transformers.DoExternalTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.DoTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.ForTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.InputsTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.JavaActionTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.NavigateTransformer;
import io.cloudslang.lang.compiler.modeller.SystemPropertiesHelper;
import io.cloudslang.lang.compiler.modeller.transformers.OutputsTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.ParallelLoopForTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.PublishTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.PythonActionTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.ResultsTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.RobotGroupTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.SeqActionTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.SeqStepsTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.compiler.modeller.transformers.WorkFlowTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.WorkerGroupTransformer;
import io.cloudslang.lang.compiler.parser.MetadataParser;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.MetadataValidator;
import io.cloudslang.lang.compiler.parser.utils.MetadataValidatorImpl;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.scorecompiler.DefaultExternalExecutionStepFactory;
import io.cloudslang.lang.compiler.scorecompiler.ExecutionPlanBuilder;
import io.cloudslang.lang.compiler.scorecompiler.ExecutionStepFactory;
import io.cloudslang.lang.compiler.scorecompiler.ExternalExecutionStepFactory;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompilerImpl;
import io.cloudslang.lang.compiler.validator.CompileValidator;
import io.cloudslang.lang.compiler.validator.CompileValidatorImpl;
import io.cloudslang.lang.compiler.validator.DefaultExternalExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.ExternalPythonScriptValidator;
import io.cloudslang.lang.compiler.validator.ExternalPythonScriptValidatorImpl;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import io.cloudslang.lang.entities.encryption.DummyEncryptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.util.List;

@Configuration
@ComponentScan("io.cloudslang.lang.compiler")
@Import(SlangEntitiesSpringConfig.class)
public class SlangCompilerSpringConfig {

	private static final String OBJECT_REPOSITORY_CAMEL_CASE = "objectRepository";

	@Bean
	@Scope("prototype")
	public Yaml yaml() {
		Yaml yaml = new Yaml(getConstructor());
		yaml.setBeanAccess(BeanAccess.FIELD);
		return yaml;
	}

	@Bean
	public DummyEncryptor dummyEncryptor() {
		return new DummyEncryptor();
	}

	@Bean
	public YamlParser yamlParser() {
		YamlParser yamlParser = new YamlParser() {
			@Override
			public Yaml getYaml() {
				return yaml();
			}
		};
		yamlParser.setExecutableValidator(executableValidator());
		yamlParser.setParserExceptionHandler(parserExceptionHandler());
		return yamlParser;
	}

	@Bean
	public CachedPrecompileService cachedPrecompileService() {
		return new CachedPrecompileServiceImpl();
	}

	@Bean
	public PreCompileValidator precompileValidator() {
		PreCompileValidatorImpl preCompileValidator = new PreCompileValidatorImpl();
		preCompileValidator.setExecutableValidator(executableValidator());
		return preCompileValidator;
	}

	@Bean
	public ExecutableValidator executableValidator() {
		ExecutableValidatorImpl executableValidator = new ExecutableValidatorImpl();
		executableValidator.setSystemPropertyValidator(systemPropertyValidator());
		return executableValidator;
	}

	@Bean
	public PreCompileValidator externalPrecompileValidator() {
		PreCompileValidatorImpl preCompileValidator = new PreCompileValidatorImpl();
		preCompileValidator.setExecutableValidator(externalExecutableValidator());
		return preCompileValidator;
	}

	@Bean
	public ExecutableValidator externalExecutableValidator() {
		return new DefaultExternalExecutableValidator();
	}

	@Bean
	public ExecutionPlanBuilder executionPlanBuilder() {
		ExecutionPlanBuilder executionPlanBuilder = new ExecutionPlanBuilder();
		executionPlanBuilder.setStepFactory(stepFactory());
		executionPlanBuilder.setExternalStepFactory(externalStepFactory());
		return executionPlanBuilder;
	}

	@Bean
	public ExecutionStepFactory stepFactory() {
		return new ExecutionStepFactory();
	}

	@Bean
	public ExternalExecutionStepFactory externalStepFactory() {
		return new DefaultExternalExecutionStepFactory();
	}

	@Bean
	public SystemPropertyValidator systemPropertyValidator() {
		return new SystemPropertyValidatorImpl();
	}

	@Bean
	public CompileValidator compileValidator() {
		return new CompileValidatorImpl();
	}

	@Bean
	public DependenciesHelper dependenciesHelper() {
		DependenciesHelper dependenciesHelper = new DependenciesHelper();
		dependenciesHelper.setPublishTransformer(publishTransformer());
		return dependenciesHelper;
	}

	@Bean
	public DependencyFormatValidator dependencyFormatValidator() {
		return new DependencyFormatValidator();
	}

	@Bean
	public ExternalPythonScriptValidator externalPythonScriptValidator() {
		return new ExternalPythonScriptValidatorImpl();
	}

	@Bean
	public MetadataExtractor metadataExtractor() {
		MetadataExtractorImpl metadataExtractor = new MetadataExtractorImpl();
		metadataExtractor.setMetadataModeller(metadataModeller());
		metadataExtractor.setMetadataParser(metadataParser());
		metadataExtractor.setMetadataValidator(metadataValidator());
		return metadataExtractor;
	}

	@Bean
	public MetadataParser metadataParser() {
		MetadataParser metadataParser = new MetadataParser();
		metadataParser.setParserExceptionHandler(parserExceptionHandler());
		return metadataParser;
	}

	@Bean
	public MetadataValidator metadataValidator() {
		MetadataValidatorImpl metadataValidator = new MetadataValidatorImpl();
		metadataValidator.setMetadataParser(metadataParser());
		return metadataValidator;
	}

	@Bean
	public ParserExceptionHandler parserExceptionHandler() {
		return new ParserExceptionHandler();
	}

	@Bean
	public MetadataModeller metadataModeller() {
		return new MetadataModellerImpl();
	}

	@Bean
	public ScoreCompiler scoreCompiler() {
		ScoreCompilerImpl scoreCompiler = new ScoreCompilerImpl();
		scoreCompiler.setCompileValidator(compileValidator());
		scoreCompiler.setDependenciesHelper(dependenciesHelper());
		scoreCompiler.setExecutionPlanBuilder(executionPlanBuilder());

		return scoreCompiler;
	}

	@Bean
	public SlangCompiler slangCompiler() {
		SlangCompilerImpl slangCompiler = new SlangCompilerImpl();

		slangCompiler.setCachedPrecompileService(cachedPrecompileService());
		slangCompiler.setCompileValidator(compileValidator());
		slangCompiler.setScoreCompiler(scoreCompiler());
		slangCompiler.setSlangModeller(slangModeller());
		slangCompiler.setSystemPropertyValidator(systemPropertyValidator());
		slangCompiler.setYamlParser(yamlParser());
		slangCompiler.setMetadataExtractor(metadataExtractor());

		return slangCompiler;
	}

	@Bean
	public SlangModeller slangModeller() {
		SlangModellerImpl slangModeller = new SlangModellerImpl();
		slangModeller.setExecutableBuilder(executableBuilder());
		return slangModeller;
	}

	@Bean
	public PublishTransformer publishTransformer() {
		PublishTransformer publishTransformer = new PublishTransformer();
		setAbstractOutputTransformerDependencies(publishTransformer);

		return publishTransformer;
	}

	@Bean
	public PublishTransformer externalPublishTransformer() {
		PublishTransformer externalPublish = new PublishTransformer();
		setAbstractOutputTransformerDependencies(externalPublish);
		externalPublish.setType(Transformer.Type.EXTERNAL);

		return externalPublish;
	}

	@Bean
	public OutputsTransformer outputsTransformer() {
		OutputsTransformer outputsTransformer = new OutputsTransformer();
		setAbstractOutputTransformerDependencies(outputsTransformer);

		return outputsTransformer;
	}

	@Bean
	public PythonActionTransformer pythonActionTransformer() {
		PythonActionTransformer pythonActionTransformer = new PythonActionTransformer();
		pythonActionTransformer.setDependencyFormatValidator(dependencyFormatValidator());
		pythonActionTransformer.setExternalPythonScriptValidator(externalPythonScriptValidator());

		return pythonActionTransformer;
	}

	@Bean
	public SeqActionTransformer seqActionTransformer() {
		return new SeqActionTransformer(dependencyFormatValidator(), precompileValidator(), seqStepsTransformer());
	}

	@Bean
	public SystemPropertiesHelper systemPropertiesHelper() {
		return new SystemPropertiesHelper();
	}

	@Bean
	public SeqStepsTransformer seqStepsTransformer() {
		return new SeqStepsTransformer();
	}

	@Bean
	public BreakTransformer breakTransformer() {
		BreakTransformer breakTransformer = new BreakTransformer();
		breakTransformer.setExecutableValidator(executableValidator());

		return breakTransformer;
	}

	@Bean
	public NavigateTransformer navigateTransformer() {
		final NavigateTransformer navigateTransformer = new NavigateTransformer();
		navigateTransformer.setExecutableValidator(executableValidator());
		return navigateTransformer;
	}

	@Bean
	public NavigateTransformer externalNavigateTransformer() {
		final NavigateTransformer externalNavigate = new NavigateTransformer();
		externalNavigate.setExecutableValidator(externalExecutableValidator());
		externalNavigate.setType(Transformer.Type.EXTERNAL);
		return externalNavigate;
	}

	@Bean
	public DoTransformer doTransformer() {
		DoTransformer doTransformer = new DoTransformer();
		doTransformer.setPreCompileValidator(precompileValidator());
		doTransformer.setExecutableValidator(executableValidator());

		return doTransformer;
	}

	@Bean
	public DoExternalTransformer doExternalTransformer() {
		DoExternalTransformer doExternalTransformer = new DoExternalTransformer();
		doExternalTransformer.setPreCompileValidator(externalPrecompileValidator());
		doExternalTransformer.setExecutableValidator(externalExecutableValidator());

		return doExternalTransformer;
	}

	@Bean
	public ResultsTransformer resultsTransformer() {
		ResultsTransformer resultsTransformer = new ResultsTransformer();
		resultsTransformer.setPreCompileValidator(precompileValidator());
		resultsTransformer.setExecutableValidator(executableValidator());

		return resultsTransformer;
	}

	@Bean
	public WorkFlowTransformer workFlowTransformer() {
		return new WorkFlowTransformer();
	}

	@Bean
	public InputsTransformer inputsTransformer() {
		InputsTransformer inputsTransformer = new InputsTransformer();
		setAbstractInputTransformerDependencies(inputsTransformer);

		return inputsTransformer;
	}

	@Bean
	public JavaActionTransformer javaActionTransformer() {
		JavaActionTransformer javaActionTransformer = new JavaActionTransformer();
		javaActionTransformer.setDependencyFormatValidator(dependencyFormatValidator());
		return javaActionTransformer;
	}

	@Bean
	public ParallelLoopForTransformer parallelLoopForTransformer() {
		ParallelLoopForTransformer parallelLoopForTransformer = new ParallelLoopForTransformer();

		setExecutableValidator(parallelLoopForTransformer);
		return parallelLoopForTransformer;
	}

	@Bean
	public ForTransformer forTransformer() {
		ForTransformer forTransformer = new ForTransformer();
		setExecutableValidator(forTransformer);

		return forTransformer;
	}

	@Bean
	public WorkerGroupTransformer workerGroupTransformer() {
		return new WorkerGroupTransformer();
	}

	@Bean
	public RobotGroupTransformer robotGroupTransformer() {
		return new RobotGroupTransformer();
	}

	@Bean
	public ExecutableBuilder executableBuilder() {
		ExecutableBuilder executableBuilder = new ExecutableBuilder();
		executableBuilder.setTransformers(transformers());
		executableBuilder.setTransformersHandler(transformersHandler());
		executableBuilder.setDependenciesHelper(dependenciesHelper());
		executableBuilder.setPreCompileValidator(precompileValidator());
		executableBuilder.setResultsTransformer(resultsTransformer());
		executableBuilder.setExecutableValidator(executableValidator());
		executableBuilder.setSystemPropertiesHelper(systemPropertiesHelper());

		executableBuilder.initScopedTransformersAndKeys();
		return executableBuilder;
	}

	private void setAbstractInputTransformerDependencies(AbstractInputsTransformer abstractInputsTransformer) {
		abstractInputsTransformer.setExecutableValidator(executableValidator());
		abstractInputsTransformer.setPreCompileValidator(precompileValidator());
	}

	@Bean
	public TransformersHandler transformersHandler() {
		return new TransformersHandler();
	}

	@Bean
	public List<Transformer> transformers() {
		return Lists.newArrayList(pythonActionTransformer(), parallelLoopForTransformer(), publishTransformer(),
				externalPublishTransformer(), navigateTransformer(), externalNavigateTransformer(), inputsTransformer(),
				workFlowTransformer(), resultsTransformer(), doTransformer(), doExternalTransformer(),
				outputsTransformer(), javaActionTransformer(), forTransformer(), breakTransformer(),
				seqActionTransformer(), workerGroupTransformer(), robotGroupTransformer());
	}

	private Constructor getConstructor() {
		Constructor constructor = new Constructor(ParsedSlang.class);
		constructor.setPropertyUtils(new PropertyUtils() {
			@Override
			public Property getProperty(Class<? extends Object> type, String name) {
				if (name.equals(OBJECT_REPOSITORY_KEY)) {
					name = OBJECT_REPOSITORY_CAMEL_CASE;
				}
				return super.getProperty(type, name);
			}
		});
		return constructor;
	}

	private void setAbstractOutputTransformerDependencies(AbstractOutputsTransformer abstractOutputsTransformer) {
		abstractOutputsTransformer.setPreCompileValidator(precompileValidator());
		abstractOutputsTransformer.setExecutableValidator(executableValidator());
	}

	private void setExecutableValidator(AbstractForTransformer abstractForTransformer) {
		abstractForTransformer.setExecutableValidator(executableValidator());
	}

}
