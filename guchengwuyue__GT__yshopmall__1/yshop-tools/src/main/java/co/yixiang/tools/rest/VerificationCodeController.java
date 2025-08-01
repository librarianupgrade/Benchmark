/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.tools.rest;

import co.yixiang.tools.domain.VerificationCode;
import co.yixiang.tools.domain.vo.EmailVo;
import co.yixiang.tools.service.EmailConfigService;
import co.yixiang.tools.service.VerificationCodeService;
import co.yixiang.utils.YshopConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hupeng
 * @date 2018-12-26
 */
@RestController
@RequestMapping("/api/code")
@Api(tags = "工具：验证码管理")
public class VerificationCodeController {

	private final VerificationCodeService verificationCodeService;

	private final EmailConfigService emailService;

	public VerificationCodeController(VerificationCodeService verificationCodeService,
			EmailConfigService emailService) {
		this.verificationCodeService = verificationCodeService;
		this.emailService = emailService;
	}

	@PostMapping(value = "/resetEmail")
	@ApiOperation("重置邮箱，发送验证码")
	public ResponseEntity<Object> resetEmail(@RequestBody VerificationCode code) throws Exception {
		code.setScenes(YshopConstant.RESET_MAIL);
		EmailVo emailVo = verificationCodeService.sendEmail(code);
		emailService.send(emailVo, emailService.find());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/email/resetPass")
	@ApiOperation("重置密码，发送验证码")
	public ResponseEntity<Object> resetPass(@RequestParam String email) throws Exception {
		VerificationCode code = new VerificationCode();
		code.setType("email");
		code.setValue(email);
		code.setScenes(YshopConstant.RESET_MAIL);
		EmailVo emailVo = verificationCodeService.sendEmail(code);
		emailService.send(emailVo, emailService.find());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/validated")
	@ApiOperation("验证码验证")
	public ResponseEntity<Object> validated(VerificationCode code) {
		verificationCodeService.validated(code);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
