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
package com.facebook.nifty.core;

import com.facebook.nifty.duplex.TDuplexProtocolFactory;
import com.facebook.nifty.duplex.TProtocolPair;
import com.facebook.nifty.duplex.TTransportPair;
import com.facebook.nifty.processor.NiftyProcessorFactory;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkState;

/**
 * Dispatch TNiftyTransport to the TProcessor and write output back.
 *
 * Note that all current async thrift clients are capable of sending multiple requests at once
 * but not capable of handling out-of-order responses to those requests, so this dispatcher
 * sends the requests in order. (Eventually this will be conditional on a flag in the thrift
 * message header for future async clients that can handle out-of-order responses).
 */
public class NiftyDispatcher extends SimpleChannelUpstreamHandler {
	private final NiftyProcessorFactory processorFactory;
	private final Executor exe;
	private final long taskTimeoutMillis;
	private final Timer taskTimeoutTimer;
	private final int queuedResponseLimit;
	private final Map<Integer, ThriftMessage> responseMap = new HashMap<>();
	private final AtomicInteger dispatcherSequenceId = new AtomicInteger(0);
	private final AtomicInteger lastResponseWrittenId = new AtomicInteger(0);
	private final TDuplexProtocolFactory duplexProtocolFactory;

	public NiftyDispatcher(ThriftServerDef def, Timer timer) {
		this.processorFactory = def.getProcessorFactory();
		this.duplexProtocolFactory = def.getDuplexProtocolFactory();
		this.queuedResponseLimit = def.getQueuedResponseLimit();
		this.exe = def.getExecutor();
		this.taskTimeoutMillis = (def.getTaskTimeout() == null ? 0 : def.getTaskTimeout().toMillis());
		this.taskTimeoutTimer = (def.getTaskTimeout() == null ? null : timer);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() instanceof ThriftMessage) {
			ThriftMessage message = (ThriftMessage) e.getMessage();
			if (taskTimeoutMillis > 0) {
				message.setProcessStartTimeMillis(System.currentTimeMillis());
			}
			checkResponseOrderingRequirements(ctx, message);

			TNiftyTransport messageTransport = new TNiftyTransport(ctx.getChannel(), message);
			TTransportPair transportPair = TTransportPair.fromSingleTransport(messageTransport);
			TProtocolPair protocolPair = duplexProtocolFactory.getProtocolPair(transportPair);
			TProtocol inProtocol = protocolPair.getInputProtocol();
			TProtocol outProtocol = protocolPair.getOutputProtocol();

			processRequest(ctx, message, messageTransport, inProtocol, outProtocol);
		} else {
			ctx.sendUpstream(e);
		}
	}

	private void checkResponseOrderingRequirements(ChannelHandlerContext ctx, ThriftMessage message) {
		boolean messageRequiresOrderedResponses = message.isOrderedResponsesRequired();

		if (!DispatcherContext.isResponseOrderingRequirementInitialized(ctx)) {
			// This is the first request. This message will decide whether all responses on the
			// channel must be strictly ordered, or whether out-of-order is allowed.
			DispatcherContext.setResponseOrderingRequired(ctx, messageRequiresOrderedResponses);
		} else {
			// This is not the first request. Verify that the ordering requirement on this message
			// is consistent with the requirement on the channel itself.
			checkState(messageRequiresOrderedResponses == DispatcherContext.isResponseOrderingRequired(ctx),
					"Every message on a single channel must specify the same requirement for response ordering");
		}
	}

	private void processRequest(final ChannelHandlerContext ctx, final ThriftMessage message,
			final TNiftyTransport messageTransport, final TProtocol inProtocol, final TProtocol outProtocol) {
		// Remember the ordering of requests as they arrive, used to enforce an order on the
		// responses.
		final int requestSequenceId = dispatcherSequenceId.incrementAndGet();

		if (DispatcherContext.isResponseOrderingRequired(ctx)) {
			synchronized (responseMap) {
				// Limit the number of pending responses (responses which finished out of order, and are
				// waiting for previous requests to be finished so they can be written in order), by
				// blocking further channel reads. Due to the way Netty frame decoders work, this is more
				// of an estimate than a hard limit. Netty may continue to decode and process several
				// more requests that were in the latest read, even while further reads on the channel
				// have been blocked.
				if (requestSequenceId > lastResponseWrittenId.get() + queuedResponseLimit
						&& !DispatcherContext.isChannelReadBlocked(ctx)) {
					DispatcherContext.blockChannelReads(ctx);
				}
			}
		}

		try {
			exe.execute(new Runnable() {
				@Override
				public void run() {
					ListenableFuture<Boolean> processFuture;
					final AtomicBoolean responseSent = new AtomicBoolean(false);
					// Use AtomicReference as a generic holder class to be able to mark it final
					// and pass into inner classes. Since we only use .get() and .set(), we don't
					// actually do any atomic operations.
					final AtomicReference<Timeout> expireTimeout = new AtomicReference<>(null);

					try {
						try {
							long timeRemaining = 0;
							if (taskTimeoutMillis > 0) {
								long timeElapsed = System.currentTimeMillis() - message.getProcessStartTimeMillis();
								if (timeElapsed >= taskTimeoutMillis) {
									TApplicationException taskTimeoutException = new TApplicationException(
											TApplicationException.INTERNAL_ERROR,
											"Task stayed on the queue for " + timeElapsed
													+ " milliseconds, exceeding configured task timeout of "
													+ taskTimeoutMillis + " milliseconds.");
									sendTApplicationException(taskTimeoutException, ctx, message, requestSequenceId,
											messageTransport, inProtocol, outProtocol);
									return;
								} else {
									timeRemaining = taskTimeoutMillis - timeElapsed;
								}
							}

							if (timeRemaining > 0) {
								expireTimeout.set(taskTimeoutTimer.newTimeout(new TimerTask() {
									@Override
									public void run(Timeout timeout) throws Exception {
										// The immediateFuture returned by processors isn't cancellable, cancel() and
										// isCanceled() always return false. Use a flag to detect task expiration.
										if (responseSent.compareAndSet(false, true)) {
											TApplicationException ex = new TApplicationException(
													TApplicationException.INTERNAL_ERROR,
													"Task timed out while executing.");
											// Create a temporary transport to send the exception
											ChannelBuffer duplicateBuffer = message.getBuffer().duplicate();
											duplicateBuffer.resetReaderIndex();
											TNiftyTransport temporaryTransport = new TNiftyTransport(ctx.getChannel(),
													duplicateBuffer, message.getTransportType());
											TProtocolPair protocolPair = duplexProtocolFactory.getProtocolPair(
													TTransportPair.fromSingleTransport(temporaryTransport));
											sendTApplicationException(ex, ctx, message, requestSequenceId,
													temporaryTransport, protocolPair.getInputProtocol(),
													protocolPair.getOutputProtocol());
										}
									}
								}, timeRemaining, TimeUnit.MILLISECONDS));
							}

							ConnectionContext connectionContext = ConnectionContexts.getContext(ctx.getChannel());
							RequestContext requestContext = new NiftyRequestContext(connectionContext, inProtocol,
									outProtocol, messageTransport);
							RequestContexts.setCurrentContext(requestContext);
							processFuture = processorFactory.getProcessor(messageTransport).process(inProtocol,
									outProtocol, requestContext);
						} finally {
							// RequestContext does NOT stay set while we are waiting for the process
							// future to complete. This is by design because we'll might move on to the
							// next request using this thread before this one is completed. If you need
							// the context throughout an asynchronous handler, you need to read and store
							// it before returning a future.
							RequestContexts.clearCurrentContext();
						}

						Futures.addCallback(processFuture, new FutureCallback<Boolean>() {
							@Override
							public void onSuccess(Boolean result) {
								deleteExpirationTimer(expireTimeout.get());
								try {
									// Only write response if the client is still there and the task timeout
									// hasn't expired.
									if (ctx.getChannel().isConnected() && responseSent.compareAndSet(false, true)) {
										ThriftMessage response = message.getMessageFactory()
												.create(messageTransport.getOutputBuffer());
										writeResponse(ctx, response, requestSequenceId,
												DispatcherContext.isResponseOrderingRequired(ctx));
									}
								} catch (Throwable t) {
									onDispatchException(ctx, t);
								}
							}

							@Override
							public void onFailure(Throwable t) {
								deleteExpirationTimer(expireTimeout.get());
								onDispatchException(ctx, t);
							}
						});
					} catch (TException e) {
						onDispatchException(ctx, e);
					}
				}
			});
		} catch (RejectedExecutionException ex) {
			TApplicationException x = new TApplicationException(TApplicationException.INTERNAL_ERROR,
					"Server overloaded");
			sendTApplicationException(x, ctx, message, requestSequenceId, messageTransport, inProtocol, outProtocol);
		}
	}

	private void deleteExpirationTimer(Timeout timeout) {
		if (timeout == null) {
			return;
		}
		timeout.cancel();
	}

	private void sendTApplicationException(TApplicationException x, ChannelHandlerContext ctx, ThriftMessage request,
			int responseSequenceId, TNiftyTransport requestTransport, TProtocol inProtocol, TProtocol outProtocol) {
		if (ctx.getChannel().isConnected()) {
			try {
				TMessage message = inProtocol.readMessageBegin();
				outProtocol.writeMessageBegin(new TMessage(message.name, TMessageType.EXCEPTION, message.seqid));
				x.write(outProtocol);
				outProtocol.writeMessageEnd();
				outProtocol.getTransport().flush();

				ThriftMessage response = request.getMessageFactory().create(requestTransport.getOutputBuffer());
				writeResponse(ctx, response, responseSequenceId, DispatcherContext.isResponseOrderingRequired(ctx));
			} catch (TException ex) {
				onDispatchException(ctx, ex);
			}
		}
	}

	private void onDispatchException(ChannelHandlerContext ctx, Throwable t) {
		Channels.fireExceptionCaught(ctx, t);
		closeChannel(ctx);
	}

	private void writeResponse(ChannelHandlerContext ctx, ThriftMessage response, int responseSequenceId,
			boolean isOrderedResponsesRequired) {
		if (isOrderedResponsesRequired) {
			writeResponseInOrder(ctx, response, responseSequenceId);
		} else {
			// No ordering required, just write the response immediately
			Channels.write(ctx.getChannel(), response);
			lastResponseWrittenId.incrementAndGet();
		}
	}

	private void writeResponseInOrder(ChannelHandlerContext ctx, ThriftMessage response, int responseSequenceId) {
		// Ensure responses to requests are written in the same order the requests
		// were received.
		synchronized (responseMap) {
			int currentResponseId = lastResponseWrittenId.get() + 1;
			if (responseSequenceId != currentResponseId) {
				// This response is NOT next in line of ordered responses, save it to
				// be sent later, after responses to all earlier requests have been
				// sent.
				responseMap.put(responseSequenceId, response);
			} else {
				// This response was next in line, write this response now, and see if
				// there are others next in line that should be sent now as well.
				do {
					Channels.write(ctx.getChannel(), response);
					lastResponseWrittenId.incrementAndGet();
					++currentResponseId;
					response = responseMap.remove(currentResponseId);
				} while (null != response);

				// Now that we've written some responses, check if reads should be unblocked
				if (DispatcherContext.isChannelReadBlocked(ctx)) {
					int lastRequestSequenceId = dispatcherSequenceId.get();
					if (lastRequestSequenceId <= lastResponseWrittenId.get() + queuedResponseLimit) {
						DispatcherContext.unblockChannelReads(ctx);
					}
				}
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		// Any out of band exception are caught here and we tear down the socket
		closeChannel(ctx);

		// Send for logging
		ctx.sendUpstream(e);
	}

	private void closeChannel(ChannelHandlerContext ctx) {
		if (ctx.getChannel().isOpen()) {
			ctx.getChannel().close();
		}
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// Reads always start out unblocked
		DispatcherContext.unblockChannelReads(ctx);
		super.channelOpen(ctx, e);
	}

	private static class DispatcherContext {
		private ReadBlockedState readBlockedState = ReadBlockedState.NOT_BLOCKED;
		private boolean responseOrderingRequired = false;
		private boolean responseOrderingRequirementInitialized = false;

		public static boolean isChannelReadBlocked(ChannelHandlerContext ctx) {
			return getDispatcherContext(ctx).readBlockedState == ReadBlockedState.BLOCKED;
		}

		public static void blockChannelReads(ChannelHandlerContext ctx) {
			// Remember that reads are blocked (there is no Channel.getReadable())
			getDispatcherContext(ctx).readBlockedState = ReadBlockedState.BLOCKED;

			// NOTE: this shuts down reads, but isn't a 100% guarantee we won't get any more messages.
			// It sets up the channel so that the polling loop will not report any new read events
			// and netty won't read any more data from the socket, but any messages already fully read
			// from the socket before this ran may still be decoded and arrive at this handler. Thus
			// the limit on queued messages before we block reads is more of a guidance than a hard
			// limit.
			ctx.getChannel().setReadable(false);
		}

		public static void unblockChannelReads(ChannelHandlerContext ctx) {
			// Remember that reads are unblocked (there is no Channel.getReadable())
			getDispatcherContext(ctx).readBlockedState = ReadBlockedState.NOT_BLOCKED;
			ctx.getChannel().setReadable(true);
		}

		public static void setResponseOrderingRequired(ChannelHandlerContext ctx, boolean required) {
			DispatcherContext dispatcherContext = getDispatcherContext(ctx);
			dispatcherContext.responseOrderingRequirementInitialized = true;
			dispatcherContext.responseOrderingRequired = required;
		}

		public static boolean isResponseOrderingRequired(ChannelHandlerContext ctx) {
			return getDispatcherContext(ctx).responseOrderingRequired;
		}

		public static boolean isResponseOrderingRequirementInitialized(ChannelHandlerContext ctx) {
			return getDispatcherContext(ctx).responseOrderingRequirementInitialized;
		}

		private static DispatcherContext getDispatcherContext(ChannelHandlerContext ctx) {
			DispatcherContext dispatcherContext;
			Object attachment = ctx.getAttachment();

			if (attachment == null) {
				// No context was added yet, add one
				dispatcherContext = new DispatcherContext();
				ctx.setAttachment(dispatcherContext);
			} else if (!(attachment instanceof DispatcherContext)) {
				// There was a context, but it was the wrong type. This should never happen.
				throw new IllegalStateException(
						"NiftyDispatcher handler context should be of type NiftyDispatcher.DispatcherContext");
			} else {
				dispatcherContext = (DispatcherContext) attachment;
			}

			return dispatcherContext;
		}

		private enum ReadBlockedState {
			NOT_BLOCKED, BLOCKED,
		}
	}
}
