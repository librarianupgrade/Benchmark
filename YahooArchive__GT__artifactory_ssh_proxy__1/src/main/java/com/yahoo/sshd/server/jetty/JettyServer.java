package com.yahoo.sshd.server.jetty;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyServer {
	final static Logger LOG = LoggerFactory.getLogger(JettyServer.class);

	@SuppressWarnings({ "resource", "boxing" })
	public static Server newServer(int jettyPort, String jettyWebAppDir) throws Exception {

		if (jettyPort == 0 || jettyWebAppDir == null) {
			throw new IllegalArgumentException("Jetty port and resource dir may not be empty");
		}

		// server setup
		Server server = new Server();
		server.addBean(new ScheduledExecutorScheduler());
		server.setDumpAfterStart(false);
		server.setDumpBeforeStop(false);
		server.setStopAtShutdown(true);

		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setResourceBase(jettyWebAppDir);
		server.setHandler(resourceHandler);

		// http configuration
		HttpConfiguration http_config = new HttpConfiguration();
		http_config.setSendServerVersion(true);

		// HTTP connector
		ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
		http.setPort(jettyPort);
		server.addConnector(http);

		// start server
		server.start();

		LOG.info("Started jetty server on port: {}, resource dir: {} ", jettyPort, jettyWebAppDir);
		return server;
	}
}
