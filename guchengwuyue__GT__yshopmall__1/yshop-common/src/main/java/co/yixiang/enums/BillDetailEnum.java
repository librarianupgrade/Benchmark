/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author hupeng
 * 账单明细相关枚举
 */
@Getter
@AllArgsConstructor
public enum BillDetailEnum {

	TYPE_1("recharge", "充值"), TYPE_2("brokerage", "返佣"), TYPE_3("pay_product", "消费"), TYPE_4("extract", "提现"),
	TYPE_5("pay_product_refund", "退款"), TYPE_6("system_add", "系统添加"), TYPE_7("system_sub", "系统减少"),

	CATEGORY_1("now_money", "金额"), CATEGORY_2("integral", "积分");

	private String value;
	private String desc;

}
