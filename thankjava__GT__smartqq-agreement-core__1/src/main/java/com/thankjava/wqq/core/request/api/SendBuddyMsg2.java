package com.thankjava.wqq.core.request.api;

import com.thankjava.toolkit3d.http.async.consts.HttpMethod;
import com.thankjava.toolkit3d.http.async.entity.Headers;
import com.thankjava.toolkit3d.http.async.entity.Parameters;
import com.thankjava.toolkit3d.http.async.entity.AsyncRequest;
import com.thankjava.toolkit3d.http.async.entity.AsyncResponse;
import com.thankjava.wqq.consts.ConstsParams;
import com.thankjava.wqq.consts.RequestUrls;
import com.thankjava.wqq.entity.msg.SendMsg;
import com.thankjava.wqq.extend.ActionListener;
import com.thankjava.wqq.extend.CallBackListener;

import com.alibaba.fastjson.JSONObject;

public class SendBuddyMsg2 extends BaseHttpService {

	private SendMsg sendMsg;

	public SendBuddyMsg2(SendMsg sendMsg) {
		this.sendMsg = sendMsg;
	}

	@Override
	public AsyncResponse doRequest(CallBackListener listener) {
		if (listener != null) {
			listener.onListener(new ActionListener(asyncHttpClient.syncRequestWithSession(buildRequestParams())));
			return null;
		} else {
			return asyncHttpClient.syncRequestWithSession(buildRequestParams());
		}
	}

	@Override
	protected AsyncRequest buildRequestParams() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("to", sendMsg.getTo()); //
		jsonObject.put("content", sendMsg.getContent().toSendMsg()); //
		jsonObject.put("face", 546); // 这个其实没啥用
		jsonObject.put("clientid", ConstsParams.CLIENT_ID);
		jsonObject.put("msg_id", msgId.incrementAndGet());
		jsonObject.put("psessionid", session.getPsessionid());
		Parameters params = new Parameters("r", jsonObject.toJSONString());
		Headers headers = new Headers("Referer", RequestUrls.referer_about_msg.url);
		return new AsyncRequest(RequestUrls.send_buddy_msg2.url, HttpMethod.post, params, headers);
	}

}
