package com.zbw.fame.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbw.fame.model.entity.SysOption;

import java.util.Map;

/**
 * 设置 Service 接口
 *
 * @author zzzzbw
 * @since 2019-05-20 22:44
 */
public interface SysOptionService extends IService<SysOption> {

	/**
	 * 获取所有设置的key-value
	 *
	 * @return Map
	 */
	Map<String, String> getAllOptionMap();

	/**
	 * 获取设置value
	 *
	 * @param key          设置key
	 * @param defaultValue 默认值
	 * @return value
	 */
	<T> T get(String key, T defaultValue);

	/**
	 * 获取设置value
	 *
	 * @param key 设置key
	 * @return value
	 */
	String get(String key);

	/**
	 * 保存设置
	 *
	 * @param key   设置key
	 * @param value 设置value
	 */
	void save(String key, String value);

	/**
	 * 批量保存设置
	 *
	 * @param options 设置key-value
	 */
	void save(Map<String, String> options);

	/**
	 * 获取前端使用的设置
	 *
	 * @return Map
	 */
	Map<String, String> getFrontOptionMap();
}
