/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.tools.rest;

import co.yixiang.logging.aop.log.Log;
import co.yixiang.tools.domain.EmailConfig;
import co.yixiang.tools.domain.vo.EmailVo;
import co.yixiang.tools.service.EmailConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发送邮件
 * @author 郑杰
 * @date 2018/09/28 6:55:53
 */
@RestController
@RequestMapping("api/email")
@Api(tags = "工具：邮件管理")
public class EmailController {

	private final EmailConfigService emailService;

	public EmailController(EmailConfigService emailService) {
		this.emailService = emailService;
	}

	@GetMapping
	public ResponseEntity<Object> get() {
		return new ResponseEntity<>(emailService.find(), HttpStatus.OK);
	}

	@Log("配置邮件")
	@PutMapping
	@ApiOperation("配置邮件")
	public ResponseEntity<Object> emailConfig(@Validated @RequestBody EmailConfig emailConfig) {
		emailService.update(emailConfig, emailService.find());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Log("发送邮件")
	@PostMapping
	@ApiOperation("发送邮件")
	public ResponseEntity<Object> send(@Validated @RequestBody EmailVo emailVo) throws Exception {
		emailService.send(emailVo, emailService.find());
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
