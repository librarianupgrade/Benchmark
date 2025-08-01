/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.net.factory;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class TrustAllManager implements X509TrustManager {
	private X509Certificate[] issuers;

	public TrustAllManager() {
		this.issuers = new X509Certificate[0];
	}

	public X509Certificate[] getAcceptedIssuers() {
		return issuers;
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType) {
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) {
	}
}
