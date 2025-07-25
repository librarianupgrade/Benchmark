package org.voovan.http.server;

import org.voovan.http.server.context.HttpFilterConfig;

/**
 * Http 服务过滤器接口
 * @author helyho
 *
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public interface HttpFilter {
	/**
	 * 请求过滤器,在请求之前
	 *      如果任何一个过滤器返回的是null, 则他后面的过滤器和路由都不会被执行
	 *  	如果最后一个过滤器如果返回 null, 则不会进行路由处理
	 *   	整个过滤器执行完成后,返回response.
	 * @param filterConfig 过滤器配置对象
	 * @param request  请求对象
	 * @param response 响应对象
	 * @param prevFilterResult 上一个过滤器的结果,可用来传递状态参数,用于控制过滤器.第一个过滤器接收的值是 null.
	 * @return 本地过滤器的结果,用于传递到下一个过滤器的prevFilterResult参数
	 */
	public Object onRequest(HttpFilterConfig filterConfig, HttpRequest request, HttpResponse response,
			Object prevFilterResult);

	/**
	 * 响应过滤器,在响应之后
	 *       路由处理器处理完成,且返回给浏览器之前
	 * @param filterConfig 过滤器配置对象
	 * @param request  请求对象
	 * @param response 响应对象
	 * @param prevFilterResult 上一个过滤器的结果,可用来传递状态参数,用于控制过滤器.第一个过滤器接收的值是 null.
	 * @return 本地过滤器的结果,用于传递到下一个过滤器的prevFilterResult参数
	 */
	public Object onResponse(HttpFilterConfig filterConfig, HttpRequest request, HttpResponse response,
			Object prevFilterResult);
}
