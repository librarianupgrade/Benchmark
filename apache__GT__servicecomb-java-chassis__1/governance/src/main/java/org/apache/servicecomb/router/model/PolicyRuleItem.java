/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.router.model;

import java.util.List;

import org.apache.servicecomb.governance.marker.Matcher;
import org.apache.servicecomb.router.exception.RouterIllegalParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class PolicyRuleItem implements Comparable<PolicyRuleItem> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PolicyRuleItem.class);

	private Integer precedence;

	private Matcher match;

	private List<RouteItem> route;

	private Integer total;

	private boolean weightLess = false;

	private List<RouteItem> fallback;

	private Integer fallbackTotal;

	private boolean emptyProtection = true;

	public PolicyRuleItem() {
	}

	/**
	 * if weight is less than 100, fill with minimum version
	 *
	 */
	public void check() {
		if (CollectionUtils.isEmpty(route)) {
			throw new RouterIllegalParamException("canary rule list can not be null");
		}
		int sum = 0;
		for (RouteItem item : route) {
			if (item.getWeight() == null) {
				throw new RouterIllegalParamException("canary rule weight can not be null");
			}
			sum += item.getWeight();
		}
		if (sum > 100) {
			LOGGER.warn("canary rule weight sum is more than 100");
		} else if (sum < 100) {
			weightLess = true;
			route.add(new RouteItem(100 - sum, null));
		}
	}

	@Override
	public int compareTo(PolicyRuleItem param) {
		return Integer.compare(param.precedence, this.precedence);
	}

	public Integer getPrecedence() {
		return precedence;
	}

	public void setPrecedence(Integer precedence) {
		this.precedence = precedence;
	}

	public Matcher getMatch() {
		return match;
	}

	public void setMatch(Matcher match) {
		this.match = match;
	}

	public List<RouteItem> getRoute() {
		return route;
	}

	public void setRoute(List<RouteItem> route) {
		this.route = route;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public boolean isWeightLess() {
		return weightLess;
	}

	public void setWeightLess(boolean weightLess) {
		this.weightLess = weightLess;
	}

	public List<RouteItem> getFallback() {
		return fallback;
	}

	public void setFallback(List<RouteItem> fallback) {
		this.fallback = fallback;
	}

	public Integer getFallbackTotal() {
		return fallbackTotal;
	}

	public void setFallbackTotal(Integer fallbackTotal) {
		this.fallbackTotal = fallbackTotal;
	}

	public boolean isEmptyProtection() {
		return emptyProtection;
	}

	public void setEmptyProtection(boolean emptyProtection) {
		this.emptyProtection = emptyProtection;
	}

	@Override
	public String toString() {
		return "PolicyRuleItem{" + "precedence=" + precedence + ", match=" + match + ", route=" + route + ", total="
				+ total + ", weightLess=" + weightLess + ", fallback=" + fallback + ", fallbackTotal=" + fallbackTotal
				+ ", emptyProtection=" + emptyProtection + '}';
	}
}
