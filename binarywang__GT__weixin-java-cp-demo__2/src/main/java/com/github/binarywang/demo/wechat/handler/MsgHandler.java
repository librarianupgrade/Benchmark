package com.github.binarywang.demo.wechat.handler;

import com.github.binarywang.demo.wechat.builder.TextBuilder;
import com.github.binarywang.demo.wechat.utils.JsonUtils;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.WxCpXmlMessage;
import me.chanjar.weixin.cp.bean.WxCpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * * @author Binary Wang(https://github.com/binarywang)
 */
@Component
public class MsgHandler extends AbstractHandler {

	@Override
	public WxCpXmlOutMessage handle(WxCpXmlMessage wxMessage, Map<String, Object> context, WxCpService weixinService,
			WxSessionManager sessionManager) {

		if (!wxMessage.getMsgType().equals(WxConsts.XML_MSG_EVENT)) {
			//TODO 可以选择将消息保存到本地
		}

		//TODO 组装回复消息
		String content = "收到信息内容：" + JsonUtils.toJson(wxMessage);

		return new TextBuilder().build(content, wxMessage, weixinService);

	}

}
