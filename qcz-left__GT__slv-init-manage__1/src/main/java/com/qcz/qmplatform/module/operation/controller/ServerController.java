package com.qcz.qmplatform.module.operation.controller;

import com.qcz.qmplatform.common.bean.ResponseResult;
import com.qcz.qmplatform.common.utils.SystemUtils;
import com.qcz.qmplatform.module.base.BaseController;
import com.qcz.qmplatform.module.operation.pojo.ServerInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 服务器运行状态
 */
@Controller
@RequestMapping("/server")
public class ServerController extends BaseController {

	private static final String PREFIX = "/module/operation/";

	@GetMapping("/infoPage")
	public String infoPage(Map<String, Object> root) {
		root.put("computer", SystemUtils.getComputer());
		root.put("mem", SystemUtils.getMem());
		root.put("disk", SystemUtils.getDisk());
		return PREFIX + "serverInfo";
	}

	@GetMapping("/info")
	@ResponseBody
	public ResponseResult<ServerInfo> getInfo() {
		return ResponseResult.ok(SystemUtils.getServerInfo());
	}

}
