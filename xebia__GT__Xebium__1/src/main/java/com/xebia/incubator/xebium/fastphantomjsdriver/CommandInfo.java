package com.xebia.incubator.xebium.fastphantomjsdriver;

import org.openqa.selenium.remote.http.HttpMethod;

public class CommandInfo {

	private final String url;
	private final HttpMethod method;

	/**
	 * @deprecated Use {@link org.openqa.selenium.remote.CommandInfo(String, HttpMethod)}.
	 */
	@Deprecated
	public CommandInfo(String url, HttpVerb verb) {
		this(url, verb.toHttpMethod());
	}

	public CommandInfo(String url, HttpMethod method) {
		this.url = url;
		this.method = method;
	}

	String getUrl() {
		return url;
	}

	HttpMethod getMethod() {
		return method;
	}
}
