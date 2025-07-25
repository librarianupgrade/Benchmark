/**
 * Copyright 2012 Comcast Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.comcast.cqs.controller;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

import javax.servlet.AsyncContext;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.comcast.cmb.common.persistence.PersistenceFactory;
import com.comcast.cmb.common.util.CMBProperties;
import com.comcast.cqs.io.CQSMessagePopulator;
import com.comcast.cqs.model.CQSMessage;
import com.comcast.cqs.model.CQSQueue;

public class CQSLongPollReceiver {

	private static Logger logger = Logger.getLogger(CQSLongPollReceiver.class);
	private static boolean initialized = false;

	private static ChannelFactory serverSocketChannelFactory;

	//public static volatile ConcurrentHashMap<String,Object> queueMonitors; 

	public static volatile ConcurrentHashMap<String, ConcurrentLinkedQueue<AsyncContext>> contextQueues;

	//
	// long poll design:
	//
	// http request handlers are now asynchronous: they offload action execution to a separate pool of worker threads
	// there is one single long poll receiver thread per api server listening on a dedicated port for "message available" notifications using netty nio library (asynchronous i/o)
	// each long polling receive() api call does a wait(timeout) on a monitor (one monitor per queue)
	// when the long poll receiver thread receives a notification (which only consists of the queue arn that received a message) it will do a notify() on the monitor associated with the queue
	// this wakes up at most one waiting receive() api call, which will then try to read and return messages from redis/cassandra as usual
	// if no messages are found (race conditions etc.) and there is still long polling time left, receive will call wait() again
	// each send() api call will write the message to redis/cassandra as usual and send the target queue arn to all api servers using the netty nio library
	// each api server will write a heart beat (timestamp, ip, port) to cassandra, maybe once a minute or so
	// each api server will read the heart beat table once a minute or so to be aware of active api servers and their ip:port combinations
	//
	// known limitations:
	//
	// each long poll request occupies a waiting thread on the worker pool
	// no short cut if send and receive happens on same api server (could add that as performance improvement)
	// receive publishes to all api servers regardless of whether they are actually listening or not (to save management overhead)
	// long poll receiver thread is single point of failure on api server
	//
	// redesign to allow for true async implementation:
	//
	// prerequisite: use application level request or response wrappers to allow additional meta-data to travel with the async context, such as: 
	//   long requestReceivedMillis (timestamp when request was received)
	//   boolean requestActive (true at first but false if request times out or ends in error, set by async event handlers)
	//   boolean completed (only set to true by ReceiveMessage api as all other apis are just run in pseudo-asynchronous mode)
	// upon receive() do everything as usual up until reading messages from redis/cassandra
	// if messages are found immediately, return these messages and complete the async context as usual 
	// otherwise put async context on in-memory queue (e.g. ConcurrentLinkedQueue) and do NOT complete context (note: we need one in-mem queue per cqs queue, referenced via a concurrent hash map!)
	// when any of the async events occurs (complete, timeout, error) we mark the async context as outdated
	// when receiving an external send() notification we look up the correct in-mem queue and pull an async context from it
	//   if no context there we do nothing (nobody is currently long-polling)
	//   if a context is there but it's marked as outdated we simply discard it and check for further elements on the queue
	//   if an active async context is found we try to read messages
	//     if messages are found we generate a response and complete the context, potentially we could keep going as long as we can find messages
	//     if no messages are found (and there is long poll time left) we put the context back on the queue
	//
	// miscellaneous improvements:
	//
	// reuse established netty channels instead of creating new tcp connections for every lp sendmessage() call - use blocking queue and single 
	// long poll sender process reestablishing connections only on failure or after a set period of time (e.g. 60 sec)
	// long poll receivemessage() to check for messages on timeout
	// only send notifications to service endpoints with receint lp receivemessage() calls
	// sensible tcp default ettings for client and server
	//

	private static class LongPollServerHandler extends SimpleChannelHandler {

		StringBuffer queueArn = new StringBuffer("");

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

			ChannelBuffer buf = (ChannelBuffer) e.getMessage();

			while (buf.readable()) {

				char c = ((char) buf.readByte());

				if (c == ';') {

					processNotification(queueArn.toString(),
							e.getRemoteAddress() != null ? e.getRemoteAddress().toString() : "");

					// start reading new message

					queueArn = new StringBuffer("");

					/*if (monitor != null) {
						synchronized (monitor) {
							logger.info("event=notifying_thread");
							monitor.notify();
						}
					}*/

				} else {
					queueArn.append(c);
				}
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
			logger.error("event=longpoll_receiver_error", e.getCause());
			e.getChannel().close();
		}
	}

	public static void processNotification(String queueArn, String remoteAddress) {

		//Object monitor = queueMonitors.get(queueArn);

		contextQueues.putIfAbsent(queueArn, new ConcurrentLinkedQueue<AsyncContext>());
		ConcurrentLinkedQueue<AsyncContext> contextQueue = contextQueues.get(queueArn);

		AsyncContext asyncContext = contextQueue.poll();

		if (asyncContext == null) {
			logger.debug("event=no_pending_receive queue_arn=" + queueArn + " remote_address=" + remoteAddress);
			return;
		}

		if (asyncContext.getRequest() == null) {
			logger.info("event=skipping_invalid_context queue_arn=" + queueArn + " remote_address=" + remoteAddress);
			return;
		}

		if (!(asyncContext.getRequest() instanceof CQSHttpServletRequest)) {
			logger.info("event=skipping_invalid_request queue_arn=" + queueArn + " remote_address=" + remoteAddress);
			return;
		}

		CQSHttpServletRequest request = (CQSHttpServletRequest) asyncContext.getRequest();

		// skip if request is already finished or outdated

		if (!request.isActive()
				|| System.currentTimeMillis() - request.getRequestReceivedTimestamp() > request.getWaitTime()) {
			logger.info("event=skipping_outdated_context queue_arn=" + queueArn + " remote_address=" + remoteAddress);
			return;
		}

		logger.debug("event=notification_received queue_arn=" + queueArn + " remote_address=" + remoteAddress);

		try {

			CQSQueue queue = request.getQueue();
			List<CQSMessage> messageList = PersistenceFactory.getCQSMessagePersistence().receiveMessage(queue,
					request.getReceiveAttributes());

			if (messageList.size() > 0) {

				StringBuffer handles = new StringBuffer("");

				for (CQSMessage message : messageList) {
					handles.append(message.getReceiptHandle() + ",");
				}

				logger.info(
						"event=messages_found_for_longpoll_receive receipt_handles=" + handles.toString() + " count="
								+ messageList.size() + " queue_arn=" + queueArn + " remote_address=" + remoteAddress);

				CQSMonitor.getInstance().addNumberOfMessagesReturned(queue.getRelativeUrl(), messageList.size());
				String out = CQSMessagePopulator.getReceiveMessageResponseAfterSerializing(messageList,
						request.getFilterAttributes());
				asyncContext.getResponse().getWriter().println(out);
				asyncContext.complete();

			} else {

				// if there's longpoll time left, put back on queue

				if (request.getWaitTime() - System.currentTimeMillis() + request.getRequestReceivedTimestamp() > 0) {
					logger.info("event=no_messages_found_for_longpoll_receive action=re_queueing time_left_ms="
							+ (request.getWaitTime() - System.currentTimeMillis()
									+ request.getRequestReceivedTimestamp())
							+ " queue_arn=" + queueArn + " remote_address=" + remoteAddress);
					contextQueue.offer(asyncContext);
				}
			}

		} catch (Exception ex) {
			logger.error("event=longpoll_queue_error queue_arn=" + queueArn, ex);
		}
	}

	public static void listen() {

		if (!initialized) {

			//queueMonitors = new ConcurrentHashMap<String,Object>();

			contextQueues = new ConcurrentHashMap<String, ConcurrentLinkedQueue<AsyncContext>>();

			serverSocketChannelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool());

			ServerBootstrap serverBootstrap = new ServerBootstrap(serverSocketChannelFactory);

			serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				public ChannelPipeline getPipeline() {
					return Channels.pipeline(new LongPollServerHandler());
				}
			});

			serverBootstrap.setOption("child.tcpNoDelay", true);
			serverBootstrap.setOption("child.keepAlive", true);

			serverBootstrap.bind(new InetSocketAddress(CMBProperties.getInstance().getCQSLongPollPort()));

			initialized = true;

			logger.info("event=longpoll_receiver_service_listening port="
					+ CMBProperties.getInstance().getCQSLongPollPort());
		}
	}

	public static void shutdown() {
		if (serverSocketChannelFactory != null) {
			serverSocketChannelFactory.releaseExternalResources();
		}
	}
}
