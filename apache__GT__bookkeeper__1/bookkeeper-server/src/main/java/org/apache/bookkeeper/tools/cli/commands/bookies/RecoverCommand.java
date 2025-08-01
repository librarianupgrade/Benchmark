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
package org.apache.bookkeeper.tools.cli.commands.bookies;

import static org.apache.bookkeeper.meta.MetadataDrivers.runFunctionWithRegistrationManager;

import com.beust.jcommander.Parameter;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.bookkeeper.bookie.BookieException;
import org.apache.bookkeeper.bookie.Cookie;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.discover.RegistrationManager;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.tools.cli.helpers.BookieCommand;
import org.apache.bookkeeper.tools.framework.CliFlags;
import org.apache.bookkeeper.tools.framework.CliSpec;
import org.apache.bookkeeper.util.IOUtils;
import org.apache.bookkeeper.versioning.Versioned;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to ledger data recovery for failed bookie.
 */
public class RecoverCommand extends BookieCommand<RecoverCommand.RecoverFlags> {

	private static final Logger LOG = LoggerFactory.getLogger(RecoverCommand.class);

	private static final String NAME = "recover";
	private static final String DESC = "Recover the ledger data for failed bookie";

	private static final long DEFAULT_ID = -1L;

	public RecoverCommand() {
		this(new RecoverFlags());
	}

	private RecoverCommand(RecoverFlags flags) {
		super(CliSpec.<RecoverFlags>newBuilder().withName(NAME).withDescription(DESC).withFlags(flags).build());
	}

	/**
	 * Flags for recover command.
	 */
	@Accessors(fluent = true)
	@Setter
	public static class RecoverFlags extends CliFlags {

		@Parameter(names = { "-l", "--ledger" }, description = "Recover a specific ledger")
		private long ledger = DEFAULT_ID;

		@Parameter(names = { "-f", "--force" }, description = "Force recovery without confirmation")
		private boolean force;

		@Parameter(names = { "-q", "--query" }, description = "Query the ledgers that contain given bookies")
		private boolean query;

		@Parameter(names = { "-dr", "--drarun" }, description = "Printing the recovery plan w/o doing actual recovery")
		private boolean dryRun;

		@Parameter(names = { "-sk", "--skipopenledgers" }, description = "Skip recovering open ledgers")
		private boolean skipOpenLedgers;

		@Parameter(names = { "-d", "--deletecookie" }, description = "Delete cookie node for the bookie")
		private boolean deleteCookie;

		@Parameter(names = { "-bs", "--bokiesrc" }, description = "Bookie address")
		private String bookieAddress;

		@Parameter(names = { "-sku", "--skipunrecoverableledgers" }, description = "Skip unrecoverable ledgers")
		private boolean skipUnrecoverableLedgers;

		@Parameter(names = { "-rate", "--replicationrate" }, description = "Replication rate in bytes")
		private int replicateRate;
	}

	@Override
	public boolean apply(ServerConfiguration conf, RecoverFlags cmdFlags) {
		try {
			return recover(conf, cmdFlags);
		} catch (Exception e) {
			throw new UncheckedExecutionException(e.getMessage(), e);
		}
	}

	private boolean recover(ServerConfiguration conf, RecoverFlags flags)
			throws IOException, BKException, InterruptedException, KeeperException {
		boolean query = flags.query;
		boolean dryrun = flags.dryRun;
		boolean force = flags.force;
		boolean skipOpenLedgers = flags.skipOpenLedgers;
		boolean removeCookies = !dryrun && flags.deleteCookie;
		boolean skipUnrecoverableLedgers = flags.skipUnrecoverableLedgers;

		Long ledgerId = flags.ledger;
		int replicateRate = flags.replicateRate;

		// Get bookies list
		final String[] bookieStrs = flags.bookieAddress.split(",");
		final Set<BookieId> bookieAddrs = new HashSet<>();
		for (String bookieStr : bookieStrs) {
			try {
				bookieAddrs.add(BookieId.parse(bookieStr));
			} catch (IllegalArgumentException err) {
				LOG.error("BookieSrcs has invalid bookie id format: {}", bookieStr);
				return false;
			}
		}

		if (!force) {
			LOG.error("Bookies : {}", bookieAddrs);
			if (!IOUtils.confirmPrompt("Are you sure to recover them : (Y/N)")) {
				LOG.error("Give up!");
				return false;
			}
		}

		LOG.info("Constructing admin");
		conf.setReplicationRateByBytes(replicateRate);
		ClientConfiguration adminConf = new ClientConfiguration(conf);
		BookKeeperAdmin admin = new BookKeeperAdmin(adminConf);
		LOG.info("Construct admin : {}", admin);
		try {
			if (query) {
				return bkQuery(admin, bookieAddrs);
			}
			if (DEFAULT_ID != ledgerId) {
				return bkRecoveryLedger(admin, ledgerId, bookieAddrs, dryrun, skipOpenLedgers, removeCookies);
			}
			return bkRecovery(admin, bookieAddrs, dryrun, skipOpenLedgers, removeCookies, skipUnrecoverableLedgers);
		} finally {
			admin.close();
		}
	}

