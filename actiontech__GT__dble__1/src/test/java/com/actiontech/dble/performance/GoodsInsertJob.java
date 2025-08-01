/*
 * Copyright (C) 2016-2023 ActionTech.
 * based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */
package com.actiontech.dble.performance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class GoodsInsertJob implements Runnable {
	private final long endId;
	private long finsihed;
	private final int batchSize;
	private final AtomicLong finshiedCount;
	private final AtomicLong failedCount;
	Calendar date = Calendar.getInstance();
	DateFormat datafomat = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleConPool conPool;

	public GoodsInsertJob(SimpleConPool conPool, long totalRecords, int batchSize, long startId,
			AtomicLong finshiedCount, AtomicLong failedCount) {
		super();
		this.conPool = conPool;
		this.endId = startId + totalRecords - 1;
		this.batchSize = batchSize;
		this.finsihed = startId;
		this.finshiedCount = finshiedCount;
		this.failedCount = failedCount;
	}

	private int insert(Connection con, List<Map<String, String>> list) throws SQLException {
		PreparedStatement ps;
		String sql = "insert into goods (id,name ,good_type,good_img_url,good_created ,good_desc, price ) values(?,? ,?,?,? ,?, ?)";
		ps = con.prepareStatement(sql);
		for (Map<String, String> map : list) {
			ps.setLong(1, Long.parseLong(map.get("id")));
			ps.setString(2, (String) map.get("name"));
			ps.setShort(3, Short.parseShort(map.get("good_type")));
			ps.setString(4, (String) map.get("good_img_url"));
			ps.setString(5, (String) map.get("good_created"));
			ps.setString(6, (String) map.get("good_desc"));
			ps.setDouble(7, Double.parseDouble(map.get("price")));
			ps.addBatch();
		}
		ps.executeBatch();
		return list.size();
	}

	private List<Map<String, String>> getNextBatch() {
		if (finsihed >= endId) {
			return Collections.emptyList();
		}
		long end = (finsihed + batchSize) < this.endId ? (finsihed + batchSize) : endId;
		// the last batch
		if (end + batchSize > this.endId) {
			end = this.endId;
		}
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for (long i = finsihed; i < end; i++) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("id", i + "");
			m.put("name", "googs " + i);
			m.put("good_type", i % 100 + "");
			m.put("good_img_url", "http://openclouddb.org/" + i);
			m.put("good_created", getRandomDay(i));
			m.put("good_desc", "best goods " + i);
			m.put("price", (i + 0.0) % 1000 + "");
			list.add(m);
		}
		finsihed += list.size();
		return list;
	}

	private String getRandomDay(long i) {
		int month = Long.valueOf(i % 11 + 1).intValue();
		int day = Long.valueOf(i % 27 + 1).intValue();

		date.set(Calendar.MONTH, month);
		date.set(Calendar.DAY_OF_MONTH, day);
		return datafomat.format(date.getTime());

	}

	@Override
	public void run() {
		Connection con = null;
		try {

			List<Map<String, String>> batch = getNextBatch();
			while (!batch.isEmpty()) {
				try {
					if (con == null || con.isClosed()) {
						con = conPool.getConnection();
						con.setAutoCommit(true);
					}

					insert(con, batch);
					finshiedCount.addAndGet(batch.size());
				} catch (Exception e) {
					failedCount.addAndGet(batch.size());
					e.printStackTrace();
				}
				batch = getNextBatch();
			}
		} finally {
			if (con != null) {
				this.conPool.returnCon(con);
			}
		}
	}
}