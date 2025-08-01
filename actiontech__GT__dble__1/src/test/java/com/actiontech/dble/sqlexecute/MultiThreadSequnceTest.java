/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.sqlexecute;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MultiThreadSequnceTest {
	private static void testSequnce(Connection theCon) throws SQLException {
		try {
			theCon.setAutoCommit(false);
			String sql = "select next value for DBLESEQ_GLOBAL ";
			Statement stmt = theCon.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				System.out.println(Thread.currentThread().getName() + " get seq " + rs.getLong(1));
			} else {
				System.out.println(Thread.currentThread().getName() + " can't get  seq ");
			}

			theCon.commit();
			stmt.close();
		} finally {
			theCon.close();
		}
	}

	private static Connection getCon(String url, String user, String passwd) throws SQLException {
		Connection theCon = DriverManager.getConnection(url, user, passwd);
		return theCon;
	}

	public static void main(String[] args) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		final String url = "jdbc:mysql://localhost:8066/TESTDB";
		final String user = "test";
		final String password = "test";
		List<Thread> threads = new ArrayList<Thread>(100);
		for (int i = 0; i < 100; i++) {

			threads.add(new Thread() {
				public void run() {
					Connection con;
					try {
						con = getCon(url, user, password);
						testSequnce(con);
					} catch (SQLException e) {

						e.printStackTrace();
					}

				}
			});

		}
		for (Thread thred : threads) {
			thred.start();

		}
		boolean hasRunning = true;
		while (hasRunning) {
			hasRunning = false;
			for (Thread thred : threads) {
				if (thred.isAlive()) {
					try {
						Thread.sleep(1000);
						hasRunning = true;
					} catch (InterruptedException e) {

					}
				}

			}
		}

	}
}
