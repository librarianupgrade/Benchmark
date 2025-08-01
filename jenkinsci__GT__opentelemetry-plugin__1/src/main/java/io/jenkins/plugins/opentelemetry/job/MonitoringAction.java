/*
 * Copyright The Original Author or Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.opentelemetry.job;

import hudson.ExtensionList;
import hudson.model.Action;
import hudson.model.Run;
import io.jenkins.plugins.opentelemetry.JenkinsOpenTelemetryPluginConfiguration;
import io.jenkins.plugins.opentelemetry.backend.ObservabilityBackend;
import io.jenkins.plugins.opentelemetry.semconv.JenkinsOtelSemanticAttributes;
import jenkins.model.Jenkins;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public class MonitoringAction implements Action, RunAction2, SimpleBuildStep.LastBuildAction {
	private final static Logger LOGGER = Logger.getLogger(MonitoringAction.class.getName());

	final String traceId;
	final String spanId;
	SpanNamingStrategy spanNamingStrategy;
	transient Run run;
	transient JenkinsOpenTelemetryPluginConfiguration pluginConfiguration;

	public MonitoringAction(String traceId, String spanId) {
		this.traceId = traceId;
		this.spanId = spanId;
	}

	@Override
	public void onAttached(Run<?, ?> r) {
		this.run = r;
		this.pluginConfiguration = ExtensionList.lookupSingleton(JenkinsOpenTelemetryPluginConfiguration.class);
		this.spanNamingStrategy = this.pluginConfiguration.getSpanNamingStrategy();
	}

	@Override
	public void onLoad(Run<?, ?> r) {
		this.run = r;
		this.pluginConfiguration = ExtensionList.lookupSingleton(JenkinsOpenTelemetryPluginConfiguration.class);
		this.spanNamingStrategy = this.pluginConfiguration.getSpanNamingStrategy();
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return "OpenTelemetry";
	}

	@Override
	public Collection<? extends Action> getProjectActions() {
		return run.getParent().getLastSuccessfulBuild().getActions(MonitoringAction.class);
	}

	@Override
	public String getUrlName() {
		return null;
	}

	public String getTraceId() {
		return traceId;
	}

	public String getSpanId() {
		return spanId;
	}

	@Nonnull
	public List<ObservabilityBackendLink> getLinks() {
		List<ObservabilityBackend> observabilityBackends = this.pluginConfiguration.getObservabilityBackends();

		if (observabilityBackends.isEmpty()) {
			return Collections.singletonList(new ObservabilityBackendLink(
					"Please define an OpenTelemetry Visualisation URL of pipelines in Jenkins configuration",
					Jenkins.get().getRootUrl() + "/configure", "/images/48x48/gear2.png", null));
		}
		Map<String, Object> binding = new HashMap<>();
		binding.put("serviceName",
				Objects.requireNonNull(JenkinsOpenTelemetryPluginConfiguration.get().getServiceName()));
		binding.put("rootSpanName", spanNamingStrategy.getRootSpanName(run));
		binding.put("traceId", this.traceId);
		binding.put("spanId", this.spanId);
		binding.put("startTime", Instant.ofEpochMilli(run.getStartTimeInMillis()));

		List<ObservabilityBackendLink> links = new ArrayList<>();
		for (ObservabilityBackend observabilityBackend : observabilityBackends) {
			links.add(new ObservabilityBackendLink("View pipeline with " + observabilityBackend.getName(),
					observabilityBackend.getTraceVisualisationUrl(binding), observabilityBackend.getIconPath(),
					observabilityBackend.getEnvVariableName()));

		}
		return links;
	}

	@Override
	public String toString() {
		return "MonitoringAction{" + "traceId='" + traceId + '\'' + ", spanId='" + spanId + '\'' + ", run='" + run
				+ '\'' + '}';
	}

	public static class ObservabilityBackendLink {
		final String label;
		final String url;
		final String iconUrl;
		final String environmentVariableName;

		public ObservabilityBackendLink(String label, String url, String iconUrl, String environmentVariableName) {
			this.label = label;
			this.url = url;
			this.iconUrl = iconUrl;
			this.environmentVariableName = environmentVariableName;
		}

		public String getLabel() {
			return label;
		}

		public String getUrl() {
			return url;
		}

		public String getIconUrl() {
			return iconUrl;
		}

		public String getEnvironmentVariableName() {
			return environmentVariableName;
		}

		@Override
		public String toString() {
			return "ObservabilityBackendLink{" + "label='" + label + '\'' + ", url='" + url + '\'' + ", iconUrl='"
					+ iconUrl + '\'' + ", environmentVariableName='" + environmentVariableName + '\'' + '}';
		}
	}
}
