/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * 版权所有，侵权必究！
 */

package com.github.niefy.modules.sys.controller;

import com.github.niefy.common.utils.PageUtils;
import com.github.niefy.common.utils.R;
import com.github.niefy.modules.sys.service.SysLogService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 系统日志
 * @author Mark sunlightcs@gmail.com
 */
@Controller
@RequestMapping("/sys/log")
public class SysLogController {
	@Autowired
	private SysLogService sysLogService;

	/**
	 * 列表
	 */
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("sys:log:list")
	public R list(@RequestParam Map<String, Object> params) {
		PageUtils page = sysLogService.queryPage(params);

		return R.ok().put("page", page);
	}

}
