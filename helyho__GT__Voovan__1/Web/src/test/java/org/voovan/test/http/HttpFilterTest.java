package org.voovan.test.http;

import org.voovan.http.server.HttpFilter;
import org.voovan.http.server.HttpRequest;
import org.voovan.http.server.HttpResponse;
import org.voovan.http.server.context.HttpFilterConfig;
import org.voovan.tools.log.Logger;

import java.util.Map.Entry;

public class HttpFilterTest implements HttpFilter {

	@Override
	public Object onRequest(HttpFilterConfig filterConfig, HttpRequest request, HttpResponse response,
			Object prevFilterResult) {
		String msg = "[" + filterConfig.getName() + "] ";
		for (Entry<String, Object> entry : filterConfig.getParameters().entrySet()) {
			msg += entry.getKey() + " = " + entry.getValue() + ", ";
		}
		msg = msg + "RequestPath=" + request.protocol().getPath();

		if (prevFilterResult == null) {
			//			request.redirect("/img/logo.jpg");  //转向请求用于拦截非法请求并志向其他
			prevFilterResult = 1;
		} else {
			prevFilterResult = (int) prevFilterResult + 1;
		}
		Logger.simplef("ON_REQUEST: {1},filter sequence: {2}", msg, prevFilterResult);

		return prevFilterResult;
	}

	@Override
	public Object onResponse(HttpFilterConfig filterConfig, HttpRequest request, HttpResponse response,
			Object prevFilterResult) {
		String msg = "[" + filterConfig.getName() + "] ";
		for (Entry<String, Object> entry : filterConfig.getParameters().entrySet()) {
			msg += entry.getKey() + " = " + entry.getValue() + ", ";
		}
		msg = msg + "RequestPath=" + request.protocol().getPath();

		if (!(prevFilterResult instanceof Integer) || prevFilterResult == null) {
			prevFilterResult = 1;
		} else {
			prevFilterResult = (int) prevFilterResult + 1;
		}
		Logger.simplef("ON_RESPONSE: {1} ,filter sequence: {2}", msg, prevFilterResult);

		return prevFilterResult;
	}

}
