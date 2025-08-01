/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.utils;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页工具
 * @author Zheng Jie
 * @date 2018-12-10
 */
public class PageUtil extends cn.hutool.core.util.PageUtil {

	/**
	 * List 分页
	 */
	public static List toPage(int page, int size, List list) {
		int fromIndex = page * size;
		int toIndex = page * size + size;
		if (fromIndex > list.size()) {
			return new ArrayList();
		} else if (toIndex >= list.size()) {
			return list.subList(fromIndex, list.size());
		} else {
			return list.subList(fromIndex, toIndex);
		}
	}

	/**
	 * Page 数据处理，预防redis反序列化报错
	 */
	public static Map<String, Object> toPage(Page page) {
		Map<String, Object> map = new LinkedHashMap<>(2);
		map.put("content", page.getContent());
		map.put("totalElements", page.getTotalElements());
		return map;
	}

	/**
	 * 自定义分页
	 */
	public static Map<String, Object> toPage(Object object, Object totalElements) {
		Map<String, Object> map = new LinkedHashMap<>(2);
		map.put("content", object);
		map.put("totalElements", totalElements);
		return map;
	}

}
