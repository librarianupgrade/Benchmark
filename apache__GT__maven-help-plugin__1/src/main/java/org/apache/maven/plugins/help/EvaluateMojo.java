package org.apache.maven.plugins.help;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.lang3.ClassUtils;
import org.apache.maven.lifecycle.internal.MojoDescriptorCreator;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.apache.maven.shared.artifact.ArtifactCoordinate;
import org.apache.maven.shared.artifact.resolve.ArtifactResolverException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.components.interactivity.InputHandler;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.PropertiesConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Evaluates Maven expressions given by the user in an interactive mode.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @since 2.1
 */
@Mojo(name = "evaluate", requiresProject = false)
public class EvaluateMojo extends AbstractHelpMojo {
	// ----------------------------------------------------------------------
	// Mojo components
	// ----------------------------------------------------------------------

	/**
	 * Input handler, needed for command line handling.
	 */
	@Component
	private InputHandler inputHandler;

	/**
	 * Component used to get mojo descriptors.
	 */
	@Component
	private MojoDescriptorCreator mojoDescriptorCreator;

	// ----------------------------------------------------------------------
	// Mojo parameters
	// ----------------------------------------------------------------------

	// we need to hide the 'output' defined in AbstractHelpMojo to have a correct "since".
	/**
	 * Optional parameter to write the output of this help in a given file, instead of writing to the console.
	 * This parameter will be ignored if no <code>expression</code> is specified.
	 * <br/>
	 * <b>Note</b>: Could be a relative path.
	 * 
	 * @since 3.0.0
	 */
	@Parameter(property = "output")
	private File output;

	/**
	 * This options gives the option to output information in cases where the output has been suppressed by using
	 * <code>-q</code> (quiet option) in Maven. This is useful if you like to use
	 * <code>maven-help-plugin:evaluate</code> in a script call (for example in bash) like this:
	 * 
	 * <pre>
	 * RESULT=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
	 * echo $RESULT
	 * </pre>
	 * 
	 * This will only printout the information which has been requested by <code>expression</code> to
	 * <code>stdout</code>.
	 * 
	 * @since 3.1.0
	 */
	@Parameter(property = "forceStdout", defaultValue = "false")
	private boolean forceStdout;

	/**
	 * An artifact for evaluating Maven expressions. <br/>
	 * <b>Note</b>: Should respect the Maven format, i.e. <code>groupId:artifactId[:version]</code>. The latest version
	 * of the artifact will be used when no version is specified.
	 */
	@Parameter(property = "artifact")
	private String artifact;

	/**
	 * An expression to evaluate instead of prompting. Note that this <i>must not</i> include the surrounding ${...}.
	 */
	@Parameter(property = "expression")
	private String expression;

	/**
	 * Maven project built from the given {@link #artifact}. Otherwise, the current Maven project or the super pom.
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	/**
	 * The system settings for Maven.
	 */
	@Parameter(defaultValue = "${settings}", readonly = true, required = true)
	private Settings settings;

	// ----------------------------------------------------------------------
	// Instance variables
	// ----------------------------------------------------------------------

	/** lazy loading evaluator variable */
	private PluginParameterExpressionEvaluator evaluator;

	/** lazy loading xstream variable */
	private XStream xstream;

	// ----------------------------------------------------------------------
	// Public methods
	// ----------------------------------------------------------------------

	/** {@inheritDoc} */
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (expression == null && !settings.isInteractiveMode()) {

			getLog().error("Maven is configured to NOT interact with the user for input. "
					+ "This Mojo requires that 'interactiveMode' in your settings file is flag to 'true'.");
			return;
		}

		validateParameters();

		if (StringUtils.isNotEmpty(artifact)) {
			project = getMavenProject(artifact);
		}

