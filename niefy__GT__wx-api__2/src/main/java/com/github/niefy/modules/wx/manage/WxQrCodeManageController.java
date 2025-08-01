package com.github.niefy.modules.wx.manage;

import com.github.niefy.modules.wx.form.WxQrCodeForm;
import com.github.niefy.modules.wx.service.WxQrCodeService;
import com.github.niefy.common.utils.PageUtils;
import com.github.niefy.common.utils.R;
import com.github.niefy.modules.wx.entity.WxQrCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 公众号带参二维码管理
 * https://github.com/Wechat-Group/WxJava/wiki/MP_二维码管理
 */
@RestController
@RequestMapping("/manage/wxQrCode")
@Api(tags = { "公众号带参二维码-管理后台" })
public class WxQrCodeManageController {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private WxQrCodeService wxQrCodeService;
	@Autowired
	private WxMpService wxMpService;

	/**
	 * 创建带参二维码ticket
	 */
	@PostMapping("/createTicket")
	@RequiresPermissions("wx:wxqrcode:save")
	@ApiOperation(value = "创建带参二维码ticket", notes = "ticket可以换取二维码图片")
	public R createTicket(@CookieValue String appid, @RequestBody WxQrCodeForm form) throws WxErrorException {
		wxMpService.switchoverTo(appid);
		WxMpQrCodeTicket ticket = wxQrCodeService.createQrCode(appid, form);
		return R.ok().put(ticket);
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	@RequiresPermissions("wx:wxqrcode:list")
	@ApiOperation(value = "列表")
	public R list(@CookieValue String appid, @RequestParam Map<String, Object> params) {
		params.put("appid", appid);
		PageUtils page = wxQrCodeService.queryPage(params);

		return R.ok().put("page", page);
	}

	/**
	 * 信息
	 */
	@GetMapping("/info/{id}")
	@RequiresPermissions("wx:wxqrcode:info")
	@ApiOperation(value = "详情")
	public R info(@CookieValue String appid, @PathVariable("id") Long id) {
		WxQrCode wxQrCode = wxQrCodeService.getById(id);

		return R.ok().put("wxQrCode", wxQrCode);
	}

	/**
	 * 删除
	 */
	@PostMapping("/delete")
	@RequiresPermissions("wx:wxqrcode:delete")
	@ApiOperation(value = "删除")
	public R delete(@CookieValue String appid, @RequestBody Long[] ids) {
		wxQrCodeService.removeByIds(Arrays.asList(ids));

		return R.ok();
	}
}
