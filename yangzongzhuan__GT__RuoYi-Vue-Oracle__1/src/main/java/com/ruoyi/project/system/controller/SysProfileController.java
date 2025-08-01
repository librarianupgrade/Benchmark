package com.ruoyi.project.system.controller;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.framework.security.LoginUser;
import com.ruoyi.framework.security.service.TokenService;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.system.domain.SysUser;
import com.ruoyi.project.system.service.ISysUserService;

/**
 * 个人信息 业务处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController {
	@Autowired
	private ISysUserService userService;

	@Autowired
	private TokenService tokenService;

	/**
	 * 个人信息
	 */
	@GetMapping
	public AjaxResult profile() {
		LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
		SysUser user = loginUser.getUser();
		AjaxResult ajax = AjaxResult.success(user);
		ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
		ajax.put("postGroup", userService.selectUserPostGroup(loginUser.getUsername()));
		return ajax;
	}

	/**
	 * 修改用户
	 */
	@Log(title = "个人信息", businessType = BusinessType.UPDATE)
	@PutMapping
	public AjaxResult updateProfile(@RequestBody SysUser user) {
		if (StringUtils.isNotEmpty(user.getPhonenumber())
				&& UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
			return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
		} else if (StringUtils.isNotEmpty(user.getEmail())
				&& UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
			return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
		}
		if (userService.updateUserProfile(user) > 0) {
			LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
			// 更新缓存用户信息
			loginUser.getUser().setNickName(user.getNickName());
			loginUser.getUser().setPhonenumber(user.getPhonenumber());
			loginUser.getUser().setEmail(user.getEmail());
			loginUser.getUser().setSex(user.getSex());
			tokenService.setLoginUser(loginUser);
			return AjaxResult.success();
		}
		return AjaxResult.error("修改个人信息异常，请联系管理员");
	}

	/**
	 * 重置密码
	 */
	@Log(title = "个人信息", businessType = BusinessType.UPDATE)
	@PutMapping("/updatePwd")
	public AjaxResult updatePwd(String oldPassword, String newPassword) {
		LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
		String userName = loginUser.getUsername();
		String password = loginUser.getPassword();
		if (!SecurityUtils.matchesPassword(oldPassword, password)) {
			return AjaxResult.error("修改密码失败，旧密码错误");
		}
		if (SecurityUtils.matchesPassword(newPassword, password)) {
			return AjaxResult.error("新密码不能与旧密码相同");
		}
		if (userService.resetUserPwd(userName, SecurityUtils.encryptPassword(newPassword)) > 0) {
			// 更新缓存用户密码
			loginUser.getUser().setPassword(SecurityUtils.encryptPassword(newPassword));
			tokenService.setLoginUser(loginUser);
			return AjaxResult.success();
		}
		return AjaxResult.error("修改密码异常，请联系管理员");
	}

	/**
	 * 头像上传
	 */
	@Log(title = "用户头像", businessType = BusinessType.UPDATE)
	@PostMapping("/avatar")
	public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) throws IOException {
		if (!file.isEmpty()) {
			LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
			String avatar = FileUploadUtils.upload(RuoYiConfig.getAvatarPath(), file);
			if (userService.updateUserAvatar(loginUser.getUsername(), avatar)) {
				AjaxResult ajax = AjaxResult.success();
				ajax.put("imgUrl", avatar);
				// 更新缓存用户头像
				loginUser.getUser().setAvatar(avatar);
				tokenService.setLoginUser(loginUser);
				return ajax;
			}
		}
		return AjaxResult.error("上传图片异常，请联系管理员");
	}
}
