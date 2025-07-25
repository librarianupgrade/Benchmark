/**
 * Copyright (c) 2015-2021, Michael Yang 杨福海 (fuhai999@gmail.com).
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
package io.jboot.utils;

import com.jfinal.core.Controller;
import com.jfinal.kit.Base64Kit;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.LogKit;
import com.jfinal.log.Log;
import io.jboot.Jboot;
import io.jboot.web.JbootWebConfig;

import java.math.BigInteger;

/**
 * 参考：spring-security
 * https://github.com/spring-projects/spring-security/
 * blob/master/web/src/main/java/org/springframework/security/
 * web/authentication/rememberme/TokenBasedRememberMeServices.java
 * ....AbstractRememberMeServices.java
 * <p>
 * 加密的cookie工具类
 */
public class CookieUtil {
	private static final Log LOG = Log.getLog(CookieUtil.class);

	private final static String COOKIE_SEPARATOR = "#";

	// cookie 加密秘钥
	private static String COOKIE_ENCRYPT_KEY = Jboot.config(JbootWebConfig.class).getCookieEncryptKey();

	// 2 days（单位：秒）
	private static int COOKIE_MAX_AGE = Jboot.config(JbootWebConfig.class).getCookieMaxAge();

	/**
	 * 在使用之前，小调用此方法进行加密key的设置
	 *
	 * @param key
	 */
	public static void initEncryptKey(String key) {
		COOKIE_ENCRYPT_KEY = key;
	}

	/**
	 * 设置 默认的 Cookie 有效时间，单位：秒
	 *
	 * @param seconds
	 */
	public static void initDefaultCookieMaxAge(int seconds) {
		COOKIE_MAX_AGE = seconds;
	}

	public static void put(Controller ctr, String key, Object value) {
		put(ctr, key, value, COOKIE_MAX_AGE, null, null, COOKIE_ENCRYPT_KEY);
	}

	public static void put(Controller ctr, String key, Object value, String secretKey) {
		put(ctr, key, value, COOKIE_MAX_AGE, null, null, secretKey);
	}

	public static void put(Controller ctr, String key, String value, int maxAgeInSeconds) {
		put(ctr, key, value, maxAgeInSeconds, null, null, COOKIE_ENCRYPT_KEY);
	}

	public static void put(Controller ctr, String key, String value, int maxAgeInSeconds, String secretKey) {
		put(ctr, key, value, maxAgeInSeconds, null, null, secretKey);
	}

	public static void put(Controller ctr, String key, Object value, int maxAgeInSeconds, String path, String domain,
			String secretKey) {
		String cookie = buildCookieValue(value.toString(), maxAgeInSeconds, secretKey);
		ctr.setCookie(key, cookie, maxAgeInSeconds, path, domain, true);
	}

	public static void remove(Controller ctr, String key) {
		ctr.removeCookie(key);
	}

	public static void remove(Controller ctr, String key, String path, String domain) {
		ctr.removeCookie(key, path, domain);
	}

	public static String get(Controller ctr, String key) {
		return get(ctr, key, COOKIE_ENCRYPT_KEY);
	}

	public static String get(Controller ctr, String key, String secretKey) {
		String cookieValue = ctr.getCookie(key);

		if (cookieValue == null) {
			return null;
		}

		try {
			String value = new String(Base64Kit.decode(cookieValue));
			return getFromCookieInfo(secretKey, value);
		}

		//倘若 cookie 被人为修改的情况下能会出现异常情况
		catch (Exception ex) {
			LogKit.error(ex.toString(), ex);
		}

		return null;
	}

	public static String buildCookieValue(String value, int maxAgeInSeconds, String secretKey) {
		long saveTime = System.currentTimeMillis();
		String encryptValue = encrypt(secretKey, saveTime, maxAgeInSeconds, value);

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(encryptValue);
		stringBuilder.append(COOKIE_SEPARATOR);
		stringBuilder.append(saveTime);
		stringBuilder.append(COOKIE_SEPARATOR);
		stringBuilder.append(maxAgeInSeconds);
		stringBuilder.append(COOKIE_SEPARATOR);
		stringBuilder.append(Base64Kit.encode(value));

		return Base64Kit.encode(stringBuilder.toString());
	}

	private static String encrypt(String secretKey, Object saveTime, Object maxAgeInSeconds, String value) {
		if (JbootWebConfig.DEFAULT_COOKIE_ENCRYPT_KEY.equals(secretKey)) {
			LOG.warn(
					"warn!!! encrypt key is defalut value. please config \"jboot.web.cookieEncryptKey = xxx\" in jboot.properties ");
		}
		return HashKit.md5(secretKey + saveTime.toString() + maxAgeInSeconds.toString() + value);
	}

	public static String getFromCookieInfo(String secretKey, String cookieValue) {
		if (StrUtil.isBlank(cookieValue)) {
			return null;
		}

		String[] cookieStrings = cookieValue.split(COOKIE_SEPARATOR);
		if (cookieStrings == null || cookieStrings.length != 4) {
			return null;
		}

		String encryptValue = cookieStrings[0];
		String saveTime = cookieStrings[1];
		String maxAgeInSeconds = cookieStrings[2];
		String value = Base64Kit.decodeToStr(cookieStrings[3]);

		String encrypt = encrypt(secretKey, Long.valueOf(saveTime), maxAgeInSeconds, value);

		// 非常重要，确保 cookie 不被人为修改
		if (!encrypt.equals(encryptValue)) {
			return null;
		}

		long maxTimeMillis = Long.parseLong(maxAgeInSeconds) * 1000;

		//可能设置的时间为 0 或者 -1
		if (maxTimeMillis <= 0) {
			return value;
		}

		long saveTimeMillis = Long.parseLong(saveTime);
		// 查看是否过时
		if ((saveTimeMillis + maxTimeMillis) - System.currentTimeMillis() > 0) {
			return value;
		}
		//已经超时了
		else {
			return null;
		}
	}

	public static Long getLong(Controller ctr, String key) {
		String value = get(ctr, key);
		return null == value ? null : Long.parseLong(value);
	}

	public static long getLong(Controller ctr, String key, long defalut) {
		String value = get(ctr, key);
		return null == value ? defalut : Long.parseLong(value);
	}

	public static Integer getInt(Controller ctr, String key) {
		String value = get(ctr, key);
		return null == value ? null : Integer.parseInt(value);
	}

	public static int getInt(Controller ctr, String key, int defalut) {
		String value = get(ctr, key);
		return null == value ? defalut : Integer.parseInt(value);
	}

	public static BigInteger getBigInteger(Controller ctr, String key) {
		String value = get(ctr, key);
		return null == value ? null : new BigInteger(value);
	}

	public static BigInteger getBigInteger(Controller ctr, String key, BigInteger defalut) {
		String value = get(ctr, key);
		return null == value ? defalut : new BigInteger(value);
	}

}
