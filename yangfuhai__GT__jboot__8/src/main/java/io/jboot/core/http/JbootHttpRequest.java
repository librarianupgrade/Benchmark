/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.core.http;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * httpRequest
 */
public class JbootHttpRequest {

	public static final String METHOD_POST = "POST";
	public static final String METHOD_GET = "GET";
	public static final int READ_TIME_OUT = 1000 * 10; // 10秒
	public static final int CONNECT_TIME_OUT = 1000 * 5; // 5秒
	public static final String CHAR_SET = "UTF-8";

	public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
	public static final String CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded;charset=utf-8";

	Map<String, String> headers;
	Map<String, Object> params;

	private String requestUrl;
	private String certPath;
	private String certPass;

	private String method = METHOD_GET;
	private int readTimeOut = READ_TIME_OUT;
	private int connectTimeOut = CONNECT_TIME_OUT;
	private String charset = CHAR_SET;

	private boolean multipartFormData = false;

	private File downloadFile;
	private String contentType = CONTENT_TYPE_URL_ENCODED;

	public static JbootHttpRequest create(String url) {
		return new JbootHttpRequest(url);
	}

	public static JbootHttpRequest create(String url, String method) {
		JbootHttpRequest request = new JbootHttpRequest(url);
		request.setMethod(method);
		return request;
	}

	public static JbootHttpRequest create(String url, Map<String, Object> params) {
		JbootHttpRequest request = new JbootHttpRequest(url);
		request.setParams(params);
		return request;
	}

	public static JbootHttpRequest create(String url, Map<String, Object> params, String method) {
		JbootHttpRequest request = new JbootHttpRequest(url);
		request.setMethod(method);
		request.setParams(params);
		return request;
	}

	public JbootHttpRequest() {
	}

	public JbootHttpRequest(String url) {
		this.requestUrl = url;
	}

	public void addParam(String key, Object value) {
		if (params == null) {
			params = new HashMap<>();
		}
		if (value instanceof File) {
			setMultipartFormData(true);
		}
		params.put(key, value);
	}

	public void addParams(Map<String, Object> map) {
		if (params == null) {
			params = new HashMap<>();
		}
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			if (entry.getValue() instanceof File) {
				setMultipartFormData(true);
			}

			params.put(entry.getKey(), entry.getValue());
		}
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getCertPath() {
		return certPath;
	}

	public void setCertPath(String certPath) {
		this.certPath = certPath;
	}

	public String getCertPass() {
		return certPass;
	}

	public void setCertPass(String certPass) {
		this.certPass = certPass;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public int getReadTimeOut() {
		return readTimeOut;
	}

	public void setReadTimeOut(int readTimeOut) {
		this.readTimeOut = readTimeOut;
	}

	public int getConnectTimeOut() {
		return connectTimeOut;
	}

	public void setConnectTimeOut(int connectTimeOut) {
		this.connectTimeOut = connectTimeOut;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void addHeader(String key, String value) {
		if (headers == null) {
			headers = new HashMap<>();
		}
		headers.put(key, value);
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		if (params == null) {
			return;
		}
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			if (entry.getValue() instanceof File) {
				setMultipartFormData(true);
			}
		}
		this.params = params;
	}

	public boolean isGetRquest() {
		return METHOD_GET.equalsIgnoreCase(method);
	}

	public boolean isPostRquest() {
		return METHOD_POST.equalsIgnoreCase(method);
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public boolean isMultipartFormData() {
		return multipartFormData;
	}

	public void setMultipartFormData(boolean multipartFormData) {
		this.multipartFormData = multipartFormData;
	}

	public File getDownloadFile() {
		return downloadFile;
	}

	public void setDownloadFile(File downloadFile) {
		this.downloadFile = downloadFile;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
