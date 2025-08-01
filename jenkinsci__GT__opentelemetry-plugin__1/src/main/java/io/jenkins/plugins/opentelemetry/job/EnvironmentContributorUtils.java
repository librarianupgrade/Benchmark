package io.jenkins.plugins.opentelemetry.job;

import hudson.EnvVars;
import hudson.model.Run;
import io.jenkins.plugins.opentelemetry.semconv.JenkinsOtelSemanticAttributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;

import javax.annotation.Nonnull;

public class EnvironmentContributorUtils {

	public static void setEnvironmentVariables(@Nonnull Run run, @Nonnull EnvVars envs, @Nonnull Span span) {
		String spanId = span.getSpanContext().getSpanId();
		String traceId = span.getSpanContext().getTraceId();
		try (Scope ignored = span.makeCurrent()) {
			envs.putIfAbsent(JenkinsOtelSemanticAttributes.TRACE_ID, traceId);
			envs.put(JenkinsOtelSemanticAttributes.SPAN_ID, spanId);
			TextMapSetter<EnvVars> setter = (carrier, key, value) -> carrier.put(key.toUpperCase(), value);
			W3CTraceContextPropagator.getInstance().inject(Context.current(), envs, setter);
		}

		MonitoringAction action = new MonitoringAction(traceId, spanId);
		action.onAttached(run);
		for (MonitoringAction.ObservabilityBackendLink link : action.getLinks()) {
			// Default backend link got an empty environment variable.
			if (link.getEnvironmentVariableName() != null) {
				envs.put(link.getEnvironmentVariableName(), link.getUrl());
			}
		}
	}
}
