package com.mindata.blockchain.socket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioListener;
import org.tio.utils.json.Json;

/**
 * @author wuweifeng wrote on 2018/3/12.
 * 连接状态的监听器
 * 2017年3月26日 下午8:22:31
 */
public class BlockServerAioListener implements ServerAioListener {
	private static Logger log = LoggerFactory.getLogger(BlockServerAioListener.class);

	@Override
	public void onAfterClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) {
		log.info("onAfterClose channelContext:{}, throwable:{}, remark:{}, isRemove:{}", channelContext, throwable,
				remark, isRemove);
	}

	@Override
	public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) {
		log.info("onAfterConnected channelContext:{}, isConnected:{}, isReconnect:{}", channelContext, isConnected,
				isReconnect);

		//连接后，需要把连接会话对象设置给channelContext
		//channelContext.setAttribute(new ShowcaseSessionContext());
	}

	@Override
	public void onAfterReceived(ChannelContext channelContext, Packet packet, int packetSize) {
		log.info("onAfterReceived channelContext:{}, packet:{}, packetSize:{}", channelContext, Json.toJson(packet),
				packetSize);
	}

	@Override
	public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) {
		log.info("onAfterSent channelContext:{}, packet:{}, isSentSuccess:{}", channelContext, Json.toJson(packet),
				isSentSuccess);
	}

	@Override
	public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) {
	}
}
