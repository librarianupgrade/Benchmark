package org.zeromq.api;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zeromq.jzmq.ManagedContext;

import static junit.framework.Assert.assertEquals;

public class PubSubTest {
	private ManagedContext context;

	@Before
	public void setUp() {
		context = new ManagedContext();
	}

	@After
	public void tearDown() throws Exception {
		context.close();
	}

	@Test(timeout = 1000)
	public void testPubSub() throws Exception {
		Socket publisher = context.buildSocket(SocketType.PUB).bind("inproc://publisher");
		Socket subscriber = context.buildSocket(SocketType.SUB).asSubscribable().subscribe("H".getBytes())
				.connect("inproc://publisher");
		publisher.send("Hello".getBytes());
		byte[] contents = subscriber.receive();
		assertEquals("Hello", new String(contents));
	}

	@Test(timeout = 1000)
	public void testMultipleSubscribers() throws Exception {
		Socket publisher = context.buildSocket(SocketType.PUB).bind("inproc://publisher");
		Socket subscriber1 = context.buildSocket(SocketType.SUB).asSubscribable().subscribe("H".getBytes())
				.connect("inproc://publisher");
		Socket subscriber2 = context.buildSocket(SocketType.SUB).asSubscribable().subscribe("H".getBytes())
				.connect("inproc://publisher");
		publisher.send("Hello".getBytes());
		assertEquals("Hello", new String(subscriber1.receive()));
		assertEquals("Hello", new String(subscriber2.receive()));
	}
}
