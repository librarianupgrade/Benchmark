/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.integration.order.controller;

import com.alibaba.cloud.integration.common.BusinessException;
import com.alibaba.cloud.integration.common.Result;
import com.alibaba.cloud.integration.order.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author TrevorLink
 */
@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@PostMapping("/create")
	public Result<?> createOrder(@RequestParam("userId") String userId,
			@RequestParam("commodityCode") String commodityCode, @RequestParam("count") Integer count) {
		Result<?> res = null;
		try {
			res = orderService.createOrder(userId, commodityCode, count);
		} catch (BusinessException e) {
			return Result.failed(e.getMessage());
		}
		return res;
	}

}
