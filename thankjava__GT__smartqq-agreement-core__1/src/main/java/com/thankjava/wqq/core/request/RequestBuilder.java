
package com.thankjava.wqq.core.request;

import com.thankjava.toolkit3d.http.async.entity.AsyncResponse;
import com.thankjava.wqq.extend.CallBackListener;

public interface RequestBuilder {

	/**
	 * 请求腾讯服务器 得到请求结果
	 * <p>Function: doRequest</p>
	 * <p>Description: </p>
	 *
	 * @param listener 回调函数 如果回调函数为空 则通过 return 返回请求结果
	 * @return 如果没有传入回调函数则结果将通过该参数返回， 如果传入了回调函数则返回值为null
	 * @author acexy@thankjava.com
	 * @date 2016年12月19日 下午3:05:37
	 * @version 1.0
	 */
	public AsyncResponse doRequest(CallBackListener listener);

}
