package com.qcz.qmplatform.module.operation.controller;

import com.qcz.qmplatform.common.aop.annotation.Module;
import com.qcz.qmplatform.common.aop.annotation.RecordLog;
import com.qcz.qmplatform.common.aop.assist.OperateType;
import com.qcz.qmplatform.common.bean.PageRequest;
import com.qcz.qmplatform.common.bean.PageResult;
import com.qcz.qmplatform.common.bean.PageResultHelper;
import com.qcz.qmplatform.common.bean.PrivCode;
import com.qcz.qmplatform.common.bean.ResponseResult;
import com.qcz.qmplatform.common.utils.StringUtils;
import com.qcz.qmplatform.module.base.BaseController;
import com.qcz.qmplatform.module.operation.domain.Dict;
import com.qcz.qmplatform.module.operation.service.DictService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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

/**
 * <p>
 * 字典管理前端控制器
 * </p>
 *
 * @author quchangzhong
 * @since 2020-12-19
 */
@Controller
@RequestMapping("/operation/dict")
@Module("字典管理")
public class DictController extends BaseController {

	private static final String PATH_PREFIX = "/module/operation/";

	@Resource
	private DictService dictService;

	@GetMapping("/dictListPage")
	public String dictListPage() {
		return PATH_PREFIX + "dictList";
	}

	@GetMapping("/dictDetailPage")
	public String dictDetailPage() {
		return PATH_PREFIX + "dictDetail";
	}

	/**
	 * 获取字典列表
	 *
	 * @param pageRequest 分页请求
	 * @param dict        请求参数
	 */
	@PostMapping("/getDictList")
	@ResponseBody
	public ResponseResult<PageResult> getDictList(PageRequest pageRequest, Dict dict) {
		PageResultHelper.startPage(pageRequest);
		return ResponseResult.ok(PageResultHelper.parseResult(dictService.getDictList(dict)));
	}

	/**
	 * 获取字典
	 *
	 * @param dictId 字典id
	 */
	@GetMapping("/getDictOne/{dictId}")
	@ResponseBody
	public ResponseResult<Dict> getDictOne(@PathVariable String dictId) {
		return ResponseResult.ok(dictService.getById(dictId));
	}

	/**
	 * 验证字典编码唯一
	 *
	 * @param dictId   字典id
	 * @param dictCode 字典编码
	 */
	@GetMapping("/validateDictCode")
	@ResponseBody
	public ResponseResult<?> validateDictCode(String dictId, String dictCode) {
		return ResponseResult.newInstance(dictService.validateDictCode(dictId, dictCode));
	}

	/**
	 * 添加字典
	 *
	 * @param dict 字典信息
	 */
	@PostMapping("/addDict")
	@ResponseBody
	@RequiresPermissions(PrivCode.BTN_CODE_DICT_SAVE)
	@RecordLog(type = OperateType.INSERT, description = "新增字典")
	public ResponseResult<?> addDict(@RequestBody Dict dict) {
		return ResponseResult.newInstance(dictService.addDictOne(dict));
	}

	/**
	 * 修改字典
	 *
	 * @param dict 字典信息
	 */
	@PutMapping("/updateDict")
	@ResponseBody
	@RequiresPermissions(PrivCode.BTN_CODE_DICT_SAVE)
	@RecordLog(type = OperateType.UPDATE, description = "修改字典")
	public ResponseResult<?> updateDict(@RequestBody Dict dict) {
		return ResponseResult.newInstance(dictService.updateDictOne(dict));
	}

	/**
	 * 删除字典
	 *
	 * @param dictIds 字典id数组
	 */
	@DeleteMapping("/deleteDict")
	@ResponseBody
	@RequiresPermissions(PrivCode.BTN_CODE_DICT_DELETE)
	@RecordLog(type = OperateType.DELETE, description = "删除字典")
	public ResponseResult<?> deleteDict(String dictIds) {
		return ResponseResult.newInstance(dictService.deleteDict(StringUtils.split(dictIds, ',')));
	}
}
