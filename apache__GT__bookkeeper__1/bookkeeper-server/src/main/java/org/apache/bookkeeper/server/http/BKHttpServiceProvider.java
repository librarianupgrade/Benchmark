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
package org.apache.bookkeeper.server.http;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.bookkeeper.bookie.Bookie;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.http.HttpServer.ApiType;
import org.apache.bookkeeper.http.HttpServiceProvider;
import org.apache.bookkeeper.http.service.ErrorHttpService;
import org.apache.bookkeeper.http.service.HeartbeatService;
import org.apache.bookkeeper.http.service.HttpEndpointService;
import org.apache.bookkeeper.meta.LedgerManagerFactory;
import org.apache.bookkeeper.proto.BookieServer;
import org.apache.bookkeeper.replication.Auditor;
import org.apache.bookkeeper.replication.AutoRecoveryMain;
import org.apache.bookkeeper.server.http.service.AutoRecoveryStatusService;
import org.apache.bookkeeper.server.http.service.BookieInfoService;
import org.apache.bookkeeper.server.http.service.BookieIsReadyService;
import org.apache.bookkeeper.server.http.service.BookieStateReadOnlyService;
import org.apache.bookkeeper.server.http.service.BookieStateService;
import org.apache.bookkeeper.server.http.service.ConfigurationService;
import org.apache.bookkeeper.server.http.service.DecommissionService;
import org.apache.bookkeeper.server.http.service.DeleteLedgerService;
import org.apache.bookkeeper.server.http.service.ExpandStorageService;
import org.apache.bookkeeper.server.http.service.GCDetailsService;
import org.apache.bookkeeper.server.http.service.GetLastLogMarkService;
import org.apache.bookkeeper.server.http.service.GetLedgerMetaService;
import org.apache.bookkeeper.server.http.service.ListBookieInfoService;
import org.apache.bookkeeper.server.http.service.ListBookiesService;
import org.apache.bookkeeper.server.http.service.ListDiskFilesService;
import org.apache.bookkeeper.server.http.service.ListLedgerService;
import org.apache.bookkeeper.server.http.service.ListUnderReplicatedLedgerService;
import org.apache.bookkeeper.server.http.service.LostBookieRecoveryDelayService;
import org.apache.bookkeeper.server.http.service.MetricsService;
import org.apache.bookkeeper.server.http.service.ReadLedgerEntryService;
import org.apache.bookkeeper.server.http.service.RecoveryBookieService;
import org.apache.bookkeeper.server.http.service.TriggerAuditService;
import org.apache.bookkeeper.server.http.service.TriggerGCService;
import org.apache.bookkeeper.server.http.service.WhoIsAuditorService;
import org.apache.bookkeeper.stats.StatsProvider;
import org.apache.zookeeper.KeeperException;

/**
 * Bookkeeper based implementation of HttpServiceProvider,
 * which provide bookkeeper services to handle http requests
 * from different http endpoints.
 */
@Slf4j
public class BKHttpServiceProvider implements HttpServiceProvider {

	private final StatsProvider statsProvider;
	private final BookieServer bookieServer;
	private final AutoRecoveryMain autoRecovery;
	private final LedgerManagerFactory ledgerManagerFactory;
	private final ServerConfiguration serverConf;
	private final BookKeeperAdmin bka;
	private final ExecutorService executor;

	private BKHttpServiceProvider(BookieServer bookieServer, AutoRecoveryMain autoRecovery,
			LedgerManagerFactory ledgerManagerFactory, ServerConfiguration serverConf, StatsProvider statsProvider)
			throws IOException, KeeperException, InterruptedException, BKException {
		this.bookieServer = bookieServer;
		this.autoRecovery = autoRecovery;
		this.ledgerManagerFactory = ledgerManagerFactory;
		this.serverConf = serverConf;
		this.statsProvider = statsProvider;
		ClientConfiguration clientConfiguration = new ClientConfiguration(serverConf);
		this.bka = new BookKeeperAdmin(clientConfiguration);

		this.executor = Executors.newSingleThreadExecutor(
				new ThreadFactoryBuilder().setNameFormat("BKHttpServiceThread").setDaemon(true).build());
	}

	@Override
	public void close() throws IOException {
		try {
			executor.shutdown();
			if (bka != null) {
				bka.close();
			}
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			log.error("Interruption while closing BKHttpServiceProvider", ie);
			throw new IOException("Interruption while closing BKHttpServiceProvider", ie);
		} catch (BKException e) {
			log.error("Error while closing BKHttpServiceProvider", e);
			throw new IOException("Error while closing BKHttpServiceProvider", e);
		}
	}

