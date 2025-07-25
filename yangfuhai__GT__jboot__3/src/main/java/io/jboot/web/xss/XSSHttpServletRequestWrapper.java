/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.web.xss;

import io.jboot.utils.StrUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class XSSHttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {

	public XSSHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getParameter(String name) {
		return cleanXss(super.getParameter(name));

	}

	@Override
	public String[] getParameterValues(String name) {
		String[] values = super.getParameterValues(name);
		if (null == values) {
			return null;
		}
		for (int i = 0; i < values.length; i++) {
			values[i] = cleanXss(values[i]);
		}
		return values;
	}

	@Override
	public String getHeader(String name) {
		return cleanXss(super.getHeader(name));
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> paraMap = super.getParameterMap();
		if (null == paraMap || paraMap.isEmpty()) {
			return paraMap;
		}

		Map<String, String[]> ret = new HashMap<>(paraMap.size());
		for (Map.Entry<String, String[]> entry : paraMap.entrySet()) {
			String[] values = entry.getValue();
			if (null == values || values.length == 0) {
				ret.put(entry.getKey(), values);
			} else {
				String[] newValues = new String[values.length];
				for (int i = 0; i < values.length; i++) {
					newValues[i] = cleanXss(values[i]);
				}
				ret.put(entry.getKey(), newValues);
			}
		}
		return ret;
	}

	private static String cleanXss(String para) {
		return StrUtil.escapeHtml(para);
	}
}