/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.mp.handler;

import co.yixiang.mp.builder.TextBuilder;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType;

@Component
public class LocationHandler extends AbstractHandler {

	@Override
	public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService,
			WxSessionManager sessionManager) {
		if (wxMessage.getMsgType().equals(XmlMsgType.LOCATION)) {
			//TODO 接收处理用户发送的地理位置消息
			try {
				String content = "感谢反馈，您的的地理位置已收到！";
				return new TextBuilder().build(content, wxMessage, null);
			} catch (Exception e) {
				this.logger.error("位置消息接收处理失败", e);
				return null;
			}
		}

		//上报地理位置事件
		this.logger.info("上报地理位置，纬度 : {}，经度 : {}，精度 : {}", wxMessage.getLatitude(), wxMessage.getLongitude(),
				String.valueOf(wxMessage.getPrecision()));

		//TODO  可以将用户地理位置信息保存到本地数据库，以便以后使用

		return null;
	}

}
