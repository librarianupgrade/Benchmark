/*
 * Copyright (C) 2016-2023 ActionTech.
 * based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */
package com.actiontech.dble.services.manager.response;

import com.actiontech.dble.DbleServer;
import com.actiontech.dble.backend.mysql.PacketUtil;
import com.actiontech.dble.config.Fields;
import com.actiontech.dble.config.model.user.AnalysisUserConfig;
import com.actiontech.dble.net.IOProcessor;
import com.actiontech.dble.net.connection.FrontendConnection;
import com.actiontech.dble.net.mysql.*;
import com.actiontech.dble.net.service.AbstractService;
import com.actiontech.dble.services.manager.ManagerService;
import com.actiontech.dble.services.mysqlsharding.ShardingService;
import com.actiontech.dble.services.rwsplit.RWSplitService;
import com.actiontech.dble.util.FormatUtil;
import com.actiontech.dble.util.LongUtil;
import com.actiontech.dble.util.StringUtil;
import com.actiontech.dble.util.TimeUtil;

import java.nio.ByteBuffer;

/**
 * @author mycat
 */
public final class ShowConnectionSQL {
	private ShowConnectionSQL() {
	}

	private static final int FIELD_COUNT = 8;
	private static final ResultSetHeaderPacket HEADER = PacketUtil.getHeader(FIELD_COUNT);
	private static final FieldPacket[] FIELDS = new FieldPacket[FIELD_COUNT];
	private static final EOFPacket EOF = new EOFPacket();

	static {
		int i = 0;
		byte packetId = 0;
		HEADER.setPacketId(++packetId);

		FIELDS[i] = PacketUtil.getField("FRONT_ID", Fields.FIELD_TYPE_LONG);
		FIELDS[i++].setPacketId(++packetId);

		FIELDS[i] = PacketUtil.getField("HOST", Fields.FIELD_TYPE_VAR_STRING);
		FIELDS[i++].setPacketId(++packetId);

		FIELDS[i] = PacketUtil.getField("USER", Fields.FIELD_TYPE_VAR_STRING);
		FIELDS[i++].setPacketId(++packetId);

		FIELDS[i] = PacketUtil.getField("SCHEMA", Fields.FIELD_TYPE_VAR_STRING);
		FIELDS[i++].setPacketId(++packetId);

		FIELDS[i] = PacketUtil.getField("START_TIME", Fields.FIELD_TYPE_VAR_STRING);
		FIELDS[i++].setPacketId(++packetId);

		FIELDS[i] = PacketUtil.getField("EXECUTE_TIME", Fields.FIELD_TYPE_LONGLONG);
		FIELDS[i++].setPacketId(++packetId);

		FIELDS[i] = PacketUtil.getField("SQL", Fields.FIELD_TYPE_VAR_STRING);
		FIELDS[i++].setPacketId(++packetId);

		FIELDS[i] = PacketUtil.getField("STAGE", Fields.FIELD_TYPE_VAR_STRING);
		FIELDS[i].setPacketId(++packetId);

		EOF.setPacketId(++packetId);
	}

	public static void execute(ManagerService service) {
		ByteBuffer buffer = service.allocate();

		// write header
		buffer = HEADER.write(buffer, service, true);

		// write fields
		for (FieldPacket field : FIELDS) {
			buffer = field.write(buffer, service, true);
		}

		// write eof
		buffer = EOF.write(buffer, service, true);

		// write rows
		byte packetId = EOF.getPacketId();
		for (IOProcessor p : DbleServer.getInstance().getFrontProcessors()) {
			for (FrontendConnection fc : p.getFrontends().values()) {
				if (!fc.isClosed() && fc.isAuthorized()) {
					RowDataPacket row = getRow(fc, service.getCharset().getResults());
					row.setPacketId(++packetId);
					buffer = row.write(buffer, service, true);
				}
			}
		}

		// write last eof
		EOFRowPacket lastEof = new EOFRowPacket();
		lastEof.setPacketId(++packetId);

		lastEof.write(buffer, service);

	}

	private static RowDataPacket getRow(FrontendConnection c, String charset) {
		RowDataPacket row = new RowDataPacket(FIELD_COUNT);
		row.add(LongUtil.toBytes(c.getId()));
		row.add(StringUtil.encode(c.getHost(), charset));
		row.add(StringUtil.encode(c.getFrontEndService().getUser().getFullName(), charset));
		AbstractService service = c.getService();
		String executeSql = c.getFrontEndService().getExecuteSql();
		if (executeSql != null) {
			executeSql = executeSql.length() <= 1024 ? executeSql : executeSql.substring(0, 1024);
		} else {
			executeSql = "";
		}
		if (service instanceof ShardingService) {
			row.add(StringUtil.encode(((ShardingService) c.getService()).getSchema(), charset));
			generalTransformRow(c, executeSql, row, charset);
			row.add(StringUtil.encode(((ShardingService) c.getService()).getSession2().getSessionStage().toString(),
					charset));
		} else if (service instanceof RWSplitService) {
			row.add(StringUtil.encode(((RWSplitService) c.getService()).getSchema(), charset));
			generalTransformRow(c, executeSql, row, charset);
			//temporary process
			if (((RWSplitService) service).getUserConfig() instanceof AnalysisUserConfig) {
				row.add(StringUtil.encode("Analysis connection", charset));
			} else {
				row.add(StringUtil.encode("RWSplit connection", charset));
			}
		} else {
			row.add(StringUtil.encode("", charset));
			generalTransformRow(c, executeSql, row, charset);
			row.add(StringUtil.encode("Manager connection", charset));
		}
		return row;
	}

	private static void generalTransformRow(FrontendConnection c, String executeSql, RowDataPacket row,
			String charset) {
		row.add(StringUtil.encode(FormatUtil.formatDate(c.getLastReadTime()), charset));
		long rt = c.getLastReadTime();
		long wt = c.getLastWriteTime();
		row.add(LongUtil.toBytes((wt >= rt) ? (wt - rt) : (TimeUtil.currentTimeMillis() - rt)));
		row.add(StringUtil.encode(executeSql, charset));
	}

}
