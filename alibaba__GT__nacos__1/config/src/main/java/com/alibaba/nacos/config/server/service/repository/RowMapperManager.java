/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.config.server.service.repository;

import com.alibaba.nacos.config.server.model.ConfigAdvanceInfo;
import com.alibaba.nacos.config.server.model.ConfigAllInfo;
import com.alibaba.nacos.config.server.model.ConfigHistoryInfo;
import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.model.ConfigInfo4Beta;
import com.alibaba.nacos.config.server.model.ConfigInfo4Tag;
import com.alibaba.nacos.config.server.model.ConfigInfoAggr;
import com.alibaba.nacos.config.server.model.ConfigInfoBase;
import com.alibaba.nacos.config.server.model.ConfigInfoBetaWrapper;
import com.alibaba.nacos.config.server.model.ConfigInfoChanged;
import com.alibaba.nacos.config.server.model.ConfigInfoTagWrapper;
import com.alibaba.nacos.config.server.model.ConfigInfoWrapper;
import com.alibaba.nacos.config.server.model.ConfigKey;
import com.alibaba.nacos.config.server.model.TenantInfo;
import com.alibaba.nacos.config.server.utils.LogUtil;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manager RowMapper {@link RowMapper} for database object mapping.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class RowMapperManager {

	public static final RowMapper<TenantInfo> TENANT_INFO_ROW_MAPPER = new TenantInfoRowMapper();

	public static final RowMapper<ConfigInfoWrapper> CONFIG_INFO_WRAPPER_ROW_MAPPER = new ConfigInfoWrapperRowMapper();

	public static final RowMapper<ConfigKey> CONFIG_KEY_ROW_MAPPER = new ConfigKeyRowMapper();

	public static final ConfigInfoBetaWrapperRowMapper CONFIG_INFO_BETA_WRAPPER_ROW_MAPPER = new ConfigInfoBetaWrapperRowMapper();

	public static final ConfigInfoTagWrapperRowMapper CONFIG_INFO_TAG_WRAPPER_ROW_MAPPER = new ConfigInfoTagWrapperRowMapper();

	public static final ConfigInfoRowMapper CONFIG_INFO_ROW_MAPPER = new ConfigInfoRowMapper();

	public static final ConfigAdvanceInfoRowMapper CONFIG_ADVANCE_INFO_ROW_MAPPER = new ConfigAdvanceInfoRowMapper();

	public static final ConfigAllInfoRowMapper CONFIG_ALL_INFO_ROW_MAPPER = new ConfigAllInfoRowMapper();

	public static final ConfigInfo4BetaRowMapper CONFIG_INFO4BETA_ROW_MAPPER = new ConfigInfo4BetaRowMapper();

	public static final ConfigInfo4TagRowMapper CONFIG_INFO4TAG_ROW_MAPPER = new ConfigInfo4TagRowMapper();

	public static final ConfigInfoBaseRowMapper CONFIG_INFO_BASE_ROW_MAPPER = new ConfigInfoBaseRowMapper();

	public static final ConfigInfoAggrRowMapper CONFIG_INFO_AGGR_ROW_MAPPER = new ConfigInfoAggrRowMapper();

	public static final ConfigInfoChangedRowMapper CONFIG_INFO_CHANGED_ROW_MAPPER = new ConfigInfoChangedRowMapper();

	public static final ConfigHistoryRowMapper HISTORY_LIST_ROW_MAPPER = new ConfigHistoryRowMapper();

	public static final ConfigHistoryDetailRowMapper HISTORY_DETAIL_ROW_MAPPER = new ConfigHistoryDetailRowMapper();

	public static final MapRowMapper MAP_ROW_MAPPER = new MapRowMapper();

	public static Map<String, RowMapper> mapperMap = new HashMap<>(16);

	static {

		// TENANT_INFO_ROW_MAPPER

		mapperMap.put(TENANT_INFO_ROW_MAPPER.getClass().getCanonicalName(), TENANT_INFO_ROW_MAPPER);

		// CONFIG_INFO_WRAPPER_ROW_MAPPER

		mapperMap.put(CONFIG_INFO_WRAPPER_ROW_MAPPER.getClass().getCanonicalName(), CONFIG_INFO_WRAPPER_ROW_MAPPER);

		// CONFIG_KEY_ROW_MAPPER

		mapperMap.put(CONFIG_KEY_ROW_MAPPER.getClass().getCanonicalName(), CONFIG_KEY_ROW_MAPPER);

		// CONFIG_INFO_BETA_WRAPPER_ROW_MAPPER

		mapperMap.put(CONFIG_INFO_BETA_WRAPPER_ROW_MAPPER.getClass().getCanonicalName(),
				CONFIG_INFO_BETA_WRAPPER_ROW_MAPPER);

		// CONFIG_INFO_TAG_WRAPPER_ROW_MAPPER

		mapperMap.put(CONFIG_INFO_TAG_WRAPPER_ROW_MAPPER.getClass().getCanonicalName(),
				CONFIG_INFO_TAG_WRAPPER_ROW_MAPPER);

		// CONFIG_INFO_ROW_MAPPER

		mapperMap.put(CONFIG_INFO_ROW_MAPPER.getClass().getCanonicalName(), CONFIG_INFO_ROW_MAPPER);

		// CONFIG_ADVANCE_INFO_ROW_MAPPER

		mapperMap.put(CONFIG_ADVANCE_INFO_ROW_MAPPER.getClass().getCanonicalName(), CONFIG_ADVANCE_INFO_ROW_MAPPER);

		// CONFIG_ALL_INFO_ROW_MAPPER

		mapperMap.put(CONFIG_ALL_INFO_ROW_MAPPER.getClass().getCanonicalName(), CONFIG_ALL_INFO_ROW_MAPPER);

		// CONFIG_INFO4BETA_ROW_MAPPER

		mapperMap.put(CONFIG_INFO4BETA_ROW_MAPPER.getClass().getCanonicalName(), CONFIG_INFO4BETA_ROW_MAPPER);

		// CONFIG_INFO4TAG_ROW_MAPPER

		mapperMap.put(CONFIG_INFO4TAG_ROW_MAPPER.getClass().getCanonicalName(), CONFIG_INFO4TAG_ROW_MAPPER);

		// CONFIG_INFO_BASE_ROW_MAPPER

		mapperMap.put(CONFIG_INFO_BASE_ROW_MAPPER.getClass().getCanonicalName(), CONFIG_INFO_BASE_ROW_MAPPER);

		// CONFIG_INFO_AGGR_ROW_MAPPER

		mapperMap.put(CONFIG_INFO_AGGR_ROW_MAPPER.getClass().getCanonicalName(), CONFIG_INFO_AGGR_ROW_MAPPER);

		// CONFIG_INFO_CHANGED_ROW_MAPPER

		mapperMap.put(CONFIG_INFO_CHANGED_ROW_MAPPER.getClass().getCanonicalName(), CONFIG_INFO_CHANGED_ROW_MAPPER);

		// HISTORY_LIST_ROW_MAPPER

		mapperMap.put(HISTORY_LIST_ROW_MAPPER.getClass().getCanonicalName(), HISTORY_LIST_ROW_MAPPER);

		// HISTORY_DETAIL_ROW_MAPPER

		mapperMap.put(HISTORY_DETAIL_ROW_MAPPER.getClass().getCanonicalName(), HISTORY_DETAIL_ROW_MAPPER);

		// MAP_ROW_MAPPER

		mapperMap.put(MAP_ROW_MAPPER.getClass().getCanonicalName(), MAP_ROW_MAPPER);
	}

	public static <D> RowMapper<D> getRowMapper(String classFullName) {
		return (RowMapper<D>) mapperMap.get(classFullName);
	}

	/**
	 * Register custom row mapper to manager.
	 *
	 * @param classFullName full class name of row mapper handled.
	 * @param rowMapper     row mapper
	 * @param <D>           class of row mapper handled
	 */
	public static synchronized <D> void registerRowMapper(String classFullName, RowMapper<D> rowMapper) {
		if (mapperMap.containsKey(classFullName)) {
			LogUtil.DEFAULT_LOG.warn("row mapper {} conflicts, {} will be replaced by {}", classFullName,
					mapperMap.get(classFullName).getClass().getCanonicalName(),
					rowMapper.getClass().getCanonicalName());
		}
		mapperMap.put(classFullName, rowMapper);
	}

	public static final class MapRowMapper implements RowMapper<Map<String, Object>> {

		@Override
		public Map<String, Object> mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			Map<String, Object> map = new LinkedHashMap<>(columnCount);
			for (int i = 1; i <= columnCount; i++) {
				map.put(metaData.getColumnLabel(i), resultSet.getObject(i));
			}
			return map;
		}
	}

	public static final class ConfigInfoWrapperRowMapper implements RowMapper<ConfigInfoWrapper> {

		@Override
		public ConfigInfoWrapper mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigInfoWrapper info = new ConfigInfoWrapper();

			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));
			info.setTenant(rs.getString("tenant_id"));
			info.setAppName(rs.getString("app_name"));
			info.setType(rs.getString("type"));

			try {
				info.setContent(rs.getString("content"));
			} catch (SQLException ignore) {
			}
			try {
				info.setId(rs.getLong("id"));
			} catch (SQLException ignore) {
			}
			try {
				info.setLastModified(rs.getTimestamp("gmt_modified").getTime());
			} catch (SQLException ignore) {
			}
			try {
				info.setMd5(rs.getString("md5"));
			} catch (SQLException ignore) {
			}
			try {
				info.setEncryptedDataKey(rs.getString("encrypted_data_key"));
			} catch (SQLException ignore) {

			}
			return info;
		}
	}

	public static final class ConfigInfoBetaWrapperRowMapper implements RowMapper<ConfigInfoBetaWrapper> {

		@Override
		public ConfigInfoBetaWrapper mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigInfoBetaWrapper info = new ConfigInfoBetaWrapper();

			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));
			info.setTenant(rs.getString("tenant_id"));
			info.setAppName(rs.getString("app_name"));
			info.setBetaIps(rs.getString("beta_ips"));

			try {
				info.setContent(rs.getString("content"));
			} catch (SQLException ignore) {
			}
			try {
				info.setId(rs.getLong("id"));
			} catch (SQLException ignore) {
			}
			try {
				info.setLastModified(rs.getTimestamp("gmt_modified").getTime());
			} catch (SQLException ignore) {
			}
			try {
				info.setMd5(rs.getString("md5"));
			} catch (SQLException ignore) {
			}
			try {
				info.setEncryptedDataKey(rs.getString("encrypted_data_key"));
			} catch (SQLException ignore) {
			}
			return info;
		}
	}

	public static final class ConfigInfoTagWrapperRowMapper implements RowMapper<ConfigInfoTagWrapper> {

		@Override
		public ConfigInfoTagWrapper mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigInfoTagWrapper info = new ConfigInfoTagWrapper();

			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));
			info.setTenant(rs.getString("tenant_id"));
			info.setTag(rs.getString("tag_id"));
			info.setAppName(rs.getString("app_name"));

			try {
				info.setContent(rs.getString("content"));
			} catch (SQLException ignore) {
			}
			try {
				info.setId(rs.getLong("id"));
			} catch (SQLException ignore) {
			}
			try {
				info.setLastModified(rs.getTimestamp("gmt_modified").getTime());
			} catch (SQLException ignore) {
			}
			try {
				info.setMd5(rs.getString("md5"));
			} catch (SQLException ignore) {
			}
			return info;
		}
	}

	public static final class ConfigInfoRowMapper implements RowMapper<ConfigInfo> {

		@Override
		public ConfigInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigInfo info = new ConfigInfo();

			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));
			info.setTenant(rs.getString("tenant_id"));
			info.setAppName(rs.getString("app_name"));

			try {
				info.setContent(rs.getString("content"));
			} catch (SQLException ignore) {
			}
			try {
				info.setMd5(rs.getString("md5"));
			} catch (SQLException ignore) {
			}
			try {
				info.setId(rs.getLong("id"));
			} catch (SQLException ignore) {
			}
			try {
				info.setType(rs.getString("type"));
			} catch (SQLException ignore) {
			}
			try {
				info.setEncryptedDataKey(rs.getString("encrypted_data_key"));
			} catch (SQLException ignore) {
			}
			return info;
		}
	}

	public static final class ConfigKeyRowMapper implements RowMapper<ConfigKey> {

		@Override
		public ConfigKey mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigKey info = new ConfigKey();

			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));
			info.setAppName(rs.getString("app_name"));

			return info;
		}
	}

	public static final class ConfigAdvanceInfoRowMapper implements RowMapper<ConfigAdvanceInfo> {

		@Override
		public ConfigAdvanceInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigAdvanceInfo info = new ConfigAdvanceInfo();
			info.setCreateTime(rs.getTimestamp("gmt_modified").getTime());
			info.setModifyTime(rs.getTimestamp("gmt_modified").getTime());
			info.setCreateUser(rs.getString("src_user"));
			info.setCreateIp(rs.getString("src_ip"));
			info.setDesc(rs.getString("c_desc"));
			info.setUse(rs.getString("c_use"));
			info.setEffect(rs.getString("effect"));
			info.setType(rs.getString("type"));
			info.setSchema(rs.getString("c_schema"));
			return info;
		}
	}

	public static final class ConfigAllInfoRowMapper implements RowMapper<ConfigAllInfo> {

		@Override
		public ConfigAllInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigAllInfo info = new ConfigAllInfo();
			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));
			info.setTenant(rs.getString("tenant_id"));
			info.setAppName(rs.getString("app_name"));
			try {
				info.setContent(rs.getString("content"));
			} catch (SQLException ignore) {
			}
			try {
				info.setMd5(rs.getString("md5"));
			} catch (SQLException ignore) {
			}
			try {
				info.setId(rs.getLong("id"));
			} catch (SQLException ignore) {
			}
			info.setCreateTime(rs.getTimestamp("gmt_modified").getTime());
			info.setModifyTime(rs.getTimestamp("gmt_modified").getTime());
			info.setCreateUser(rs.getString("src_user"));
			info.setCreateIp(rs.getString("src_ip"));
			info.setDesc(rs.getString("c_desc"));
			info.setUse(rs.getString("c_use"));
			info.setEffect(rs.getString("effect"));
			info.setType(rs.getString("type"));
			info.setSchema(rs.getString("c_schema"));
			try {
				info.setEncryptedDataKey(rs.getString("encrypted_data_key"));
			} catch (SQLException ignore) {

			}
			return info;
		}
	}

	public static final class ConfigInfo4BetaRowMapper implements RowMapper<ConfigInfo4Beta> {

		@Override
		public ConfigInfo4Beta mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigInfo4Beta info = new ConfigInfo4Beta();

			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));
			info.setTenant(rs.getString("tenant_id"));
			info.setAppName(rs.getString("app_name"));
			info.setBetaIps(rs.getString("beta_ips"));

			try {
				info.setContent(rs.getString("content"));
			} catch (SQLException ignore) {
			}
			try {
				info.setId(rs.getLong("id"));
			} catch (SQLException ignore) {
			}
			try {
				info.setMd5(rs.getString("md5"));
			} catch (SQLException ignore) {
			}
			return info;
		}
	}

	public static final class ConfigInfo4TagRowMapper implements RowMapper<ConfigInfo4Tag> {

		@Override
		public ConfigInfo4Tag mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigInfo4Tag info = new ConfigInfo4Tag();

			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));
			info.setTenant(rs.getString("tenant_id"));
			info.setTag(rs.getString("tag_id"));
			info.setAppName(rs.getString("app_name"));

			try {
				info.setContent(rs.getString("content"));
			} catch (SQLException ignore) {
			}
			try {
				info.setId(rs.getLong("id"));
			} catch (SQLException ignore) {
			}
			try {
				info.setMd5(rs.getString("md5"));
			} catch (SQLException ignore) {
			}
			return info;
		}
	}

	public static final class ConfigInfoBaseRowMapper implements RowMapper<ConfigInfoBase> {

		@Override
		public ConfigInfoBase mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigInfoBase info = new ConfigInfoBase();

			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));

			try {
				info.setContent(rs.getString("content"));
			} catch (SQLException ignore) {
			}
			try {
				info.setId(rs.getLong("id"));
			} catch (SQLException ignore) {
			}
			return info;
		}
	}

	public static final class ConfigInfoAggrRowMapper implements RowMapper<ConfigInfoAggr> {

		@Override
		public ConfigInfoAggr mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigInfoAggr info = new ConfigInfoAggr();
			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));
			info.setDatumId(rs.getString("datum_id"));
			info.setTenant(rs.getString("tenant_id"));
			info.setAppName(rs.getString("app_name"));
			info.setContent(rs.getString("content"));
			return info;
		}
	}

	public static final class ConfigInfoChangedRowMapper implements RowMapper<ConfigInfoChanged> {

		@Override
		public ConfigInfoChanged mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigInfoChanged info = new ConfigInfoChanged();
			info.setDataId(rs.getString("data_id"));
			info.setGroup(rs.getString("group_id"));
			info.setTenant(rs.getString("tenant_id"));
			return info;
		}
	}

	public static final class ConfigHistoryRowMapper implements RowMapper<ConfigHistoryInfo> {

		@Override
		public ConfigHistoryInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigHistoryInfo configHistoryInfo = new ConfigHistoryInfo();
			configHistoryInfo.setId(rs.getLong("nid"));
			configHistoryInfo.setDataId(rs.getString("data_id"));
			configHistoryInfo.setGroup(rs.getString("group_id"));
			configHistoryInfo.setTenant(rs.getString("tenant_id"));
			configHistoryInfo.setAppName(rs.getString("app_name"));
			configHistoryInfo.setSrcIp(rs.getString("src_ip"));
			configHistoryInfo.setSrcUser(rs.getString("src_user"));
			configHistoryInfo.setOpType(rs.getString("op_type"));
			configHistoryInfo.setCreatedTime(rs.getTimestamp("gmt_create"));
			configHistoryInfo.setLastModifiedTime(rs.getTimestamp("gmt_modified"));
			return configHistoryInfo;
		}
	}

	public static final class ConfigHistoryDetailRowMapper implements RowMapper<ConfigHistoryInfo> {

		@Override
		public ConfigHistoryInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
			ConfigHistoryInfo configHistoryInfo = new ConfigHistoryInfo();
			configHistoryInfo.setId(rs.getLong("nid"));
			configHistoryInfo.setDataId(rs.getString("data_id"));
			configHistoryInfo.setGroup(rs.getString("group_id"));
			configHistoryInfo.setTenant(rs.getString("tenant_id"));
			configHistoryInfo.setAppName(rs.getString("app_name"));
			configHistoryInfo.setMd5(rs.getString("md5"));
			configHistoryInfo.setContent(rs.getString("content"));
			configHistoryInfo.setSrcUser(rs.getString("src_user"));
			configHistoryInfo.setSrcIp(rs.getString("src_ip"));
			configHistoryInfo.setOpType(rs.getString("op_type"));
			configHistoryInfo.setCreatedTime(rs.getTimestamp("gmt_create"));
			configHistoryInfo.setLastModifiedTime(rs.getTimestamp("gmt_modified"));
			try {
				configHistoryInfo.setEncryptedDataKey(rs.getString("encrypted_data_key"));
			} catch (SQLException ignore) {

			}
			return configHistoryInfo;
		}
	}

	public static final class TenantInfoRowMapper implements RowMapper<TenantInfo> {

		@Override
		public TenantInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
			TenantInfo info = new TenantInfo();
			info.setTenantId(rs.getString("tenant_id"));
			info.setTenantName(rs.getString("tenant_name"));
			info.setTenantDesc(rs.getString("tenant_desc"));
			return info;
		}
	}

}
