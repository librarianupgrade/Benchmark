package com.github.liuweijw.business.wechat.config;

import me.chanjar.weixin.common.api.WxConsts.EventType;
import me.chanjar.weixin.common.api.WxConsts.MenuButtonType;
import me.chanjar.weixin.common.api.WxConsts.XmlMsgType;
import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.constant.WxMpEventConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.liuweijw.business.wechat.handler.AbstractHandler;
import com.github.liuweijw.business.wechat.handler.KfSessionHandler;
import com.github.liuweijw.business.wechat.handler.LocationHandler;
import com.github.liuweijw.business.wechat.handler.LogHandler;
import com.github.liuweijw.business.wechat.handler.MenuHandler;
import com.github.liuweijw.business.wechat.handler.MsgHandler;
import com.github.liuweijw.business.wechat.handler.NullHandler;
import com.github.liuweijw.business.wechat.handler.StoreCheckNotifyHandler;
import com.github.liuweijw.business.wechat.handler.SubscribeHandler;
import com.github.liuweijw.business.wechat.handler.UnsubscribeHandler;

@Configuration
@ConditionalOnClass(WxMpService.class)
@EnableConfigurationProperties(WechatMpProperties.class)
public class WechatMpConfiguration {

	@Autowired
	protected LogHandler logHandler;

	@Autowired
	protected NullHandler nullHandler;

	@Autowired
	protected KfSessionHandler kfSessionHandler;

	@Autowired
	protected StoreCheckNotifyHandler storeCheckNotifyHandler;

	@Autowired
	private WechatMpProperties properties;

	@Autowired
	private LocationHandler locationHandler;

	@Autowired
	private MenuHandler menuHandler;

	@Autowired
	private MsgHandler msgHandler;

	@Autowired
	private UnsubscribeHandler unsubscribeHandler;

	@Autowired
	private SubscribeHandler subscribeHandler;

	@Bean
	@ConditionalOnMissingBean
	public WxMpConfigStorage configStorage() {
		WxMpInMemoryConfigStorage configStorage = new WxMpInMemoryConfigStorage();
		configStorage.setAppId(this.properties.getAppId());
		configStorage.setSecret(this.properties.getSecret());
		configStorage.setToken(this.properties.getToken());
		configStorage.setAesKey(this.properties.getAesKey());
		return configStorage;
	}

	@Bean
	@ConditionalOnMissingBean
	public WxMpService wxMpService(WxMpConfigStorage configStorage) {
		// WxMpService wxMpService = new me.chanjar.weixin.mp.api.impl.okhttp.WxMpServiceImpl();
		// WxMpService wxMpService = new me.chanjar.weixin.mp.api.impl.jodd.WxMpServiceImpl();
		// WxMpService wxMpService = new me.chanjar.weixin.mp.api.impl.apache.WxMpServiceImpl();
		WxMpService wxMpService = new me.chanjar.weixin.mp.api.impl.WxMpServiceImpl();
		wxMpService.setWxMpConfigStorage(configStorage);
		return wxMpService;
	}

	@Bean
	public WxMpMessageRouter router(WxMpService wxMpService) {
		final WxMpMessageRouter newRouter = new WxMpMessageRouter(wxMpService);

		// 记录所有事件的日志 （异步执行）
		newRouter.rule().handler(this.logHandler).next();

		// 接收客服会话管理事件
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(WxMpEventConstants.CustomerService.KF_CREATE_SESSION).handler(this.kfSessionHandler).end();
		newRouter.rule().async(false).msgType(XmlMsgType.EVENT)
				.event(WxMpEventConstants.CustomerService.KF_CLOSE_SESSION).handler(this.kfSessionHandler).end();
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

		return newRouter;
	}

	protected MenuHandler getMenuHandler() {
		return this.menuHandler;
	}

	protected SubscribeHandler getSubscribeHandler() {
		return this.subscribeHandler;
	}

	protected UnsubscribeHandler getUnsubscribeHandler() {
		return this.unsubscribeHandler;
	}

	protected AbstractHandler getLocationHandler() {
		return this.locationHandler;
	}

	protected MsgHandler getMsgHandler() {
		return this.msgHandler;
	}

	protected AbstractHandler getScanHandler() {
		return null;
	}

}