	private boolean bkQuery(BookKeeperAdmin bkAdmin, Set<BookieId> bookieAddrs)
			throws InterruptedException, BKException {
		SortedMap<Long, LedgerMetadata> ledgersContainBookies = bkAdmin.getLedgersContainBookies(bookieAddrs);
		LOG.error("NOTE: Bookies in inspection list are marked with '*'.");
		for (Map.Entry<Long, LedgerMetadata> ledger : ledgersContainBookies.entrySet()) {
			LOG.info("ledger {} : {}", ledger.getKey(), ledger.getValue().getState());
			Map<Long, Integer> numBookiesToReplacePerEnsemble = inspectLedger(ledger.getValue(), bookieAddrs);
			LOG.info("summary: [");
			for (Map.Entry<Long, Integer> entry : numBookiesToReplacePerEnsemble.entrySet()) {
				LOG.info("{}={}, ", entry.getKey(), entry.getValue());
			}
			LOG.info("]");
			LOG.info("");
		}
		LOG.error("Done");
		return true;
	}

	private Map<Long, Integer> inspectLedger(LedgerMetadata metadata, Set<BookieId> bookiesToInspect) {
		Map<Long, Integer> numBookiesToReplacePerEnsemble = new TreeMap<Long, Integer>();
		for (Map.Entry<Long, ? extends List<BookieId>> ensemble : metadata.getAllEnsembles().entrySet()) {
			List<BookieId> bookieList = ensemble.getValue();
			LOG.info("{}:\t", ensemble.getKey());
			int numBookiesToReplace = 0;
			for (BookieId bookie : bookieList) {
				LOG.info("{}", bookie.toString());
				if (bookiesToInspect.contains(bookie)) {
					LOG.info("*");
					++numBookiesToReplace;
				} else {
					LOG.info(" ");
				}
				LOG.info(" ");
			}
			LOG.info("");
			numBookiesToReplacePerEnsemble.put(ensemble.getKey(), numBookiesToReplace);
		}
		return numBookiesToReplacePerEnsemble;
	}

	private boolean bkRecoveryLedger(BookKeeperAdmin bkAdmin, long lid, Set<BookieId> bookieAddrs, boolean dryrun,
			boolean skipOpenLedgers, boolean removeCookies) throws InterruptedException, BKException {
		bkAdmin.recoverBookieData(lid, bookieAddrs, dryrun, skipOpenLedgers);
		if (removeCookies) {
			deleteCookies(bkAdmin.getConf(), bookieAddrs);
		}
		return true;
	}

	private void deleteCookies(ClientConfiguration conf, Set<BookieId> bookieAddrs) throws BKException {
		ServerConfiguration serverConf = new ServerConfiguration(conf);
		try {
			runFunctionWithRegistrationManager(serverConf, rm -> {
				try {
					for (BookieId addr : bookieAddrs) {
						deleteCookie(rm, addr);
					}
				} catch (Exception e) {
					throw new UncheckedExecutionException(e);
				}
				return null;
			});
		} catch (Exception e) {
			Throwable cause = e;
			if (e instanceof UncheckedExecutionException) {
				cause = e.getCause();
			}
			if (cause instanceof BKException) {
				throw (BKException) cause;
			} else {
				BKException bke = new BKException.MetaStoreException();
				bke.initCause(bke);
				throw bke;
			}
		}

	}

	private void deleteCookie(RegistrationManager rm, BookieId bookieSrc) throws BookieException {
		try {
			Versioned<Cookie> cookie = Cookie.readFromRegistrationManager(rm, bookieSrc);
			cookie.getValue().deleteFromRegistrationManager(rm, bookieSrc, cookie.getVersion());
		} catch (BookieException.CookieNotFoundException nne) {
			LOG.warn("No cookie to remove for {} : ", bookieSrc, nne);
		}
	}

	private boolean bkRecovery(BookKeeperAdmin bkAdmin, Set<BookieId> bookieAddrs, boolean dryrun,
			boolean skipOpenLedgers, boolean removeCookies, boolean skipUnrecoverableLedgers)
			throws InterruptedException, BKException {
		bkAdmin.recoverBookieData(bookieAddrs, dryrun, skipOpenLedgers, skipUnrecoverableLedgers);
		if (removeCookies) {
			deleteCookies(bkAdmin.getConf(), bookieAddrs);
		}
		return true;
	}
}
