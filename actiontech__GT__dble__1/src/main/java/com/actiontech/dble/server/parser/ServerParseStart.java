/*
 * Copyright (C) 2016-2023 ActionTech.
 * based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */
package com.actiontech.dble.server.parser;

import com.actiontech.dble.route.parser.util.ParseUtil;

/**
 * @author mycat
 */
public class ServerParseStart {
	public ServerParseStart() {
	}

	public int parse(String stmt, int offset) {
		int i = offset;
		for (; i < stmt.length(); i++) {
			switch (stmt.charAt(i)) {
			case ' ':
				continue;
			case '/':
			case '#':
				i = ParseUtil.comment(stmt, i);
				continue;
			case 'T':
			case 't':
				return transactionCheck(stmt, i);
			default:
				return ServerParse.OTHER;
			}
		}
		return ServerParse.OTHER;
	}

	protected int transactionCheck(String stmt, int offset) {
		if (stmt.length() > offset + 10) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			char c6 = stmt.charAt(++offset);
			char c7 = stmt.charAt(++offset);
			char c8 = stmt.charAt(++offset);
			char c9 = stmt.charAt(++offset);
			char c10 = stmt.charAt(++offset);
			if ((c1 == 'R' || c1 == 'r') && (c2 == 'A' || c2 == 'a') && (c3 == 'N' || c3 == 'n')
					&& (c4 == 'S' || c4 == 's') && (c5 == 'A' || c5 == 'a') && (c6 == 'C' || c6 == 'c')
					&& (c7 == 'T' || c7 == 't') && (c8 == 'I' || c8 == 'i') && (c9 == 'O' || c9 == 'o')
					&& (c10 == 'N' || c10 == 'n')) {
				if (stmt.length() == ++offset)
					return ServerParse.START_TRANSACTION;
				int currentOffset = ParseUtil.skipSpace(stmt, offset);
				if (stmt.length() == currentOffset) {
					return ServerParse.START_TRANSACTION;
				} else {
					int currentOffset2 = ParseUtil.commentHint(stmt, currentOffset);
					if (stmt.length() == ++currentOffset2
							|| stmt.length() == ParseUtil.skipSpace(stmt, currentOffset2)) {
						return ServerParse.START_TRANSACTION;
					} else {
						return transactionReadCheck0(stmt, currentOffset);
					}
				}
			}
		}
		return ServerParse.OTHER;
	}

	protected int transactionReadCheck0(String stmt, int offset) {
		if (stmt.length() >= offset + "read ".length()) {
			char c0 = stmt.charAt(offset);
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			if ((c0 == 'R' || c0 == 'r') && (c1 == 'E' || c1 == 'e') && (c2 == 'A' || c2 == 'a')
					&& (c3 == 'D' || c3 == 'd') && ParseUtil.isSpace(c4)) {
				return ServerParse.UNSUPPORT;
			}
		}
		return ServerParse.OTHER;
	}
}
