package com.thankjava.wqq.core.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.thankjava.wqq.entity.enums.LoginResultStatus;
import com.thankjava.wqq.extend.ActionListener;
import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.thankjava.toolkit3d.http.async.entity.AsyncResponse;
import com.thankjava.wqq.consts.ConfigParams;
import com.thankjava.wqq.consts.ConstsParams;
import com.thankjava.wqq.consts.DataResRegx;
import com.thankjava.wqq.core.event.MsgPollEvent;
import com.thankjava.wqq.core.request.RequestBuilder;
import com.thankjava.wqq.core.request.api.CheckLoginQRcodeStatus;
import com.thankjava.wqq.core.request.api.CheckSig;
import com.thankjava.wqq.core.request.api.GetLoginQRcode;
import com.thankjava.wqq.core.request.api.GetVfWebqq;
import com.thankjava.wqq.core.request.api.Login2;
import com.thankjava.wqq.entity.Session;
import com.thankjava.wqq.entity.wqq.FriendsList;
import com.thankjava.wqq.extend.CallBackListener;
import com.thankjava.wqq.factory.ActionFactory;
import com.thankjava.wqq.factory.RequestFactory;
import com.thankjava.wqq.util.RegexUtil;
import com.alibaba.fastjson.JSONObject;

public class LoginAction {

	private static final Logger logger = LoggerFactory.getLogger(LoginAction.class);

	private Session session = Session.getSession();

	private RequestBuilder getLoginQRcode = RequestFactory.getInstance(GetLoginQRcode.class);
	private RequestBuilder checkSig = RequestFactory.getInstance(CheckSig.class);
	private RequestBuilder getVfWebqq = RequestFactory.getInstance(GetVfWebqq.class);
	private RequestBuilder login2 = RequestFactory.getInstance(Login2.class);
	private RequestBuilder checkLoginQRcodeStatus = RequestFactory.getInstance(CheckLoginQRcodeStatus.class);

	private GetInfoAction getInfo = ActionFactory.getInstance(GetInfoAction.class);

	public void login(final CallBackListener getQrListener, final CallBackListener loginListener) {

		AsyncResponse asyncResponse = getLoginQRcode.doRequest(null);
		if (asyncResponse == null) {
			logger.error("获取二维码失败,执行重试");
			login(getQrListener, loginListener);
		}

		ActionListener actionListener = null;
		try {
			// 得到二维码数据
			actionListener = new ActionListener(
					ImageIO.read(new ByteArrayInputStream(asyncResponse.getDataByteArray())));
		} catch (IOException e) {
			logger.error("获取二维码数据失败", e);
			loginListener.onListener(new ActionListener(LoginResultStatus.exception));
		}

		getQrListener.onListener(actionListener);

		logger.debug("获取二维码完成,启动二维码状态检查");

		checkLoginQRcodeStatus(getQrListener, loginListener);
	}

