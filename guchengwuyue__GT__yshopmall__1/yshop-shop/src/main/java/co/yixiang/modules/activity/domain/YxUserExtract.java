/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.modules.activity.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
* @author hupeng
* @date 2020-05-13
*/
@Data
@TableName("yx_user_extract")
public class YxUserExtract implements Serializable {

	@TableId
	private Integer id;

	private Integer uid;

	/** 名称 */
	private String realName;

	/** bank = 银行卡 alipay = 支付宝wx=微信 */
	private String extractType;

	/** 银行卡 */
	private String bankCode;

	/** 开户地址 */
	private String bankAddress;

	/** 支付宝账号 */
	private String alipayCode;

	/** 提现金额 */
	private BigDecimal extractPrice;

	private String mark;

	private BigDecimal balance;

	/** 无效原因 */
	private String failMsg;

	private Integer failTime;

	/** 添加时间 */
	private Integer addTime;

	/** -1 未通过 0 审核中 1 已提现 */
	private Integer status;

	/** 微信号 */
	private String wechat;

	public void copy(YxUserExtract source) {
		BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
	}
}
