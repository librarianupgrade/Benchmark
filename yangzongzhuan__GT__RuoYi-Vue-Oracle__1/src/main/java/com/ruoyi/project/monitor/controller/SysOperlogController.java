package com.ruoyi.project.monitor.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.monitor.domain.SysOperLog;
import com.ruoyi.project.monitor.service.ISysOperLogService;

/**
 * 操作日志记录
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController extends BaseController {
	@Autowired
	private ISysOperLogService operLogService;

	@PreAuthorize("@ss.hasPermi('monitor:operlog:list')")
	@GetMapping("/list")
	public TableDataInfo list(SysOperLog operLog) {
		startPage();
		List<SysOperLog> list = operLogService.selectOperLogList(operLog);
		return getDataTable(list);
	}

	@Log(title = "操作日志", businessType = BusinessType.EXPORT)
	@PreAuthorize("@ss.hasPermi('monitor:operlog:export')")
	@GetMapping("/export")
	public AjaxResult export(SysOperLog operLog) {
		List<SysOperLog> list = operLogService.selectOperLogList(operLog);
		ExcelUtil<SysOperLog> util = new ExcelUtil<SysOperLog>(SysOperLog.class);
		return util.exportExcel(list, "操作日志");
	}

	@Log(title = "操作日志", businessType = BusinessType.DELETE)
	@PreAuthorize("@ss.hasPermi('monitor:operlog:remove')")
	@DeleteMapping("/{operIds}")
	public AjaxResult remove(@PathVariable Long[] operIds) {
		return toAjax(operLogService.deleteOperLogByIds(operIds));
	}

	@Log(title = "操作日志", businessType = BusinessType.CLEAN)
	@PreAuthorize("@ss.hasPermi('monitor:operlog:remove')")
	@DeleteMapping("/clean")
	public AjaxResult clean() {
		operLogService.cleanOperLog();
		return AjaxResult.success();
	}
}
