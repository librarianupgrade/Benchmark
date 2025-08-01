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
package com.facebook.nifty.client;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

class NiftyClientChannelPipelineFactory implements ChannelPipelineFactory {
	private final int maxFrameSize;

	NiftyClientChannelPipelineFactory(int maxFrameSize) {
		this.maxFrameSize = maxFrameSize;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline cp = Channels.pipeline();
		cp.addLast("frameEncoder", new LengthFieldPrepender(4));
		cp.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(maxFrameSize, 0, 4, 0, 4));
		return cp;
	}
}
