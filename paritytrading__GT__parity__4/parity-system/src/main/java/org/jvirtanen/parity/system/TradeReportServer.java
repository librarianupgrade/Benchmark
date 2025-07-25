package org.jvirtanen.parity.system;

import static org.jvirtanen.parity.util.Applications.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import org.jvirtanen.nassau.moldudp64.MoldUDP64DefaultMessageStore;
import org.jvirtanen.nassau.moldudp64.MoldUDP64DownstreamPacket;
import org.jvirtanen.nassau.moldudp64.MoldUDP64RequestServer;
import org.jvirtanen.nassau.moldudp64.MoldUDP64Server;
import org.jvirtanen.parity.net.ptr.PTR;

class TradeReportServer {

	private PTR.Trade trade;

	private MoldUDP64Server transport;

	private MoldUDP64RequestServer requestTransport;

	private MoldUDP64DefaultMessageStore messages;

	private MoldUDP64DownstreamPacket packet;

	private ByteBuffer buffer;

	private TradeReportServer(MoldUDP64Server transport, MoldUDP64RequestServer requestTransport) {
		this.trade = new PTR.Trade();

		this.transport = transport;

		this.requestTransport = requestTransport;

		this.messages = new MoldUDP64DefaultMessageStore();

		this.packet = new MoldUDP64DownstreamPacket();
		this.buffer = ByteBuffer.allocate(1024);
	}

	public static TradeReportServer create(String session, InetSocketAddress multicastGroup, int requestPort)
			throws IOException {
		DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);

		channel.connect(multicastGroup);

		MoldUDP64Server transport = new MoldUDP64Server(channel, session);

		DatagramChannel requestChannel = DatagramChannel.open();

		requestChannel.bind(new InetSocketAddress(requestPort));
		requestChannel.configureBlocking(false);

		MoldUDP64RequestServer requestTransport = new MoldUDP64RequestServer(requestChannel);

		return new TradeReportServer(transport, requestTransport);
	}

	public MoldUDP64Server getTransport() {
		return transport;
	}

	public MoldUDP64RequestServer getRequestTransport() {
		return requestTransport;
	}

	public void serve() {
		try {
			requestTransport.serve(messages);
		} catch (IOException e) {
			fatal(e);
		}
	}

	public void trade(long matchNumber, long instrument, long quantity, long price, long buyer, long buyOrderNumber,
			long seller, long sellOrderNumber) {
		long currentTimeMillis = System.currentTimeMillis() - TradingSystem.EPOCH_MILLIS;

		trade.timestamp = currentTimeMillis * 1000 * 1000;
		trade.matchNumber = matchNumber;
		trade.instrument = instrument;
		trade.quantity = quantity;
		trade.price = price;
		trade.buyer = buyer;
		trade.buyOrderNumber = buyOrderNumber;
		trade.seller = seller;
		trade.sellOrderNumber = sellOrderNumber;

		buffer.clear();
		trade.put(buffer);
		buffer.flip();

		try {
			packet.put(buffer);

			transport.send(packet);

			packet.payload().flip();

			messages.put(packet);

			packet.clear();
		} catch (IOException e) {
			fatal(e);
		}
	}

}
