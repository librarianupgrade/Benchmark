/**
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
package org.apache.bookkeeper.proto;

import io.netty.channel.Channel;
import java.util.concurrent.TimeUnit;
import org.apache.bookkeeper.proto.BookieProtocol.Request;
import org.apache.bookkeeper.stats.OpStatsLogger;
import org.apache.bookkeeper.util.MathUtils;
import org.apache.bookkeeper.util.SafeRunnable;
import org.apache.bookkeeper.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for bookeeper packet processors.
 */
abstract class PacketProcessorBase<T extends Request> extends SafeRunnable {
	private static final Logger logger = LoggerFactory.getLogger(PacketProcessorBase.class);
	T request;
	Channel channel;
	BookieRequestProcessor requestProcessor;
	long enqueueNanos;

	protected void init(T request, Channel channel, BookieRequestProcessor requestProcessor) {
		this.request = request;
		this.channel = channel;
		this.requestProcessor = requestProcessor;
		this.enqueueNanos = MathUtils.nowInNano();
	}

	protected void reset() {
		request = null;
		channel = null;
		requestProcessor = null;
		enqueueNanos = -1;
	}

	protected boolean isVersionCompatible() {
		byte version = request.getProtocolVersion();
		if (version < BookieProtocol.LOWEST_COMPAT_PROTOCOL_VERSION
				|| version > BookieProtocol.CURRENT_PROTOCOL_VERSION) {
			logger.error("Invalid protocol version, expected something between "
					+ BookieProtocol.LOWEST_COMPAT_PROTOCOL_VERSION + " & " + BookieProtocol.CURRENT_PROTOCOL_VERSION
					+ ". got " + request.getProtocolVersion());
			return false;
		}
		return true;
	}

	protected void sendWriteReqResponse(int rc, Object response, OpStatsLogger statsLogger) {
		sendResponse(rc, response, statsLogger);
		requestProcessor.onAddRequestFinish();
	}

	protected void sendReadReqResponse(int rc, Object response, OpStatsLogger statsLogger, boolean throttle) {
		if (throttle) {
			sendResponseAndWait(rc, response, statsLogger);
		} else {
			sendResponse(rc, response, statsLogger);
		}
		requestProcessor.onReadRequestFinish();
	}

	protected void sendResponse(int rc, Object response, OpStatsLogger statsLogger) {
		final long writeNanos = MathUtils.nowInNano();

		final long timeOut = requestProcessor.getWaitTimeoutOnBackpressureMillis();
		if (timeOut >= 0 && !channel.isWritable()) {
			if (!requestProcessor.isBlacklisted(channel)) {
				synchronized (channel) {
					if (!channel.isWritable() && !requestProcessor.isBlacklisted(channel)) {
						final long waitUntilNanos = writeNanos + TimeUnit.MILLISECONDS.toNanos(timeOut);
						while (!channel.isWritable() && MathUtils.nowInNano() < waitUntilNanos) {
							try {
								TimeUnit.MILLISECONDS.sleep(1);
							} catch (InterruptedException e) {
								break;
							}
						}
						if (!channel.isWritable()) {
							requestProcessor.blacklistChannel(channel);
							requestProcessor.handleNonWritableChannel(channel);
						}
					}
				}
			}

			if (!channel.isWritable()) {
				LOGGER.warn("cannot write response to non-writable channel {} for request {}", channel,
						StringUtils.requestToString(request));
				requestProcessor.getRequestStats().getChannelWriteStats()
						.registerFailedEvent(MathUtils.elapsedNanos(writeNanos), TimeUnit.NANOSECONDS);
				statsLogger.registerFailedEvent(MathUtils.elapsedNanos(enqueueNanos), TimeUnit.NANOSECONDS);
				return;
			} else {
				requestProcessor.invalidateBlacklist(channel);
			}
		}

		if (channel.isActive()) {
			channel.writeAndFlush(response, channel.voidPromise());
		} else {
			LOGGER.debug("Netty channel {} is inactive, "
					+ "hence bypassing netty channel writeAndFlush during sendResponse", channel);
		}
		if (BookieProtocol.EOK == rc) {
			statsLogger.registerSuccessfulEvent(MathUtils.elapsedNanos(enqueueNanos), TimeUnit.NANOSECONDS);
		} else {
			statsLogger.registerFailedEvent(MathUtils.elapsedNanos(enqueueNanos), TimeUnit.NANOSECONDS);
		}
	}

	/**
	 * Write on the channel and wait until the write is completed.
	 *
	 * <p>That will make the thread to get blocked until we're able to
	 * write everything on the TCP stack, providing auto-throttling
	 * and avoiding using too much memory when handling read-requests.
	 */
	protected void sendResponseAndWait(int rc, Object response, OpStatsLogger statsLogger) {
		try {
			channel.writeAndFlush(response).await();
		} catch (InterruptedException e) {
			return;
		}

		if (BookieProtocol.EOK == rc) {
			statsLogger.registerSuccessfulEvent(MathUtils.elapsedNanos(enqueueNanos), TimeUnit.NANOSECONDS);
		} else {
			statsLogger.registerFailedEvent(MathUtils.elapsedNanos(enqueueNanos), TimeUnit.NANOSECONDS);
		}
	}

	@Override
	public void safeRun() {
		requestProcessor.getRequestStats().getWriteThreadQueuedLatency()
				.registerSuccessfulEvent(MathUtils.elapsedNanos(enqueueNanos), TimeUnit.NANOSECONDS);
		if (!isVersionCompatible()) {
			sendResponse(BookieProtocol.EBADVERSION,
					ResponseBuilder.buildErrorResponse(BookieProtocol.EBADVERSION, request),
					requestProcessor.getRequestStats().getReadRequestStats());
			if (request instanceof BookieProtocol.ReadRequest) {
				requestProcessor.onReadRequestFinish();
			}
			if (request instanceof BookieProtocol.AddRequest) {
				requestProcessor.onAddRequestFinish();
			}
			return;
		}
		processPacket();
	}

	protected abstract void processPacket();
}
