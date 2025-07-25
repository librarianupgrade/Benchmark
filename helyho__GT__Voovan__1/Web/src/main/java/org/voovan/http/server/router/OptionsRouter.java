package org.voovan.http.server.router;

import org.voovan.http.message.HttpStatic;
import org.voovan.http.server.HttpRequest;
import org.voovan.http.server.HttpResponse;
import org.voovan.http.server.HttpRouter;

/**
 * 用类处理自定义 header 以及跨域的处理
 *      自定义的 header 会先用 Options 探测是否支持自定义的 headers
 *
 * @author: helyho
 * DBase Framework.
 * WebSite: https://github.com/helyho/DBase
 * Licence: Apache v2 License
 */
public class OptionsRouter implements HttpRouter {

	private String method;
	private String domains;
	private String headers;

	public OptionsRouter(String method, String domains, String headers) {
		this.method = method;
		this.domains = domains == null ? "*" : domains;
		this.headers = headers;
	}

	@Override
	public void process(HttpRequest request, HttpResponse response) throws Exception {
		if ("*".equals(domains)) {
			domains = request.header().get("Origin");
		}

		response.header().put(HttpStatic.ACCESS_CONTROL_ALLOW_METHODS_STRING, method);
		response.header().put(HttpStatic.ACCESS_CONTROL_ALLOW_ORIGIN_STRING, domains);
		response.header().put(HttpStatic.ACCESS_CONTROL_ALLOW_HEADERS_STRING, headers);
		response.header().put(HttpStatic.ACCESS_CONTROL_ALLOW_CREDENTIALS_STRING, "true");
	}
}
