package com.paritytrading.parity.ticker;

import com.paritytrading.foundation.ASCII;
import com.paritytrading.parity.top.Side;
import com.paritytrading.parity.util.Timestamps;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import java.util.List;
import java.util.Locale;

class DisplayFormat extends MarketDataListener {

	private static final double PRICE_FACTOR = 10000.0;

	private static final String HEADER = ""
			+ "Timestamp    Inst     Bid Px    Bid Size   Ask Px    Ask Size   Last Px   Last Size\n"
			+ "------------ -------- --------- ---------- --------- ---------- --------- ----------";

	private static final String MISSING = String.format("%9s %10s", "-", "-");

	private Long2ObjectArrayMap<Trade> trades;

	private long second;
	private long timestamp;

	private int counter;

	public DisplayFormat(List<String> instruments) {
		trades = new Long2ObjectArrayMap<>();

		for (String instrument : instruments)
			trades.put(ASCII.packLong(instrument), new Trade());

		printf("\n%s\n", HEADER);
	}

	@Override
	public void bbo(long instrument, long bidPrice, long bidSize, long askPrice, long askSize) {
		Trade trade = trades.get(instrument);

		printf("%12s %8s ", Timestamps.format(timestampMillis()), ASCII.unpackLong(instrument));

		if (bidSize != 0)
			printf("%9.2f %10d ", bidPrice / PRICE_FACTOR, bidSize);
		else
			printf("%s ", MISSING);

		if (askSize != 0)
			printf("%9.2f %10d ", askPrice / PRICE_FACTOR, askSize);
		else
			printf("%s ", MISSING);

		if (trade.size != 0)
			printf("%9.2f %10d\n", trade.price / PRICE_FACTOR, trade.size);
		else
			printf("%s\n", MISSING);
	}

	@Override
	public void trade(long instrument, Side side, long price, long size) {
		Trade trade = trades.get(instrument);

		trade.price = price;
		trade.size = size;
	}

	private void printf(String format, Object... args) {
		System.out.printf(Locale.US, format, args);
	}

	private static class Trade {
		public long price;
		public long size;
	}

}
