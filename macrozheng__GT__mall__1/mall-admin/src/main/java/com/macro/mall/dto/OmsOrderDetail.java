package com.macro.mall.dto;

import com.macro.mall.model.OmsOrder;
import com.macro.mall.model.OmsOrderItem;
import com.macro.mall.model.OmsOrderOperateHistory;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 订单详情信息
 * Created by macro on 2018/10/11.
 */
public class OmsOrderDetail extends OmsOrder {
	@Getter
	@Setter
	@ApiModelProperty("订单商品列表")
	private List<OmsOrderItem> orderItemList;
	@Getter
	@Setter
	@ApiModelProperty("订单操作记录列表")
	private List<OmsOrderOperateHistory> historyList;
}
