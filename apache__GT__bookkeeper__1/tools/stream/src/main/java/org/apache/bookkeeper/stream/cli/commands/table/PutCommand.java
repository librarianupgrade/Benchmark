/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.bookkeeper.stream.cli.commands.table;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.bookkeeper.common.concurrent.FutureUtils.result;
import static org.apache.bookkeeper.stream.cli.Commands.OP_PUT;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.bookkeeper.api.StorageClient;
import org.apache.bookkeeper.api.kv.Table;
import org.apache.bookkeeper.stream.cli.commands.ClientCommand;
import org.apache.bookkeeper.stream.cli.commands.table.PutCommand.Flags;
import org.apache.bookkeeper.tools.framework.CliFlags;
import org.apache.bookkeeper.tools.framework.CliSpec;

/**
 * Commands to put kvs.
 */
public class PutCommand extends ClientCommand<Flags> {

	private static final String NAME = OP_PUT;
	private static final String DESC = "Put key/value pair to a table";

	/**
	 * Flags of the put command.
	 */
	public static class Flags extends CliFlags {
	}

	public PutCommand() {
		super(CliSpec.<Flags>newBuilder().withName(NAME).withDescription(DESC).withFlags(new Flags())
				.withArgumentsUsage("<table> <key> <value>").build());
	}

	@Override
	@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
	protected void run(StorageClient client, Flags flags) throws Exception {
		checkArgument(flags.arguments.size() >= 3, "table and key/value are not provided");

		String tableName = flags.arguments.get(0);
		String key = flags.arguments.get(1);
		String value = flags.arguments.get(2);

		try (Table<ByteBuf, ByteBuf> table = result(client.openTable(tableName))) {
			result(table.put(Unpooled.wrappedBuffer(key.getBytes(UTF_8)),
					Unpooled.wrappedBuffer(value.getBytes(UTF_8))));
			spec.console().println("Successfully update kv: ('" + key + "', '" + value + "').");
		}
	}

}
