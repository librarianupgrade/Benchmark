/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.mp.controller;

import co.yixiang.exception.BadRequestException;
import co.yixiang.mp.config.WxMpConfiguration;
import co.yixiang.mp.domain.YxWechatMenu;
import co.yixiang.mp.service.YxWechatMenuService;
import co.yixiang.utils.OrderUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* @author hupeng
* @date 2019-10-06
*/
@Api(tags = "商城:微信菜單")
@RestController
@RequestMapping("api")
@SuppressWarnings("unchecked")
public class WechatMenuController {

	private final YxWechatMenuService YxWechatMenuService;

	public WechatMenuController(YxWechatMenuService YxWechatMenuService) {
		this.YxWechatMenuService = YxWechatMenuService;
	}

	@ApiOperation(value = "查询菜单")
	@GetMapping(value = "/YxWechatMenu")
	@PreAuthorize("hasAnyRole('admin','YxWechatMenu_ALL','YxWechatMenu_SELECT')")
	public ResponseEntity getYxWechatMenus() {
		return new ResponseEntity(
				YxWechatMenuService.getOne(new QueryWrapper<YxWechatMenu>().eq("`key`", "wechat_menus")),
				HttpStatus.OK);
	}

	@ApiOperation(value = "创建菜单")
	@PostMapping(value = "/YxWechatMenu")
	@PreAuthorize("hasAnyRole('admin','YxWechatMenu_ALL','YxWechatMenu_CREATE')")
	public ResponseEntity create(@RequestBody String jsonStr) {

		JSONObject jsonObject = JSON.parseObject(jsonStr);
		String jsonButton = jsonObject.get("buttons").toString();
		YxWechatMenu YxWechatMenu = new YxWechatMenu();
		Boolean isExist = YxWechatMenuService.isExist("wechat_menus");
		WxMenu menu = JSONObject.parseObject(jsonStr, WxMenu.class);

		WxMpService wxService = WxMpConfiguration.getWxMpService();
		if (isExist) {
			YxWechatMenu.setKey("wechat_menus");
			YxWechatMenu.setResult(jsonButton);
			YxWechatMenuService.saveOrUpdate(YxWechatMenu);
		} else {
			YxWechatMenu.setKey("wechat_menus");
			YxWechatMenu.setResult(jsonButton);
			YxWechatMenu.setAddTime(OrderUtil.getSecondTimestampTwo());
			YxWechatMenuService.save(YxWechatMenu);
		}

		//创建菜单
		try {
			wxService.getMenuService().menuDelete();
			wxService.getMenuService().menuCreate(menu);
		} catch (WxErrorException e) {
			throw new BadRequestException(e.getMessage());
			// e.printStackTrace();
		}

		return new ResponseEntity(HttpStatus.OK);
	}

}