	private ServerConfiguration getServerConf() {
		return serverConf;
	}

	private Auditor getAuditor() {
		return autoRecovery == null ? null : autoRecovery.getAuditor();
	}

	private Bookie getBookie() {
		return bookieServer == null ? null : bookieServer.getBookie();
	}

	/**
	 * Builder for HttpServiceProvider.
	 */
	public static class Builder {

		BookieServer bookieServer = null;
		AutoRecoveryMain autoRecovery = null;
		LedgerManagerFactory ledgerManagerFactory = null;
		ServerConfiguration serverConf = null;
		StatsProvider statsProvider = null;

		public Builder setBookieServer(BookieServer bookieServer) {
			this.bookieServer = bookieServer;
			return this;
		}

		public Builder setAutoRecovery(AutoRecoveryMain autoRecovery) {
			this.autoRecovery = autoRecovery;
			return this;
		}

		public Builder setServerConfiguration(ServerConfiguration conf) {
			this.serverConf = conf;
			return this;
		}

		public Builder setStatsProvider(StatsProvider statsProvider) {
			this.statsProvider = statsProvider;
			return this;
		}

		public Builder setLedgerManagerFactory(LedgerManagerFactory ledgerManagerFactory) {
			this.ledgerManagerFactory = ledgerManagerFactory;
			return this;
		}

		public BKHttpServiceProvider build() throws IOException, KeeperException, InterruptedException, BKException {
			return new BKHttpServiceProvider(bookieServer, autoRecovery, ledgerManagerFactory, serverConf,
					statsProvider);
		}
	}

	@Override
	public HttpEndpointService provideHttpEndpointService(ApiType type) {
		ServerConfiguration configuration = getServerConf();
		if (configuration == null) {
			return new ErrorHttpService();
		}

		switch (type) {
		case HEARTBEAT:
			return new HeartbeatService();
		case SERVER_CONFIG:
			return new ConfigurationService(configuration);
		case METRICS:
			return new MetricsService(configuration, statsProvider);

		// ledger
		case DELETE_LEDGER:
			return new DeleteLedgerService(configuration);
		case LIST_LEDGER:
			return new ListLedgerService(configuration, ledgerManagerFactory);
		case GET_LEDGER_META:
			return new GetLedgerMetaService(configuration, ledgerManagerFactory);
		case READ_LEDGER_ENTRY:
			return new ReadLedgerEntryService(configuration, bka);

		// bookie
		case LIST_BOOKIES:
			return new ListBookiesService(configuration, bka);
		case LIST_BOOKIE_INFO:
			return new ListBookieInfoService(configuration);
		case LAST_LOG_MARK:
			return new GetLastLogMarkService(configuration);
		case LIST_DISK_FILE:
			return new ListDiskFilesService(configuration);
		case EXPAND_STORAGE:
			return new ExpandStorageService(configuration);
		case GC:
			return new TriggerGCService(configuration, bookieServer);
		case GC_DETAILS:
			return new GCDetailsService(configuration, bookieServer);
		case BOOKIE_STATE:
			return new BookieStateService(bookieServer.getBookie());
		case BOOKIE_STATE_READONLY:
			return new BookieStateReadOnlyService(bookieServer.getBookie());
		case BOOKIE_IS_READY:
			return new BookieIsReadyService(bookieServer.getBookie());
		case BOOKIE_INFO:
			return new BookieInfoService(bookieServer.getBookie());

		// autorecovery
		case AUTORECOVERY_STATUS:
			return new AutoRecoveryStatusService(configuration);
		case RECOVERY_BOOKIE:
			return new RecoveryBookieService(configuration, bka, executor);
		case LIST_UNDER_REPLICATED_LEDGER:
			return new ListUnderReplicatedLedgerService(configuration, ledgerManagerFactory);
		case WHO_IS_AUDITOR:
			return new WhoIsAuditorService(configuration, bka);
		case TRIGGER_AUDIT:
			return new TriggerAuditService(configuration, bka);
		case LOST_BOOKIE_RECOVERY_DELAY:
			return new LostBookieRecoveryDelayService(configuration, bka);
		case DECOMMISSION:
			return new DecommissionService(configuration, bka, executor);

		default:
			return new ConfigurationService(configuration);
		}
	}

}
