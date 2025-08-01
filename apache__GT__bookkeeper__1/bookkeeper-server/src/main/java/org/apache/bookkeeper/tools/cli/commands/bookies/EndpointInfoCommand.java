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

import com.beust.jcommander.Parameter;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.IOException;
import java.util.Collection;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeperAdmin;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.apache.bookkeeper.discover.BookieServiceInfo;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.tools.cli.helpers.BookieCommand;
import org.apache.bookkeeper.tools.framework.CliFlags;
import org.apache.bookkeeper.tools.framework.CliSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get endpoint information about a Bookie.
 */
public class EndpointInfoCommand extends BookieCommand<EndpointInfoCommand.EndpointInfoFlags> {

	static final Logger LOG = LoggerFactory.getLogger(EndpointInfoCommand.class);

	private static final String NAME = "endpointinfo";
	private static final String DESC = "Get all end point information about a given bookie.";

	public EndpointInfoCommand() {
		this(new EndpointInfoFlags());
	}

	private EndpointInfoCommand(EndpointInfoFlags flags) {
		super(CliSpec.<EndpointInfoFlags>newBuilder().withName(NAME).withDescription(DESC).withFlags(flags).build());
	}

	/**
	 * Flags for this command.
	 */
	@Accessors(fluent = true)
	@Setter
	public static class EndpointInfoFlags extends CliFlags {

		@Parameter(required = true, names = { "-b",
				"--bookieid" }, description = "Get information about a remote bookie")
		private String bookie;

	}

	@Override
	public boolean apply(ServerConfiguration conf, EndpointInfoFlags cmdFlags) {
		try {
			return getEndpointInfo(conf, cmdFlags);
		} catch (Exception e) {
			throw new UncheckedExecutionException(e.getMessage(), e);
		}
	}

	private boolean getEndpointInfo(ServerConfiguration conf, EndpointInfoFlags flags)
			throws BKException, InterruptedException, IOException {
		ClientConfiguration adminConf = new ClientConfiguration(conf);
		BookKeeperAdmin admin = new BookKeeperAdmin(adminConf);
		try {
			final String bookieIdStr = flags.bookie;
			if (bookieIdStr == null || bookieIdStr.isEmpty()) {
				throw new IllegalArgumentException("BookieId is required");
			}
			BookieId bookieId = BookieId.parse(bookieIdStr);
			Collection<BookieId> allBookies = admin.getAllBookies();
			if (!allBookies.contains(bookieId)) {
				LOG.info("Bookie {} does not exist, only {}", bookieId, allBookies);
				return false;
			}
			BookieServiceInfo bookieServiceInfo = admin.getBookieServiceInfo(bookieId);

			LOG.info("BookiedId: {}", bookieId);
			if (!bookieServiceInfo.getProperties().isEmpty()) {
				LOG.info("Properties");
				bookieServiceInfo.getProperties().forEach((k, v) -> {
					LOG.info("{} : {}", k, v);
				});
			}
			if (!bookieServiceInfo.getEndpoints().isEmpty()) {
				bookieServiceInfo.getEndpoints().forEach(e -> {
					LOG.info("Endpoint: {}", e.getId());
					LOG.info("Protocol: {}", e.getProtocol());
					LOG.info("Address: {} : {}", e.getHost(), e.getPort());
					LOG.info("Auth: {}", e.getAuth());
					LOG.info("Extensions: {}", e.getExtensions());
				});
			} else {
				LOG.info("Bookie did not publish any endpoint info. Maybe it is down");
				return false;
			}

			return true;
		} catch (Exception e) {
			LOG.error("Received exception in EndpointInfoCommand ", e);
			return false;
		} finally {
			if (admin != null) {
				admin.close();
			}
		}
	}
}
