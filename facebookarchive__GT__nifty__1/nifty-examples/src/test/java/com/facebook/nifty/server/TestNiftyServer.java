/*
 * Copyright (C) 2012-2013 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.nifty.server;

import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.nifty.client.NiftyClient;
import com.facebook.nifty.core.NettyServerConfig;
import com.facebook.nifty.core.NettyServerTransport;
import com.facebook.nifty.core.RequestContext;
import com.facebook.nifty.core.RequestContexts;
import com.facebook.nifty.core.ThriftServerDefBuilder;
import com.facebook.nifty.test.LogEntry;
import com.facebook.nifty.test.ResultCode;
import com.facebook.nifty.test.scribe;
import io.airlift.log.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class TestNiftyServer {
	private static final Logger log = Logger.get(TestNiftyServer.class);
	private NettyServerTransport server;
	private int port;

	@BeforeMethod(alwaysRun = true)
	public void setup() {
		server = null;
	}

	@AfterMethod(alwaysRun = true)
	public void teardown() throws InterruptedException {
		if (server != null) {
			server.stop();
		}
	}

	private void startServer() {
		startServer(getThriftServerDefBuilder());
	}

	private void startServer(final ThriftServerDefBuilder thriftServerDefBuilder) {
		server = new NettyServerTransport(thriftServerDefBuilder.build(), NettyServerConfig.newBuilder().build(),
				new DefaultChannelGroup());
		server.start();
		port = ((InetSocketAddress) server.getServerChannel().getLocalAddress()).getPort();
	}

	private ThriftServerDefBuilder getThriftServerDefBuilder() {
		return new ThriftServerDefBuilder().listen(0).withProcessor(new scribe.Processor<>(new scribe.Iface() {
			@Override
			public ResultCode Log(List<LogEntry> messages) throws TException {
				RequestContext context = RequestContexts.getCurrentContext();

				for (LogEntry message : messages) {
					log.info("[Client: %s] %s: %s", context.getConnectionContext().getRemoteAddress(),
							message.getCategory(), message.getMessage());
				}
				return ResultCode.OK;
			}
		}));
	}

	private scribe.Client makeNiftyClient() throws TTransportException, InterruptedException {
		InetSocketAddress address = new InetSocketAddress("localhost", port);
		TTransport transport = new NiftyClient().connectSync(scribe.Client.class, new FramedClientConnector(address));
		TProtocol protocol = new TBinaryProtocol(transport);
		return new scribe.Client(protocol);
	}

	@Test
	public void testBasic() throws InterruptedException, TException {
		startServer();
		scribe.Client client1 = makeNiftyClient();
		Assert.assertEquals(client1.Log(Arrays.asList(new LogEntry("client1", "aaa"))), ResultCode.OK);
		Assert.assertEquals(client1.Log(Arrays.asList(new LogEntry("client1", "bbb"))), ResultCode.OK);
		scribe.Client client2 = makeNiftyClient();
		Assert.assertEquals(client2.Log(Arrays.asList(new LogEntry("client2", "ccc"))), ResultCode.OK);
	}

	@Test
	public void testMaxConnections() throws InterruptedException, TException {
		startServer(getThriftServerDefBuilder().limitConnectionsTo(1));
		scribe.Client client1 = makeNiftyClient();
		Assert.assertEquals(client1.Log(Arrays.asList(new LogEntry("client1", "aaa"))), ResultCode.OK);
		Assert.assertEquals(client1.Log(Arrays.asList(new LogEntry("client1", "bbb"))), ResultCode.OK);
		scribe.Client client2 = makeNiftyClient();
		try {
			client2.Log(Arrays.asList(new LogEntry("client2", "ccc")));
		} catch (TTransportException e) {
			// expected
		}
	}

	@Test
	public void testMaxConnections2() throws InterruptedException, TException {
		startServer(getThriftServerDefBuilder().limitConnectionsTo(1));
		scribe.Client client1 = makeNiftyClient();
		Assert.assertEquals(client1.Log(Arrays.asList(new LogEntry("client1", "aaa"))), ResultCode.OK);
		Assert.assertEquals(client1.Log(Arrays.asList(new LogEntry("client1", "bbb"))), ResultCode.OK);
		scribe.Client client2 = makeNiftyClient();
		try {
			client2.Log(Arrays.asList(new LogEntry("client2", "ccc")));
		} catch (TTransportException e) {
			// expected
		}
		// now need to make sure we didn't double-decrement the number of connections, so try again
		scribe.Client client3 = makeNiftyClient();
		try {
			client3.Log(Arrays.asList(new LogEntry("client3", "ddd")));
			Assert.fail();
		} catch (TTransportException e) {
			// expected
		}
	}
}
