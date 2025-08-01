/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.plan.common.item.function.mathsfunc;

import com.actiontech.dble.plan.common.MySQLcom;
import com.actiontech.dble.plan.common.item.Item;
import com.actiontech.dble.plan.common.item.function.ItemFunc;
import com.actiontech.dble.plan.common.item.function.primary.ItemFuncUnits;

import java.util.List;

public class ItemFuncRadians extends ItemFuncUnits {

	public ItemFuncRadians(List<Item> args, int charsetIndex) {
		super(args, MySQLcom.M_PI / 180, 0.0, charsetIndex);
	}

	@Override
	public final String funcName() {
		return "radians";
	}

	@Override
	public ItemFunc nativeConstruct(List<Item> realArgs) {
		return new ItemFuncRadians(realArgs, charsetIndex);
	}
}
