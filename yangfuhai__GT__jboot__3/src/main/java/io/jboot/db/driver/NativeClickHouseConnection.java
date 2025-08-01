/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.db.driver;

import com.github.housepower.jdbc.ClickHouseConnection;
import com.github.housepower.jdbc.connect.NativeClient;
import com.github.housepower.jdbc.connect.NativeContext;
import com.github.housepower.jdbc.misc.Validate;
import com.github.housepower.jdbc.protocol.HelloResponse;
import com.github.housepower.jdbc.protocol.QueryRequest;
import com.github.housepower.jdbc.settings.ClickHouseConfig;
import com.github.housepower.jdbc.settings.ClickHouseDefines;

import java.net.InetSocketAddress;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Locale;

public class NativeClickHouseConnection extends ClickHouseConnection {

	protected NativeClickHouseConnection(ClickHouseConfig cfg, NativeContext nativeCtx) {
		super(cfg, nativeCtx);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		//      clickhouse 不支持 autoGeneratedKeys，但是 jfinal 调用了此方法会出错
		return prepareStatement(sql);
	}

	public static ClickHouseConnection createClickHouseConnection(ClickHouseConfig configure) throws SQLException {
		return new NativeClickHouseConnection(configure, createNativeContext(configure));
	}

	private static NativeContext createNativeContext(ClickHouseConfig configure) throws SQLException {
		NativeClient nativeClient = NativeClient.connect(configure);
		return new NativeContext(clientContext(nativeClient, configure), serverContext(nativeClient, configure),
				nativeClient);
	}

	private static QueryRequest.ClientContext clientContext(NativeClient nativeClient, ClickHouseConfig configure)
			throws SQLException {
		Validate.isTrue(nativeClient.address() instanceof InetSocketAddress);
		InetSocketAddress address = (InetSocketAddress) nativeClient.address();
		String clientName = String.format(Locale.ROOT, "%s %s", ClickHouseDefines.NAME, "client");
		String initialAddress = "[::ffff:127.0.0.1]:0";
		return new QueryRequest.ClientContext(initialAddress, address.getHostName(), clientName);
	}

	private static NativeContext.ServerContext serverContext(NativeClient nativeClient, ClickHouseConfig configure)
			throws SQLException {
		try {
			long revision = ClickHouseDefines.CLIENT_REVISION;
			nativeClient.sendHello("client", revision, configure.database(), configure.user(), configure.password());

			HelloResponse response = nativeClient.receiveHello(configure.queryTimeout(), null);
			ZoneId timeZone = ZoneId.of(response.serverTimeZone());
			return new NativeContext.ServerContext(response.majorVersion(), response.minorVersion(),
					response.reversion(), configure, timeZone, response.serverDisplayName());
		} catch (SQLException rethrows) {
			nativeClient.silentDisconnect();
			throw rethrows;
		}
	}

}
