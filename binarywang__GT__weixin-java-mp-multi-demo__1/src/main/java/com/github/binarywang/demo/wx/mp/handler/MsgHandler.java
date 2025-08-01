package com.github.binarywang.demo.wx.mp.handler;

import com.github.binarywang.demo.wx.mp.builder.TextBuilder;
import com.github.binarywang.demo.wx.mp.service.BaseWxService;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType;

/**
 * @author Binary Wang
 */
public abstract class MsgHandler extends AbstractHandler {

	@Override
	public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService,
			WxSessionManager sessionManager) {

		BaseWxService weixinService = (BaseWxService) wxMpService;

		if (!wxMessage.getMsgType().equals(XmlMsgType.EVENT)) {
			//TODO 可以选择将消息保存到本地
		}

		//当用户输入关键词如“你好”，“客服”等，并且有客服在线时，把消息转发给在线客服
		if (StringUtils.startsWithAny(wxMessage.getContent(), "你好", "客服") && weixinService.hasKefuOnline()) {
			return WxMpXmlOutMessage.TRANSFER_CUSTOMER_SERVICE().fromUser(wxMessage.getToUser())
					.toUser(wxMessage.getFromUser()).build();
		}

		//TODO 组装回复消息
		String content = "回复信息内容";
		return new TextBuilder().build(content, wxMessage, weixinService);

	}

}
