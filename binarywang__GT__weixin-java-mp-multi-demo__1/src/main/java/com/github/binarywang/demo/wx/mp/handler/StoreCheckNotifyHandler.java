package com.github.binarywang.demo.wx.mp.handler;

import com.github.binarywang.demo.wx.mp.config.WxConfig;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 门店审核事件处理
 *
 * @author 王彬 (Binary Wang)
 */
@Component
public class StoreCheckNotifyHandler extends AbstractHandler {

	@Override
	public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService,
			WxSessionManager sessionManager) {
		// TODO 处理门店审核事件
		return null;
	}

	@Override
	protected WxConfig getWxConfig() {
		return null;
	}

}