		if (expression == null) {
			if (output != null) {
				getLog().warn(
						"When prompting for input, the result will be written to the console, " + "ignoring 'output'.");
			}
			while (true) {
				getLog().info("Enter the Maven expression i.e. ${project.groupId} or 0 to exit?:");

				try {
					String userExpression = inputHandler.readLine();
					if (userExpression == null || userExpression.toLowerCase(Locale.ENGLISH).equals("0")) {
						break;
					}

					handleResponse(userExpression, null);
				} catch (IOException e) {
					throw new MojoExecutionException("Unable to read from standard input.", e);
				}
			}
		} else {
			handleResponse("${" + expression + "}", output);
		}
	}

	// ----------------------------------------------------------------------
	// Private methods
	// ----------------------------------------------------------------------

	/**
	 * Validate Mojo parameters.
	 */
	private void validateParameters() {
		if (artifact == null) {
			// using project if found or super-pom
			getLog().info("No artifact parameter specified, using '" + project.getId() + "' as project.");
		}
	}

	/**
	 * @return a lazy loading evaluator object.
	 * @throws MojoExecutionException if any
	 * @throws MojoFailureException if any reflection exceptions occur or missing components.
	 */
	private PluginParameterExpressionEvaluator getEvaluator() throws MojoExecutionException, MojoFailureException {
		if (evaluator == null) {
			MojoDescriptor mojoDescriptor;
			try {
				mojoDescriptor = mojoDescriptorCreator.getMojoDescriptor("help:evaluate", session, project);
			} catch (Exception e) {
				throw new MojoFailureException("Failure while evaluating.", e);
			}
			MojoExecution mojoExecution = new MojoExecution(mojoDescriptor);

			MavenProject currentProject = session.getCurrentProject();
			// Maven 3: PluginParameterExpressionEvaluator gets the current project from the session:
			// synchronize in case another thread wants to fetch the real current project in between
			synchronized (session) {
				session.setCurrentProject(project);
				evaluator = new PluginParameterExpressionEvaluator(session, mojoExecution);
				session.setCurrentProject(currentProject);
			}
		}

		return evaluator;
	}

	/**
	 * @param expr the user expression asked.
	 * @param output the file where to write the result, or <code>null</code> to print in standard output.
	 * @throws MojoExecutionException if any
	 * @throws MojoFailureException if any reflection exceptions occur or missing components.
	 */
	private void handleResponse(String expr, File output) throws MojoExecutionException, MojoFailureException {
		StringBuilder response = new StringBuilder();

		Object obj;
		try {
			obj = getEvaluator().evaluate(expr);
		} catch (ExpressionEvaluationException e) {
			throw new MojoExecutionException("Error when evaluating the Maven expression", e);
		}

		if (obj != null && expr.equals(obj.toString())) {
			getLog().warn("The Maven expression was invalid. Please use a valid expression.");
			return;
		}

		// handle null
		if (obj == null) {
			response.append("null object or invalid expression");
		}
		// handle primitives objects
		else if (obj instanceof String) {
			response.append(obj.toString());
		} else if (obj instanceof Boolean) {
			response.append(obj.toString());
		} else if (obj instanceof Byte) {
			response.append(obj.toString());
		} else if (obj instanceof Character) {
			response.append(obj.toString());
		} else if (obj instanceof Double) {
			response.append(obj.toString());
		} else if (obj instanceof Float) {
			response.append(obj.toString());
		} else if (obj instanceof Integer) {
			response.append(obj.toString());
		} else if (obj instanceof Long) {
			response.append(obj.toString());
		} else if (obj instanceof Short) {
			response.append(obj.toString());
		}
		// handle specific objects
		else if (obj instanceof File) {
			File f = (File) obj;
			response.append(f.getAbsolutePath());
		}
		// handle Maven pom object
		else if (obj instanceof MavenProject) {
			MavenProject projectAsked = (MavenProject) obj;
			StringWriter sWriter = new StringWriter();
			MavenXpp3Writer pomWriter = new MavenXpp3Writer();
			try {
				pomWriter.write(sWriter, projectAsked.getModel());
			} catch (IOException e) {
				throw new MojoExecutionException("Error when writing pom", e);
			}

			response.append(sWriter.toString());
		}
		// handle Maven Settings object
		else if (obj instanceof Settings) {
			Settings settingsAsked = (Settings) obj;
			StringWriter sWriter = new StringWriter();
			SettingsXpp3Writer settingsWriter = new SettingsXpp3Writer();
			try {
				settingsWriter.write(sWriter, settingsAsked);
			} catch (IOException e) {
				throw new MojoExecutionException("Error when writing settings", e);
			}

			response.append(sWriter.toString());
		} else {
			// others Maven objects
			response.append(toXML(expr, obj));
		}

		if (output != null) {
			try {
				writeFile(output, response);
			} catch (IOException e) {
				throw new MojoExecutionException("Cannot write evaluation of expression to output: " + output, e);
			}
			getLog().info("Result of evaluation written to: " + output);
		} else {
			if (getLog().isInfoEnabled()) {
				getLog().info(LS + response.toString());
			} else {
				if (forceStdout) {
					System.out.print(response.toString());
					System.out.flush();
				}
			}
		}
	}

	/**
	 * @param expr the user expression.
	 * @param obj a not null.
	 * @return the XML for the given object.
	 */
	private String toXML(String expr, Object obj) {
		XStream currentXStream = getXStream();

		// beautify list
		if (obj instanceof List) {
			List<?> list = (List<?>) obj;
			if (list.size() > 0) {
				Object elt = list.iterator().next();

				String name = StringUtils.lowercaseFirstLetter(elt.getClass().getSimpleName());
				currentXStream.alias(pluralize(name), List.class);
			} else {
				// try to detect the alias from question
				if (expr.indexOf('.') != -1) {
					String name = expr.substring(expr.indexOf('.') + 1, expr.indexOf('}'));
					currentXStream.alias(name, List.class);
				}
			}
		}

		return currentXStream.toXML(obj);
	}

	/**
	 * @return lazy loading xstream object.
	 */
	private XStream getXStream() {
		if (xstream == null) {
			xstream = new XStream();
			addAlias(xstream);

			// handle Properties a la Maven
			xstream.registerConverter(new PropertiesConverter() {
				/** {@inheritDoc} */
				public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
					return Properties.class == type;
				}

				/** {@inheritDoc} */
				public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
					Properties properties = (Properties) source;
					Map<?, ?> map = new TreeMap<Object, Object>(properties); // sort
					for (Map.Entry<?, ?> entry : map.entrySet()) {
						writer.startNode(entry.getKey().toString());
						writer.setValue(entry.getValue().toString());
						writer.endNode();
					}
				}
			});
		}

		return xstream;
	}

	/**
	 * @param xstreamObject not null
	 */
	private void addAlias(XStream xstreamObject) {
		try {
			addAlias(xstreamObject, getMavenModelJarFile(), "org.apache.maven.model");
			addAlias(xstreamObject, getMavenSettingsJarFile(), "org.apache.maven.settings");
		} catch (MojoExecutionException e) {
			if (getLog().isDebugEnabled()) {
				getLog().debug("MojoExecutionException: " + e.getMessage(), e);
			}
		} catch (ArtifactResolverException e) {
			if (getLog().isDebugEnabled()) {
				getLog().debug("ArtifactResolverException: " + e.getMessage(), e);
			}
		} catch (ProjectBuildingException e) {
			if (getLog().isDebugEnabled()) {
				getLog().debug("ProjectBuildingException: " + e.getMessage(), e);
			}
		}

		// TODO need to handle specific Maven objects like DefaultArtifact?
	}

	/**
	 * @param xstreamObject not null
	 * @param jarFile not null
	 * @param packageFilter a package name to filter.
	 */
	private void addAlias(XStream xstreamObject, File jarFile, String packageFilter) {
		JarInputStream jarStream = null;
		try {
			jarStream = new JarInputStream(new FileInputStream(jarFile));
			for (JarEntry jarEntry = jarStream.getNextJarEntry(); jarEntry != null; jarEntry = jarStream
					.getNextJarEntry()) {
				if (jarEntry.getName().toLowerCase(Locale.ENGLISH).endsWith(".class")) {
					String name = jarEntry.getName().substring(0, jarEntry.getName().indexOf("."));
					name = name.replaceAll("/", "\\.");

					if (name.contains(packageFilter)) {
						try {
							Class<?> clazz = ClassUtils.getClass(name);
							String alias = StringUtils.lowercaseFirstLetter(clazz.getSimpleName());
							xstreamObject.alias(alias, clazz);
							if (!clazz.equals(Model.class)) {
								xstreamObject.omitField(clazz, "modelEncoding"); // unnecessary field
							}
						} catch (ClassNotFoundException e) {
							getLog().error(e);
						}
					}
				}

				jarStream.closeEntry();
			}

			jarStream.close();
			jarStream = null;
		} catch (IOException e) {
			if (getLog().isDebugEnabled()) {
				getLog().debug("IOException: " + e.getMessage(), e);
			}
		} finally {
			IOUtil.close(jarStream);
		}
	}

	/**
	 * @return the <code>org.apache.maven:maven-model</code> artifact jar file in the local repository.
	 * @throws MojoExecutionException if any
	 * @throws ProjectBuildingException if any
	 * @throws ArtifactResolverException if any
	 */
	private File getMavenModelJarFile()
			throws MojoExecutionException, ProjectBuildingException, ArtifactResolverException {
		return getArtifactFile(true);
	}

	/**
	 * @return the <code>org.apache.maven:maven-settings</code> artifact jar file in the local repository.
	 * @throws MojoExecutionException if any
	 * @throws ProjectBuildingException if any
	 * @throws ArtifactResolverException if any
	 */
	private File getMavenSettingsJarFile()
			throws MojoExecutionException, ProjectBuildingException, ArtifactResolverException {
		return getArtifactFile(false);
	}

	/**
	 * @param isPom <code>true</code> to lookup the <code>maven-model</code> artifact jar, <code>false</code> to lookup
	 *            the <code>maven-settings</code> artifact jar.
	 * @return the <code>org.apache.maven:maven-model|maven-settings</code> artifact jar file for this current
	 *         HelpPlugin pom.
	 * @throws MojoExecutionException if any
	 * @throws ProjectBuildingException if any
	 * @throws ArtifactResolverException if any
	 */
	private File getArtifactFile(boolean isPom)
			throws MojoExecutionException, ProjectBuildingException, ArtifactResolverException {
		List<Dependency> dependencies = getHelpPluginPom().getDependencies();
		for (Dependency depependency : dependencies) {
			if (!(depependency.getGroupId().equals("org.apache.maven"))) {
				continue;
			}

			if (isPom) {
				if (!(depependency.getArtifactId().equals("maven-model"))) {
					continue;
				}
			} else {
				if (!(depependency.getArtifactId().equals("maven-settings"))) {
					continue;
				}
			}

			ArtifactCoordinate coordinate = getArtifactCoordinate(depependency.getGroupId(),
					depependency.getArtifactId(), depependency.getVersion(), "jar");
			ProjectBuildingRequest pbr = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
			pbr.setRemoteRepositories(remoteRepositories);
			return artifactResolver.resolveArtifact(pbr, coordinate).getArtifact().getFile();
		}

		throw new MojoExecutionException(
				"Unable to find the 'org.apache.maven:" + (isPom ? "maven-model" : "maven-settings") + "' artifact");
	}

	/**
	 * @return the Maven POM for the current help plugin
	 * @throws MojoExecutionException if any
	 * @throws ProjectBuildingException if any
	 */
	private MavenProject getHelpPluginPom() throws MojoExecutionException, ProjectBuildingException {
		String resource = "META-INF/maven/org.apache.maven.plugins/maven-help-plugin/pom.properties";

		InputStream resourceAsStream = EvaluateMojo.class.getClassLoader().getResourceAsStream(resource);
		if (resourceAsStream == null) {
			throw new MojoExecutionException("The help plugin artifact was not found.");
		}
		Properties properties = new Properties();
		try {
			properties.load(resourceAsStream);
		} catch (IOException e) {
			if (getLog().isDebugEnabled()) {
				getLog().debug("IOException: " + e.getMessage(), e);
			}
		} finally {
			IOUtil.close(resourceAsStream);
		}

		String artifactString = properties.getProperty("groupId", "unknown") + ":"
				+ properties.getProperty("artifactId", "unknown") + ":" + properties.getProperty("version", "unknown");

		return getMavenProject(artifactString);
	}

	/**
	 * @param name not null
	 * @return the plural of the name
	 */
	private static String pluralize(String name) {
		if (StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("name is required");
		}

		if (name.endsWith("y")) {
			return name.substring(0, name.length() - 1) + "ies";
		} else if (name.endsWith("s")) {
			return name;
		} else {
			return name + "s";
		}
	}
}