	/**
	 * 检查当前的二维码的状态
	 * <p>Function: checkLoginQRcodeStatus</p>
	 * <p>Description: </p>
	 *
	 * @return
	 * @author zhaoxy@thankjava.com
	 * @date 2016年12月19日 下午4:19:00
	 * @version 1.0
	 */
	private void checkLoginQRcodeStatus(CallBackListener getQRListener, CallBackListener loginListener) {

		try {
			Thread.sleep(ConstsParams.CHECK_QRCODE_WITE_TIME);
		} catch (InterruptedException e) {
			logger.error("检查当前的二维码的状态线程等待异常", e);
			loginListener.onListener(new ActionListener(LoginResultStatus.exception));
		}

		String[] data = RegexUtil.doRegex(checkLoginQRcodeStatus.doRequest(null).getDataString(),
				DataResRegx.check_login_qrcode_status);

		if (data == null) {
			logger.error("解析二维码状态失败,重试二维码状态检查");
			checkLoginQRcodeStatus(getQRListener, loginListener);
		}

		Integer statusCode = Integer.valueOf(data[0]);
		if (statusCode == null) {
			logger.error("无法解析出有效的二维码状态码,重试二维码状态检查");
			checkLoginQRcodeStatus(getQRListener, loginListener);
		}

		switch (statusCode) {
		case 0: // 二维码认证成功
			logger.debug("二维码验证完成");
			session.setCheckSigUrl(data[2]);
			if (beginLogin()) {
				loginListener.onListener(new ActionListener(LoginResultStatus.success));
			} else {
				logger.error("登录失败");
				loginListener.onListener(new ActionListener(LoginResultStatus.failed));
			}
			break;
		case 65: // 二维码认证过期
			if (ConfigParams.AUTO_REFRESH_QR_CODE) { // 如果指定过期自动刷新二维码
				logger.debug("二维码已过期, 重新获取二维码...");
				login(getQRListener, loginListener);
				break;
			}
			logger.debug("当前二维码已过期, 并且未设置自动重刷二维码, 登录失败");
			loginListener.onListener(new ActionListener(LoginResultStatus.failed));
			break;
		default: // 二维码处于认证中|等待认证
			logger.debug("二维码状态: " + data[4]);
			checkLoginQRcodeStatus(getQRListener, loginListener);
			break;
		}
	}

	/**
	 * 扫码认证完成后执行登录态相关的认证登录
	 * <p>Function: beginLogin</p>
	 * <p>Description: </p>
	 *
	 * @author zhaoxy@thankjava.com
	 * @date 2016年12月20日 下午2:49:21
	 * @version 1.0
	 */
	private boolean beginLogin() {

		// checkSig
		AsyncResponse asyncResponse = checkSig.doRequest(null);

		// checkSig 成功后 从cookie中得到ptwebqq 确切的说其实这个cookie是检查qrcode成功后服务器下发的
		Cookie cookie = asyncResponse.getCookies().getCookie("ptwebqq");
		if (cookie == null) {
			logger.error("未能获取到ptwebqq数据");
			return false;
		}
		String ptwebqq = cookie.getValue();
		if (ptwebqq == null || ptwebqq.length() == 0) {
			logger.error("未能成功获取ptwebqq数据");
			return false;
		}

		// getvfWebqq
		session.setPtwebqq(ptwebqq);
		asyncResponse = getVfWebqq.doRequest(null);
		if (asyncResponse.isEmptyDataString()) {
			logger.error("未能成功获取vfwebqq数据");
			return false;
		}
		String content = asyncResponse.getDataString();
		JSONObject jsonObject, result;
		try {
			jsonObject = JSONObject.parseObject(content);
			result = (JSONObject) jsonObject.get("result");
			session.setVfwebqq(result.get("vfwebqq").toString());
		} catch (Exception e) {
			logger.error("未能成功解析vfwebqq数据", e);
			return false;
		}

		// login2
		asyncResponse = login2.doRequest(null);
		if (asyncResponse.isEmptyDataString()) {
			logger.error("未能成功获取登录数据");
			return false;
		}
		content = asyncResponse.getDataString();
		try {
			jsonObject = JSONObject.parseObject(content);
			result = (JSONObject) jsonObject.get("result");
			session.setUin(result.getLongValue("uin"));
			session.setPsessionid(result.getString("psessionid"));
		} catch (Exception e) {
			logger.error("未能成功解析登录数据", e);
			return false;
		}

		if (ConfigParams.INIT_LOGIN_INFO) {

			FriendsList friendsList = getInfo.getFriendsList();
			if (friendsList == null) {
				logger.error("获取好友列表失败");
			} else {
				friendsList = getInfo.getOnlineStatus();
				if (friendsList == null) {
					logger.error("为好友列表查询在线状态失败");
				}
			}
			getInfo.getGroupsList();
			getInfo.getDiscusList();
			getInfo.getSelfInfo();
			getInfo.getRecentList();
		}

		// 启动消息Poll
		new Thread(new Runnable() {
			@Override
			public void run() {
				ActionFactory.getInstance(MsgPollEvent.class).poll();
			}
		}).start();

		return true;
	}
}
