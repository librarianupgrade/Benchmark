package com.github.binarywang.demo.wx.mp.service;

import com.github.binarywang.demo.wx.mp.config.WxConfig;
import com.github.binarywang.demo.wx.mp.handler.*;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.kefu.result.WxMpKfOnlineList;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.constant.WxMpEventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import static me.chanjar.weixin.common.api.WxConsts.*;

/**
 * @author Binary Wang
 */
public abstract class BaseWxService extends WxMpServiceImpl {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected LogHandler logHandler;

	@Autowired
	protected NullHandler nullHandler;

	@Autowired
	protected KfSessionHandler kfSessionHandler;

	@Autowired
	protected StoreCheckNotifyHandler storeCheckNotifyHandler;

	private WxMpMessageRouter router;

	protected abstract WxConfig getServerConfig();

	protected abstract MenuHandler getMenuHandler();

	protected abstract SubscribeHandler getSubscribeHandler();

	protected abstract UnsubscribeHandler getUnsubscribeHandler();

	protected abstract AbstractHandler getLocationHandler();

	protected abstract MsgHandler getMsgHandler();

	protected abstract AbstractHandler getScanHandler();

	@PostConstruct
	public void init() {
		final WxMpInMemoryConfigStorage config = new WxMpInMemoryConfigStorage();
		config.setAppId(this.getServerConfig().getAppid());// 设置微信公众号的appid
		config.setSecret(this.getServerConfig().getAppsecret());// 设置微信公众号的app corpSecret
		config.setToken(this.getServerConfig().getToken());// 设置微信公众号的token
		config.setAesKey(this.getServerConfig().getAesKey());// 设置消息加解密密钥
		super.setWxMpConfigStorage(config);

		this.refreshRouter();
	}

	private void refreshRouter() {

		final WxMpMessageRouter newRouter = new WxMpMessageRouter(this);

		// 记录所有事件的日志
		newRouter.rule().handler(this.logHandler).next();

		// 接收客服会话管理事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(WxMpEventConstants.CustomerService.KF_CLOSE_SESSION).handler(this.kfSessionHandler).end();
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(WxMpEventConstants.CustomerService.KF_CREATE_SESSION).handler(this.kfSessionHandler).end();
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(WxMpEventConstants.CustomerService.KF_SWITCH_SESSION).handler(this.kfSessionHandler).end();

		// 门店审核事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT).event(WxMpEventConstants.POI_CHECK_NOTIFY)
				.handler(this.storeCheckNotifyHandler).end();

		// 自定义菜单事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT).event(MenuButtonType.CLICK)
				.handler(this.getMenuHandler()).end();

		// 点击菜单连接事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT).event(MenuButtonType.VIEW).handler(this.nullHandler)
				.end();

		// 关注事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT).event(EventType.SUBSCRIBE)
				.handler(this.getSubscribeHandler()).end();

		// 取消关注事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT).event(EventType.UNSUBSCRIBE)
				.handler(this.getUnsubscribeHandler()).end();

		// 上报地理位置事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT).event(EventType.LOCATION)
				.handler(this.getLocationHandler()).end();

		// 接收地理位置消息
		newRouter.rule().async(false).msgType(XmlMsgType.LOCATION).handler(this.getLocationHandler()).end();

		// 扫码事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT).event(EventType.SCAN).handler(this.getScanHandler())
				.end();

		// 默认
		newRouter.rule().async(false).handler(this.getMsgHandler()).end();

		this.router = newRouter;
	}

	public WxMpXmlOutMessage route(WxMpXmlMessage message) {
		try {
			return this.router.route(message);
		} catch (Exception e) {
			this.logger.error(e.getMessage(), e);
		}

		return null;
	}

	public boolean hasKefuOnline() {
		try {
			WxMpKfOnlineList kfOnlineList = this.getKefuService().kfOnlineList();
			return kfOnlineList != null && kfOnlineList.getKfOnlineList().size() > 0;
		} catch (Exception e) {
			this.logger.error("获取客服在线状态异常: " + e.getMessage(), e);
		}

		return false;
	}

}
