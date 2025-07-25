package com.qcz.qmplatform.module.system.vo;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class PasswordVO implements Serializable {

	/**
	 * 登录名
	 */
	private String loginname;

	/**
	 * 原密码
	 */
	private String password;

	/**
	 * 新密码
	 */
	@NotBlank(message = "原密码不能为空")
	private String newPassword;

	/**
	 * 确认新密码
	 */
	@NotBlank(message = "确认密码不能为空")
	private String confirmNewPassword;

	/**
	 * 验证码
	 */
	private String validateCode;

	/**
	 * 验证方式类型
	 */
	private int validateType;

	public String getLoginname() {
		return loginname;
	}

	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}

	public String getValidateCode() {
		return validateCode;
	}

	public void setValidateCode(String validateCode) {
		this.validateCode = validateCode;
	}

	public int getValidateType() {
		return validateType;
	}

	public void setValidateType(int validateType) {
		this.validateType = validateType;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmNewPassword() {
		return confirmNewPassword;
	}

	public void setConfirmNewPassword(String confirmNewPassword) {
		this.confirmNewPassword = confirmNewPassword;
	}
}
