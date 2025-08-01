package com.qcz.qmplatform.module.other.controller;

import com.qcz.qmplatform.common.aop.annotation.Module;
import com.qcz.qmplatform.common.aop.annotation.RecordLog;
import com.qcz.qmplatform.common.aop.assist.OperateType;
import com.qcz.qmplatform.common.bean.PageRequest;
import com.qcz.qmplatform.common.bean.PageResult;
import com.qcz.qmplatform.common.bean.PageResultHelper;
import com.qcz.qmplatform.common.bean.ResponseResult;
import com.qcz.qmplatform.common.utils.StringUtils;
import com.qcz.qmplatform.module.base.BaseController;
import com.qcz.qmplatform.module.other.domain.Notepad;
import com.qcz.qmplatform.module.other.service.NotepadService;
import com.qcz.qmplatform.module.other.vo.NotepadVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 记事本 前端控制器
 * </p>
 *
 * @author quchangzhong
 * @since 2021-11-04
 */
@Controller
@RequestMapping("/other/notepad")
@Module("记事本")
public class NotepadController extends BaseController {

	private static final String PREFIX = "/module/other/";

	@Resource
	NotepadService notepadService;

	@GetMapping("/detailPage")
	public String detailPage() {
		return PREFIX + "notepadDetail";
	}

	@GetMapping("/listPage")
	public String listPage() {
		return PREFIX + "notepadList";
	}

	@PostMapping("/list")
	@ResponseBody
	public ResponseResult<PageResult> list(PageRequest pageRequest, NotepadVO notepadVO) {
		PageResultHelper.startPage(pageRequest.getPage(), pageRequest.getLimit());
		List<Notepad> notepadList = notepadService.getList(notepadVO);
		return ResponseResult.ok(PageResultHelper.parseResult(notepadList));
	}

	@GetMapping("/getOne/{id}")
	@ResponseBody
	public ResponseResult<Notepad> getOne(@PathVariable String id) {
		return ResponseResult.ok(notepadService.getById(id));
	}

	@PostMapping("/insert")
	@ResponseBody
	@RecordLog(type = OperateType.INSERT, description = "添加记事本")
	public ResponseResult<?> insert(@RequestBody Notepad notepad) {
		return ResponseResult.newInstance(notepadService.saveOne(notepad));
	}

	@PostMapping("/update")
	@ResponseBody
	@RecordLog(type = OperateType.UPDATE, description = "编辑记事本")
	public ResponseResult<?> update(@RequestBody Notepad notepad) {
		return ResponseResult.newInstance(notepadService.updateOne(notepad));
	}

	@PostMapping("/delete")
	@ResponseBody
	@RecordLog(type = OperateType.DELETE, description = "删除记事本")
	public ResponseResult<?> delete(String ids) {
		return ResponseResult.newInstance(notepadService.removeByIds(StringUtils.split(ids, ',')));
	}

}
