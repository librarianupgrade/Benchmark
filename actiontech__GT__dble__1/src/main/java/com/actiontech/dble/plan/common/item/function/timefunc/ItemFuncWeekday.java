/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.plan.common.item.function.timefunc;

import com.actiontech.dble.plan.common.item.Item;
import com.actiontech.dble.plan.common.item.function.ItemFunc;
import com.actiontech.dble.plan.common.item.function.primary.ItemIntFunc;
import com.actiontech.dble.plan.common.time.MySQLTime;
import com.actiontech.dble.plan.common.time.MyTime;

import java.math.BigInteger;
import java.util.List;

public class ItemFuncWeekday extends ItemIntFunc {

	public ItemFuncWeekday(List<Item> args, int charsetIndex) {
		super(args, charsetIndex);
	}

	@Override
	public final String funcName() {
		return "weekday";
	}

	@Override
	public BigInteger valInt() {
		MySQLTime ltime = new MySQLTime();

		if (getArg0Date(ltime, MyTime.TIME_NO_ZERO_DATE))
			return BigInteger.ZERO;

		return BigInteger.valueOf(
				MyTime.calcWeekday(MyTime.calcDaynr(ltime.getYear(), ltime.getMonth(), ltime.getDay()), false));
	}

	@Override
	public void fixLengthAndDec() {
		fixCharLength(1);
		maybeNull = true;
	}

	@Override
	public ItemFunc nativeConstruct(List<Item> realArgs) {
		return new ItemFuncWeekday(realArgs, charsetIndex);
	}

}
