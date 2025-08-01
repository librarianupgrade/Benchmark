/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.metrics.core.publish;

import static org.apache.servicecomb.foundation.common.utils.StringBuilderUtils.appendLine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.core.invocation.InvocationStageTrace;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.foundation.metrics.PolledEvent;
import org.apache.servicecomb.foundation.metrics.meter.LatencyDistributionConfig;
import org.apache.servicecomb.foundation.metrics.meter.LatencyScopeConfig;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementNode;
import org.apache.servicecomb.foundation.metrics.publish.MeasurementTree;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.metrics.core.VertxMetersInitializer;
import org.apache.servicecomb.metrics.core.meter.os.NetMeter;
import org.apache.servicecomb.metrics.core.meter.os.OsMeter;
import org.apache.servicecomb.metrics.core.meter.os.SystemMeter;
import org.apache.servicecomb.metrics.core.publish.model.DefaultPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.ThreadPoolPublishModel;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerf;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroup;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroups;
import org.apache.servicecomb.metrics.core.publish.model.invocation.PerfInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.core.Vertx;

public class DefaultLogPublisher implements MetricsInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("scb-metrics");

	public static final String ENABLED = "servicecomb.metrics.publisher.defaultLog.enabled";

	// for a client, maybe will connect to too many endpoints, so default not print detail, just print summary
	public static final String ENDPOINTS_CLIENT_DETAIL_ENABLED = "servicecomb.metrics.publisher.defaultLog.endpoints.client.detail.enabled";

	private static final String FIRST_LINE_SIMPLE_FORMAT = "  %-11s\n";

	private static final String SIMPLE_FORMAT = "              %-8.1f %-18s %s%s\n";

	//details
	private static final String PRODUCER_DETAILS_FORMAT = ""
			+ "        prepare: %-18s decode-request       : %-18s queue : %-18s business-execute: %s\n"
			+ "        encode-response: %-18s send: %-18s\n";

	private static final String CONSUMER_DETAILS_FORMAT = ""
			+ "        prepare     : %-18s connection : %-18s encode-request: %-18s send     : %s\n"
			+ "        wait  : %-18s decode-response    : %-18s\n";

	private static final String EDGE_DETAILS_FORMAT = ""
			+ "        prepare     : %-18s provider-decode       : %-18s connection : %-18s consumer-encode : %s\n"
			+ "        consumer-send : %-18s wait     : %-18s consumer-decode  : %-18s provider-encode    : %s\n"
			+ "        provider-send    : %-18s\n";

	private LatencyDistributionConfig latencyDistributionConfig;

	/**
	 * if config is 0,1,10,100 then header will be:<br>
	 *   [0,1)  [1,10) [10,100) [100,)
	 */
	private String latencyDistributionHeader = "";

	/**
	 * if config is 0,1,10,100 then format will be:<br>
	 *   %-7d %-7d %-9d %-7d
	 */
	private String latencyDistributionFormat = "";

	private Environment environment;

	@Autowired
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void init(MeterRegistry meterRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
		if (!environment.getProperty(ENABLED, boolean.class, false)) {
			return;
		}

		initLatencyDistribution(config);

		eventBus.register(this);
	}

	private void initLatencyDistribution(MetricsBootstrapConfig config) {
		// default length is 7 which include a space, one minute 999999 requests, TPS is 16666, mostly it's enough
		int leastLatencyScopeStrLength = config.getMinScopeLength();

		latencyDistributionConfig = new LatencyDistributionConfig(config.getLatencyDistribution());
		String header;
		for (LatencyScopeConfig scopeConfig : latencyDistributionConfig.getScopeConfigs()) {
			if (scopeConfig.getMsMax() == LatencyDistributionConfig.MAX_LATENCY) {
				header = String.format("[%d,) ", scopeConfig.getMsMin());
			} else {
				header = String.format("[%d,%d) ", scopeConfig.getMsMin(), scopeConfig.getMsMax());
			}
			header = Strings.padEnd(header, leastLatencyScopeStrLength, ' ');
			latencyDistributionHeader += header;

			String format = "%-" + (header.length() - 1) + "d ";
			latencyDistributionFormat += format;
		}
	}

	@Subscribe
	public void onPolledEvent(PolledEvent event) {
		try {
			printLog(event.getMeters());
		} catch (Throwable e) {
			// make development easier
			LOGGER.error("Failed to print perf log.", e);
		}
	}

	protected void printLog(List<Meter> meters) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		PublishModelFactory factory = new PublishModelFactory(meters);
		DefaultPublishModel model = factory.createDefaultPublishModel();

		printOsLog(factory.getTree(), sb);
		printVertxMetrics(factory.getTree(), sb);
		printThreadPoolMetrics(model, sb);

		printConsumerLog(model, sb);
		printProducerLog(model, sb);
		printEdgeLog(model, sb);

		LOGGER.info(sb.toString());
	}

	protected void printOsLog(MeasurementTree tree, StringBuilder sb) {
		MeasurementNode osNode = tree.findChild(OsMeter.OS_NAME);
		if (osNode == null || osNode.getMeasurements().isEmpty()) {
			return;
		}

		appendLine(sb, "os:");
		printCpuLog(sb, osNode);
		printNetLog(sb, osNode);
	}

	private void printNetLog(StringBuilder sb, MeasurementNode osNode) {
		MeasurementNode netNode = osNode.findChild(OsMeter.OS_TYPE_NET);
		if (netNode == null || netNode.getMeasurements().isEmpty()) {
			return;
		}

		appendLine(sb, "  net:");
		appendLine(sb, "    send(Bps)    recv(Bps)    send(pps)    recv(pps)    interface");

		StringBuilder tmpSb = new StringBuilder();
		for (MeasurementNode interfaceNode : netNode.getChildren().values()) {
			double sendRate = interfaceNode.findChild(NetMeter.TAG_SEND.getValue()).summary();
			double sendPacketsRate = interfaceNode.findChild(NetMeter.TAG_PACKETS_SEND.getValue()).summary();
			double receiveRate = interfaceNode.findChild(NetMeter.TAG_RECEIVE.getValue()).summary();
			double receivePacketsRate = interfaceNode.findChild(NetMeter.TAG_PACKETS_RECEIVE.getValue()).summary();
			if (sendRate == 0 && receiveRate == 0 && receivePacketsRate == 0 && sendPacketsRate == 0) {
				continue;
			}
			appendLine(tmpSb, "    %-12s %-12s %-12s %-12s %s", NetUtils.humanReadableBytes((long) sendRate),
					NetUtils.humanReadableBytes((long) receiveRate),
					NetUtils.humanReadableBytes((long) sendPacketsRate),
					NetUtils.humanReadableBytes((long) receivePacketsRate), interfaceNode.getName());
		}
		if (tmpSb.length() != 0) {
			sb.append(tmpSb);
		}
	}

	private void printCpuLog(StringBuilder sb, MeasurementNode osNode) {
		MeasurementNode cpuNode = osNode.findChild(SystemMeter.CPU_USAGE);
		MeasurementNode processNode = osNode.findChild(SystemMeter.PROCESS_CPU_USAGE);
		MeasurementNode memoryNode = osNode.findChild(SystemMeter.MEMORY_USAGE);
		MeasurementNode slaNode = osNode.findChild(SystemMeter.SYSTEM_LOAD_AVERAGE);
		appendLine(sb, "  cpu:");
		appendLine(sb, "    all usage: %.2f%%    process usage: %.2f%%    sla: %.2f    memory usage: %.2f%%",
				cpuNode.summary() * 100, processNode.summary() * 100, slaNode.summary(), memoryNode.summary() * 100);
	}

	protected void printThreadPoolMetrics(DefaultPublishModel model, StringBuilder sb) {
		if (model.getThreadPools().isEmpty()) {
			return;
		}
		sb.append("threadPool:\n");
		sb.append("  coreSize maxThreads poolSize currentBusy rejected queueSize taskCount taskFinished name\n");
		for (Entry<String, ThreadPoolPublishModel> entry : model.getThreadPools().entrySet()) {
			ThreadPoolPublishModel threadPoolPublishModel = entry.getValue();
			sb.append(String.format("  %-8d %-10d %-8d %-11d %-8.0f %-9d %-9.1f %-12.1f %s\n",
					threadPoolPublishModel.getCorePoolSize(), threadPoolPublishModel.getMaxThreads(),
					threadPoolPublishModel.getPoolSize(), threadPoolPublishModel.getCurrentThreadsBusy(),
					threadPoolPublishModel.getRejected(), threadPoolPublishModel.getQueueSize(),
					threadPoolPublishModel.getAvgTaskCount(), threadPoolPublishModel.getAvgCompletedTaskCount(),
					entry.getKey()));
		}
	}

	protected void printEdgeLog(DefaultPublishModel model, StringBuilder sb) {
		OperationPerfGroups edgePerf = model.getEdge().getOperationPerfGroups();
		if (edgePerf == null) {
			return;
		}
		sb.append("" + "edge:\n" + " simple:\n" + "  status      requests      latency       ")
				.append(latencyDistributionHeader).append("operation\n");
		StringBuilder detailsBuilder = new StringBuilder();
		//print sample
		for (Map<String, OperationPerfGroup> statusMap : edgePerf.getGroups().values()) {
			for (OperationPerfGroup perfGroup : statusMap.values()) {
				//append sample
				sb.append(printSamplePerf(perfGroup));
				//append details
				detailsBuilder.append(printEdgeDetailsPerf(perfGroup));
			}
		}
		sb.append(" details:\n").append(detailsBuilder);
	}

	protected void printConsumerLog(DefaultPublishModel model, StringBuilder sb) {
		OperationPerfGroups consumerPerf = model.getConsumer().getOperationPerfGroups();
		if (consumerPerf == null) {
			return;
		}
		sb.append("" + "consumer:\n" + " simple:\n" + "  status      requests      latency       ")
				.append(latencyDistributionHeader).append("operation\n");
		StringBuilder detailsBuilder = new StringBuilder();
		//print sample
		for (Map<String, OperationPerfGroup> statusMap : consumerPerf.getGroups().values()) {
			for (OperationPerfGroup perfGroup : statusMap.values()) {
				//append sample
				sb.append(printSamplePerf(perfGroup));
				//append details
				detailsBuilder.append(printConsumerDetailsPerf(perfGroup));
			}
		}
		sb.append(" details:\n").append(detailsBuilder);
	}

	protected void printProducerLog(DefaultPublishModel model, StringBuilder sb) {
		OperationPerfGroups producerPerf = model.getProducer().getOperationPerfGroups();
		if (producerPerf == null) {
			return;
		}
		sb.append("" + "producer:\n" + " simple:\n" + "  status      requests      latency       ")
				.append(latencyDistributionHeader).append("operation\n");
		// use detailsBuilder, we can traverse the map only once
		StringBuilder detailsBuilder = new StringBuilder();
		//print sample
		for (Map<String, OperationPerfGroup> statusMap : producerPerf.getGroups().values()) {
			for (OperationPerfGroup perfGroup : statusMap.values()) {
				//append sample
				sb.append(printSamplePerf(perfGroup));
				//append details
				detailsBuilder.append(printProducerDetailsPerf(perfGroup));
			}
		}
		//print details
		sb.append(" details:\n").append(detailsBuilder);
	}

	private StringBuilder printSamplePerf(OperationPerfGroup perfGroup) {
		StringBuilder sb = new StringBuilder();
		String status = perfGroup.getTransport() + "." + perfGroup.getStatus() + ":";
		sb.append(String.format(FIRST_LINE_SIMPLE_FORMAT, status));

		for (int i = 0; i < perfGroup.getOperationPerfs().size(); i++) {
			OperationPerf operationPerf = perfGroup.getOperationPerfs().get(i);
			if (isIgnoreEmptyPerf(operationPerf)) {
				continue;
			}
			PerfInfo stageTotal = operationPerf.findStage(InvocationStageTrace.STAGE_TOTAL);
			sb.append(String.format(SIMPLE_FORMAT, stageTotal.getTotalRequests(), getDetailsFromPerf(stageTotal),
					formatLatencyDistribution(operationPerf), operationPerf.getOperation()));
		}
		OperationPerf summaryOperation = perfGroup.getSummary();
		PerfInfo stageSummaryTotal = summaryOperation.findStage(InvocationStageTrace.STAGE_TOTAL);
		//print summary
		sb.append(String.format(SIMPLE_FORMAT, stageSummaryTotal.getTotalRequests(),
				getDetailsFromPerf(stageSummaryTotal), formatLatencyDistribution(summaryOperation), "(summary)"));
		return sb;
	}

	private String formatLatencyDistribution(OperationPerf operationPerf) {
		return String.format(latencyDistributionFormat, (Object[]) operationPerf.getLatencyDistribution());
	}

	private StringBuilder printProducerDetailsPerf(OperationPerfGroup perfGroup) {
		StringBuilder sb = new StringBuilder();
		//append rest."200":
		sb.append("    ").append(perfGroup.getTransport()).append(".").append(perfGroup.getStatus()).append(":\n");
		PerfInfo prepare, queue, providerDecode, providerEncode, execute, sendResp;
		for (OperationPerf operationPerf : perfGroup.getOperationPerfs()) {
			if (isIgnoreEmptyPerf(operationPerf)) {
				continue;
			}
			prepare = operationPerf.findStage(InvocationStageTrace.STAGE_PREPARE);
			queue = operationPerf.findStage(InvocationStageTrace.STAGE_PROVIDER_QUEUE);
			providerDecode = operationPerf.findStage(InvocationStageTrace.STAGE_PROVIDER_DECODE_REQUEST);
			providerEncode = operationPerf.findStage(InvocationStageTrace.STAGE_PROVIDER_ENCODE_RESPONSE);
			execute = operationPerf.findStage(InvocationStageTrace.STAGE_PROVIDER_BUSINESS);
			sendResp = operationPerf.findStage(InvocationStageTrace.STAGE_PROVIDER_SEND);

			sb.append("      ").append(operationPerf.getOperation()).append(":\n")
					.append(String.format(PRODUCER_DETAILS_FORMAT, getDetailsFromPerf(prepare),
							getDetailsFromPerf(providerDecode), getDetailsFromPerf(queue), getDetailsFromPerf(execute),
							getDetailsFromPerf(providerEncode), getDetailsFromPerf(sendResp)));
		}

		return sb;
	}

	private StringBuilder printConsumerDetailsPerf(OperationPerfGroup perfGroup) {
		StringBuilder sb = new StringBuilder();
		//append rest."200":
		sb.append("    ").append(perfGroup.getTransport()).append(".").append(perfGroup.getStatus()).append(":\n");

		PerfInfo prepare, encodeRequest, decodeResponse, sendReq, getConnect, waitResp;
		for (OperationPerf operationPerf : perfGroup.getOperationPerfs()) {
			if (isIgnoreEmptyPerf(operationPerf)) {
				continue;
			}
			prepare = operationPerf.findStage(InvocationStageTrace.STAGE_PREPARE);
			encodeRequest = operationPerf.findStage(InvocationStageTrace.STAGE_CONSUMER_ENCODE_REQUEST);
			decodeResponse = operationPerf.findStage(InvocationStageTrace.STAGE_CONSUMER_DECODE_RESPONSE);
			sendReq = operationPerf.findStage(InvocationStageTrace.STAGE_CONSUMER_SEND);
			getConnect = operationPerf.findStage(InvocationStageTrace.STAGE_CONSUMER_CONNECTION);
			waitResp = operationPerf.findStage(InvocationStageTrace.STAGE_CONSUMER_WAIT);

			sb.append("      ").append(operationPerf.getOperation()).append(":\n")
					.append(String.format(CONSUMER_DETAILS_FORMAT, getDetailsFromPerf(prepare),
							getDetailsFromPerf(getConnect), getDetailsFromPerf(encodeRequest),
							getDetailsFromPerf(sendReq), getDetailsFromPerf(waitResp),
							getDetailsFromPerf(decodeResponse)));
		}

		return sb;
	}

	private StringBuilder printEdgeDetailsPerf(OperationPerfGroup perfGroup) {
		StringBuilder sb = new StringBuilder();
		//append rest."200":
		sb.append("    ").append(perfGroup.getTransport()).append(".").append(perfGroup.getStatus()).append(":\n");

		PerfInfo prepare, connection, decodeProviderRequest, encodeProviderResponse, encodeConsumerRequest,
				decodeConsumerResponse, sendReq, getConnect, waitResp, sendResp;
		for (OperationPerf operationPerf : perfGroup.getOperationPerfs()) {
			if (isIgnoreEmptyPerf(operationPerf)) {
				continue;
			}
			prepare = operationPerf.findStage(InvocationStageTrace.STAGE_PREPARE);
			connection = operationPerf.findStage(InvocationStageTrace.STAGE_CONSUMER_CONNECTION);
			decodeProviderRequest = operationPerf.findStage(InvocationStageTrace.STAGE_PROVIDER_DECODE_REQUEST);
			encodeProviderResponse = operationPerf.findStage(InvocationStageTrace.STAGE_PROVIDER_ENCODE_RESPONSE);
			encodeConsumerRequest = operationPerf.findStage(InvocationStageTrace.STAGE_CONSUMER_ENCODE_REQUEST);
			sendReq = operationPerf.findStage(InvocationStageTrace.STAGE_CONSUMER_SEND);
			decodeConsumerResponse = operationPerf.findStage(InvocationStageTrace.STAGE_CONSUMER_DECODE_RESPONSE);
			waitResp = operationPerf.findStage(InvocationStageTrace.STAGE_CONSUMER_WAIT);
			sendResp = operationPerf.findStage(InvocationStageTrace.STAGE_PROVIDER_SEND);

			sb.append("      ").append(operationPerf.getOperation()).append(":\n")
					.append(String.format(EDGE_DETAILS_FORMAT, getDetailsFromPerf(prepare),
							getDetailsFromPerf(decodeProviderRequest), getDetailsFromPerf(connection),
							getDetailsFromPerf(encodeConsumerRequest), getDetailsFromPerf(sendReq),
							getDetailsFromPerf(waitResp), getDetailsFromPerf(decodeConsumerResponse),
							getDetailsFromPerf(encodeProviderResponse), getDetailsFromPerf(sendResp)));
		}

		return sb;
	}

	private boolean isIgnoreEmptyPerf(OperationPerf operationPerf) {
		PerfInfo stageTotal = operationPerf.findStage(InvocationStageTrace.STAGE_TOTAL);
		// max latency is calculated in ring algorithm, maybe not 0
		if (Double.compare(0D, stageTotal.getTotalRequests()) == 0
				&& Double.compare(0D, stageTotal.getMsMaxLatency()) == 0) {
			return true;
		}
		return false;
	}

	protected void printVertxMetrics(MeasurementTree tree, StringBuilder sb) {
		appendLine(sb, "vertx:");

		appendLine(sb, "  instances:");
		appendLine(sb, "    name       eventLoopContext-created");
		for (Entry<String, Vertx> entry : VertxUtils.getVertxMap().entrySet()) {
			appendLine(sb, "    %-10s %d", entry.getKey(),
					// TODO will be fixed by next vertx update.entry.getValue().getEventLoopContextCreatedCount()
					0);
		}

		ClientEndpointsLogPublisher client = new ClientEndpointsLogPublisher(tree, sb,
				VertxMetersInitializer.ENDPOINTS_CLIENT);
		ServerEndpointsLogPublisher server = new ServerEndpointsLogPublisher(tree, sb,
				VertxMetersInitializer.ENDPOINTS_SERVER);
		if (client.isExists() || server.isExists()) {
			appendLine(sb, "  transport:");
			if (client.isExists()) {
				client.print(environment.getProperty(ENDPOINTS_CLIENT_DETAIL_ENABLED, boolean.class, true));
			}

			if (server.isExists()) {
				server.print(true);
			}
		}
	}

	private static String getDetailsFromPerf(PerfInfo perfInfo) {
		String result = "";
		if (perfInfo != null) {
			result = String.format("%.3f/%.3f", perfInfo.calcMsLatency(), perfInfo.getMsMaxLatency());
		}
		return result;
	}
}
