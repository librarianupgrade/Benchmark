package com.github.binarywang.demo.wx.mp.config;

import com.github.binarywang.demo.wx.mp.utils.JsonUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * wechat mp properties
 *
 * @author Binary Wang(https://github.com/binarywang)
 */
@Data
@ConfigurationProperties(prefix = "wx.mp")
public class WxMpProperties {
	/**
	 * 是否使用redis存储access token
	 */
	private boolean useRedis;

	/**
	 * redis 配置
	 */
	private RedisConfig redisConfig;

	@Data
	public static class RedisConfig {
		/**
		 * redis服务器 主机地址
		 */
		private String host;

		/**
		 * redis服务器 端口号
		 */
		private Integer port;
	}

	/**
	 * 多个公众号配置信息
	 */
	private List<MpConfig> configs;

	@Data
	public static class MpConfig {
		/**
		 * 设置微信公众号的appid
		 */
		private String appId;

		/**
		 * 设置微信公众号的app secret
		 */
		private String secret;

		/**
		 * 设置微信公众号的token
		 */
		private String token;

		/**
		 * 设置微信公众号的EncodingAESKey
		 */
		private String aesKey;
	}

	@Override
	public String toString() {
		return JsonUtils.toJson(this);
	}
}
