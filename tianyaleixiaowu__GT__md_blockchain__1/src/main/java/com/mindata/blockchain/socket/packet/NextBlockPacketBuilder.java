package com.mindata.blockchain.socket.packet;

import com.mindata.blockchain.ApplicationContextProvider;
import com.mindata.blockchain.block.Block;
import com.mindata.blockchain.core.event.ClientRequestEvent;
import com.mindata.blockchain.core.manager.DbBlockManager;
import com.mindata.blockchain.socket.body.RpcBlockBody;

/**
 * @author wuweifeng wrote on 2018/3/20.
 */
public class NextBlockPacketBuilder {
	public static BlockPacket build() {
		return build(null);
	}

	public static BlockPacket build(String responseId) {
		Block block = ApplicationContextProvider.getBean(DbBlockManager.class).getLastBlock();

		RpcBlockBody rpcBlockBody = new RpcBlockBody(block);
		rpcBlockBody.setResponseMsgId(responseId);
		BlockPacket blockPacket = new PacketBuilder<>().setType(PacketType.NEXT_BLOCK_INFO_REQUEST)
				.setBody(rpcBlockBody).build();
		//发布client请求事件
		ApplicationContextProvider.publishEvent(new ClientRequestEvent(blockPacket));
		return blockPacket;
	}

}
