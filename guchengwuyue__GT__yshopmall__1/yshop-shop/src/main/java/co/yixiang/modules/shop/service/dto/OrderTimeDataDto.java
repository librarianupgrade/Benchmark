/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.modules.shop.service.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName OrderTimeDataDTO
 * @Author hupeng <610796224@qq.com>
 * @Date 2019/11/25
 **/
@Data
public class OrderTimeDataDto implements Serializable {
	private Double todayPrice; //今日成交额
	private Integer todayCount; //今日订单数
	private Double proPrice; //昨日成交额
	private Integer proCount;//昨日订单数
	private Double monthPrice;//本月成交额
	private Integer monthCount;//本月订单数

	private Integer lastWeekCount;//上周
	private Double lastWeekPrice; //上周

	private Integer userCount;
	private Integer orderCount;
	private Double priceCount;
	private Integer goodsCount;
}
