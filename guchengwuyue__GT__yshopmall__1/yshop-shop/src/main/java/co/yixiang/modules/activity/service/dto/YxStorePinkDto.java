/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.modules.activity.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
* @author hupeng
* @date 2020-05-12
*/
@Data
public class YxStorePinkDto implements Serializable {

	private Integer id;

	/** 用户id */
	private Integer uid;

	/** 订单id 生成 */
	private String orderId;

	/** 订单id  数据库 */
	private Integer orderIdKey;

	/** 购买商品个数 */
	private Integer totalNum;

	/** 购买总金额 */
	private BigDecimal totalPrice;

	/** 拼团产品id */
	private Integer cid;

	/** 产品id */
	private Integer pid;

	/** 拼图总人数 */
	private Integer people;

	/** 拼团产品单价 */
	private BigDecimal price;

	/** 开始时间 */
	private String addTime;

	private String stopTime;

	/** 团长id 0为团长 */
	private Integer kId;

	/** 是否发送模板消息0未发送1已发送 */
	private Integer isTpl;

	/** 是否退款 0未退款 1已退款 */
	private Integer isRefund;

	/** 状态1进行中2已完成3未完成 */
	private Integer status;
}
