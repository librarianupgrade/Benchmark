/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.route.sequence.handler;

import com.actiontech.dble.DbleServer;
import com.actiontech.dble.cluster.values.RawJson;
import com.actiontech.dble.config.converter.SequenceConverter;
import com.actiontech.dble.config.util.ConfigException;
import com.actiontech.dble.route.util.PropertiesUtil;
import com.actiontech.dble.services.FrontendService;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLNonTransientException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.actiontech.dble.config.ConfigFileName.SEQUENCE_DB_FILE_NAME;

public class IncrSequenceMySQLHandler implements SequenceHandler {

	protected static final Logger LOGGER = LoggerFactory.getLogger(IncrSequenceMySQLHandler.class);
	protected static final String ERR_SEQ_RESULT = "-999999999,null";
	protected static final Map<String, String> LATEST_ERRORS = new ConcurrentHashMap<>();
	private final FetchMySQLSequenceHandler mysqlSeqFetcher = new FetchMySQLSequenceHandler();

	public void load(RawJson sequenceJson, Set<String> currentShardingNodes) {
		Properties props;
		if (sequenceJson != null) {
			// load cluster properties
			SequenceConverter sequenceConverter = new SequenceConverter();
			props = sequenceConverter.jsonToProperties(sequenceJson);
		} else {
			// load local properties
			props = PropertiesUtil.loadProps(SEQUENCE_DB_FILE_NAME);
		}

		loadCheck(props, currentShardingNodes);
		loadContext(props);
	}

	public void tryLoad(RawJson sequenceJson, Set<String> currentShardingNodes) {
		load(sequenceJson, currentShardingNodes);
	}

	private void loadCheck(Properties props, Set<String> currentShardingNodes) {
		Set<String> noExistShardingNodes = new HashSet<>();
		props.entrySet().stream().forEach(entry -> {
			String shardingNode = (String) entry.getValue();
			if (!currentShardingNodes.contains(shardingNode)) {
				noExistShardingNodes.add(shardingNode);
			}
		});
		if (!noExistShardingNodes.isEmpty()) {
			throw new ConfigException("the shardingNodes[" + Strings.join(noExistShardingNodes, ',') + "] of the "
					+ SEQUENCE_DB_FILE_NAME + " in sharding.xml does not exist");
		}
	}

	public void loadContext(Properties props) {
		seqValueMap.clear();
		props.entrySet().stream().forEach(entry -> {
			String seqName = (String) entry.getKey();
			String shardingNode = (String) entry.getValue();
			seqValueMap.putIfAbsent(seqName, new SequenceVal(seqName, shardingNode));
		});
	}

	public static Set<String> getShardingNodes(RawJson sequenceJson) {
		Set<String> shardingNodes = new HashSet<>();
		Properties propsTmp = (new SequenceConverter()).jsonToProperties(sequenceJson);
		propsTmp.entrySet().stream().forEach(entry -> {
			shardingNodes.add((String) entry.getValue());
		});
		return shardingNodes;
	}

	/**
	 * save sequence -> curval
	 */
	private ConcurrentHashMap<String, SequenceVal> seqValueMap = new ConcurrentHashMap<>();

	@Override
	public long nextId(String seqName, FrontendService frontendService) throws SQLNonTransientException {
		SequenceVal seqVal = matching(seqName);
		if (seqVal == null) {
			throw new ConfigException("can't find definition for sequence :" + seqName);
		}
		if (!seqVal.isSuccessFetched()) {
			return getSeqValueFromDB(seqVal);
		} else {
			return getNextValidSeqVal(seqVal);
		}

	}

	private SequenceVal matching(String key) {
		if (DbleServer.getInstance().getSystemVariables().isLowerCaseTableNames()) {
			Optional<Map.Entry<String, SequenceVal>> result = seqValueMap.entrySet().stream()
					.filter(m -> m.getKey().equalsIgnoreCase(key)).findFirst();
			if (result.isPresent()) {
				return result.get().getValue();
			} else {
				return null;
			}
		} else {
			return seqValueMap.get(key);
		}
	}

	private Long getNextValidSeqVal(SequenceVal seqVal) throws SQLNonTransientException {
		long nexVal = seqVal.counter.getNext();
		if (nexVal != -1) {
			return nexVal;
		} else {
			return getSeqValueFromDB(seqVal);
		}
	}

	private long getSeqValueFromDB(SequenceVal seqVal) throws SQLNonTransientException {
		if (seqVal.fetching.compareAndSet(false, true)) {
			//if get the lock ,connect to mysql and get next
			return this.execSeqFetcher(seqVal);
		} else {
			//other who does get the lock just wait for awhile
			return this.waitForResult(seqVal);
		}
	}

	/**
	 * get the next segment & get the value[0]
	 *
	 * @param seqVal
	 * @return
	 * @throws SQLNonTransientException
	 */
	private long execSeqFetcher(SequenceVal seqVal) throws SQLNonTransientException {
		try {
			seqVal.dbretVal = null;
			seqVal.dbfinished = false;
			mysqlSeqFetcher.execute(seqVal);
			Long[] values = seqVal.waitFinish();

			//check if the result is right
			if (values == null) {
				throw new RuntimeException("can't fetch sequence in db,sequence :" + seqVal.seqName + " detail:"
						+ mysqlSeqFetcher.getLastError(seqVal.seqName));
			} else if (values[0] == 0) {
				String msg = "sequence," + seqVal.seqName
						+ " has not been set, please check configure in dble_sequence";
				LOGGER.info(msg);
				throw new SQLNonTransientException(msg);
			} else {
				//if the result is OK just return the first value
				seqVal.setNewCounter(values[0], values[1]);
				return values[0];
			}
		} catch (Exception e) {
			throw e;
		} finally {
			seqVal.signalAll();
		}
	}

	/**
	 * waiting for the packet exec to finish and get the next value
	 *
	 * @param seqVal
	 * @return
	 * @throws SQLNonTransientException
	 */
	private long waitForResult(SequenceVal seqVal) throws SQLNonTransientException {
		seqVal.waitOtherFinish();
		return this.getNextValidSeqVal(seqVal);
	}

}
