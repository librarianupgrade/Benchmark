/**
 * Copyright 2012 Comcast Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.comcast.cns.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.HConsistencyLevel;
import me.prettyprint.hector.api.beans.Row;

import org.apache.log4j.Logger;

import com.comcast.cmb.common.model.CMBPolicy;
import com.comcast.cmb.common.model.User;
import com.comcast.cmb.common.persistence.CassandraPersistence;
import com.comcast.cmb.common.util.CMBProperties;
import com.comcast.cqs.io.CQSAPIStatsPopulator;
import com.comcast.cqs.model.CQSAPIStats;

/**
 * Subscribe action
 * @author bwolf, jorge
 *
 */
public class CNSGetAPIStatsAction extends CNSAction {

	private static Logger logger = Logger.getLogger(CNSGetAPIStatsAction.class);

	public CNSGetAPIStatsAction() {
		super("GetAPIStats");
	}

	@Override
	public boolean isActionAllowed(User user, HttpServletRequest request, String service, CMBPolicy policy)
			throws Exception {
		return true;
	}

	/**
	 * Get various stats about active cns workers
	 * @param user the user for whom we are subscribing.
	 * @param asyncContext
	 */
	@Override
	public boolean doAction(User user, AsyncContext asyncContext) throws Exception {

		HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();

		CassandraPersistence cassandraHandler = new CassandraPersistence(CMBProperties.getInstance().getCNSKeyspace());

		List<Row<String, String, String>> rows = cassandraHandler.readNextNNonEmptyRows("CNSAPIServers", null, 1000, 10,
				new StringSerializer(), new StringSerializer(), new StringSerializer(), HConsistencyLevel.QUORUM);
		List<CQSAPIStats> statsList = new ArrayList<CQSAPIStats>();

		if (rows != null) {

			for (Row<String, String, String> row : rows) {

				CQSAPIStats stats = new CQSAPIStats();
				stats.setIpAddress(row.getKey());

				if (row.getColumnSlice().getColumnByName("timestamp") != null) {
					stats.setTimestamp(Long.parseLong(row.getColumnSlice().getColumnByName("timestamp").getValue()));
				}

				if (row.getColumnSlice().getColumnByName("jmxport") != null) {
					stats.setJmxPort(Long.parseLong(row.getColumnSlice().getColumnByName("jmxport").getValue()));
				}

				if (row.getColumnSlice().getColumnByName("dataCenter") != null) {
					stats.setDataCenter(row.getColumnSlice().getColumnByName("dataCenter").getValue());
				}

				if (row.getColumnSlice().getColumnByName("serviceUrl") != null) {
					stats.setServiceUrl(row.getColumnSlice().getColumnByName("serviceUrl").getValue());
				}

				if (stats.getIpAddress().contains(":")) {
					statsList.add(stats);
				}
			}
		}

		for (CQSAPIStats stats : statsList) {

			if (System.currentTimeMillis() - stats.getTimestamp() >= 5 * 60 * 1000) {

				stats.addStatus("STALE");

			} else if (stats.getJmxPort() > 0) {

				JMXConnector jmxConnector = null;
				String url = null;

				try {

					String host = stats.getIpAddress();

					if (host.contains(":")) {
						host = host.substring(0, host.indexOf(":"));
					}

					long port = stats.getJmxPort();
					url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";

					JMXServiceURL serviceUrl = new JMXServiceURL(url);
					jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);

					MBeanServerConnection mbeanConn = jmxConnector.getMBeanServerConnection();

					ObjectName cqsAPIMonitor = new ObjectName("com.comcast.cns.controller:type=CNSMonitorMBean");

					@SuppressWarnings("unchecked")
					Map<String, AtomicLong> callStats = (Map<String, AtomicLong>) mbeanConn.getAttribute(cqsAPIMonitor,
							"CallStats");

					stats.setCallStats(callStats);

					@SuppressWarnings("unchecked")
					Map<String, AtomicLong> callFailureStats = (Map<String, AtomicLong>) mbeanConn
							.getAttribute(cqsAPIMonitor, "CallFailureStats");

					stats.setCallFailureStats(callFailureStats);

				} catch (Exception ex) {

					logger.warn("event=failed_to_connect_to_jmx_server url=" + url, ex);
					stats.addStatus("JMX UNAVAILABLE");

				} finally {

					if (jmxConnector != null) {
						jmxConnector.close();
					}
				}

				if (stats.getStatus() == null) {
					stats.addStatus("OK");
				}
			}
		}

		String res = CQSAPIStatsPopulator.getGetAPIStatsResponse(statsList);
		response.getWriter().println(res);

		return true;
	}
}
