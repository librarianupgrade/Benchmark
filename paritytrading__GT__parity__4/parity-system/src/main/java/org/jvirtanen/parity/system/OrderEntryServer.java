package org.jvirtanen.parity.system;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

class OrderEntryServer {

	private ServerSocketChannel serverChannel;

	private MatchingEngine engine;

	private OrderEntryServer(ServerSocketChannel serverChannel, MatchingEngine engine) {
		this.serverChannel = serverChannel;

		this.engine = engine;
	}

	public static OrderEntryServer create(int port, MatchingEngine engine) throws IOException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();

		serverChannel.bind(new InetSocketAddress(port));
		serverChannel.configureBlocking(false);

		return new OrderEntryServer(serverChannel, engine);
	}

	public ServerSocketChannel getChannel() {
		return serverChannel;
	}

	public Session accept() throws IOException {
		SocketChannel channel = serverChannel.accept();
		if (channel == null)
			return null;

		channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		channel.configureBlocking(false);

		return new Session(channel, engine);
	}

}
