package com.algocrafts.jbehave;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import org.jbehave.core.Embeddable;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.FilePrintStreamFactory;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.SilentStepMonitor;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.*;

public class AllStoryRunner extends JUnitStories {

	private static Paranamer paranamer = new CachingParanamer(new BytecodeReadingParanamer());

	private final Object[] steps;

	public AllStoryRunner() {
		steps = new Object[] { new ChangeLocationStep(), new SelectCalendarStep() };
	}

	@Override
	public Configuration configuration() {
		Class<? extends Embeddable> embeddableClass = this.getClass();
		Properties viewResources = new Properties();
		viewResources.put("decorateNonHtml", "true");
		return new MostUsefulConfiguration().useParanamer(paranamer)
				.useStoryLoader(new LoadFromClasspath(embeddableClass))
				.useStoryPathResolver(new UnderscoredCamelCaseResolver().removeFromClassName("Story"))
				.useStoryReporterBuilder(new StoryReporterBuilder()
						.withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass)).withDefaultFormats()
						.withPathResolver(new FilePrintStreamFactory.ResolveToPackagedName())
						.withViewResources(viewResources).withFormats(CONSOLE, TXT, HTML, XML))
				.useParameterConverters(
						new ParameterConverters().addConverters(new ParameterConverters.NumberConverter(),
								new DateConverter(new SimpleDateFormat("yyyy-MM-dd")))) // use custom date pattern
				.useStepPatternParser(new RegexPrefixCapturingPatternParser("$")) // use '%' instead of '$' to identify parameters
				.useStepMonitor(new SilentStepMonitor());
	}

	@Override
	protected List<String> storyPaths() {
		return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()).getFile(), asList("**/*.story"),
				asList(""));
	}

	@Override
	public List<CandidateSteps> candidateSteps() {
		return new InstanceStepsFactory(configuration(), steps).createCandidateSteps();
	}

}