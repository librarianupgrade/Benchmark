package com.qcz.qmplatform.module.system.controller;

import com.qcz.qmplatform.common.bean.PageRequest;
import com.qcz.qmplatform.common.bean.PageResult;
import com.qcz.qmplatform.common.bean.PageResultHelper;
import com.qcz.qmplatform.common.bean.ResponseResult;
import com.qcz.qmplatform.common.utils.StringUtils;
import com.qcz.qmplatform.module.base.BaseController;
import com.qcz.qmplatform.module.system.domain.Button;
import com.qcz.qmplatform.module.system.service.ButtonService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author quchangzhong
 * @since 2020-09-03
 */
@Controller
@RequestMapping("/button")
public class ButtonController extends BaseController {

	private static final String PREFIX = "/module/system/button/";

	@Resource
	private ButtonService buttonService;

	@GetMapping("/buttonListPage")
	public String buttonListPage() {
		return PREFIX + "buttonList";
	}

	/**
	 * 获取按钮列表
	 *
	 * @param pageRequest 分页请求
	 * @param button      请求参数
	 */
	@PostMapping("/getButtonList")
	@ResponseBody
	public ResponseResult<PageResult> getButtonList(PageRequest pageRequest, Button button) {
		PageResultHelper.startPage(pageRequest.getPage(), pageRequest.getLimit());
		List<Button> buttonList = buttonService.getButtonList(button);
		return ResponseResult.ok(PageResultHelper.parseResult(buttonList));
	}

	/**
	 * 获取按钮信息
	 *
	 * @param buttonId 按钮id
	 * @return ResponseResult<Button>
	 */
	@GetMapping("/getButtonOne/{buttonId}")
	@ResponseBody
	public ResponseResult<Button> getButtonOne(@PathVariable String buttonId) {
		return ResponseResult.ok(buttonService.getById(buttonId));
	}

	/**
	 * 添加按钮信息
	 *
	 * @param button 按钮表单信息
	 * @return ResponseResult
	 */
	@PostMapping("/addButtonOne")
	@ResponseBody
	public ResponseResult<?> addButtonOne(@RequestBody Button button) {
		return ResponseResult.newInstance(buttonService.addButtonOne(button));
	}

	/**
	 * 修改按钮信息
	 *
	 * @param button 按钮表单信息
	 */
	@PutMapping("/updateButtonOne")
	@ResponseBody
	public ResponseResult<?> updateButtonOne(@RequestBody Button button) {
		return ResponseResult.newInstance(buttonService.updateButtonOne(button));
	}

	/**
	 * 删除按钮信息
	 *
	 * @param permissionIds 按钮id数组
	 */
	@DeleteMapping("/deleteButton")
	@ResponseBody
	public ResponseResult<?> deleteButton(String permissionIds) {
		return ResponseResult.newInstance(buttonService.deleteButton(StringUtils.split(permissionIds, ',')));
	}
}
