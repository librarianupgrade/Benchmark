package org.zeromq.api;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zeromq.jzmq.ManagedContext;

import static org.junit.Assert.assertArrayEquals;

public class ProxyTest {
	private Context context;

	@Before
	public void setUp() {
		context = new ManagedContext();
	}

	@After
	public void tearDown() throws Exception {
		context.terminate();
		context.close();
	}

	@Test
	public void testProxy() throws Exception {
		//have to do this in a separate thread, since the proxy method blocks until the context is closed.
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Socket requesterEndpoint = context.buildSocket(SocketType.ROUTER).bind("inproc://requests");
				Socket workerEndpoint = context.buildSocket(SocketType.DEALER).bind("inproc://work");
				context.proxy(workerEndpoint, requesterEndpoint);
			}
		});
		thread.start();
		//give it a bit of time to start up.
		Thread.sleep(100L);

		Socket client = this.context.buildSocket(SocketType.REQ).connect("inproc://requests");
		Socket worker = this.context.buildSocket(SocketType.REP).connect("inproc://work");
		client.send("hello".getBytes());
		byte[] response = worker.receive();
		assertArrayEquals("hello".getBytes(), response);

		worker.send("goodbye".getBytes());
		byte[] response2 = client.receive();
		assertArrayEquals("goodbye".getBytes(), response2);
	}

}
