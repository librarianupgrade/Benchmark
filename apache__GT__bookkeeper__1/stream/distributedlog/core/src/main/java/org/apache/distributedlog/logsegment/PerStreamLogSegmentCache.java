/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.distributedlog.logsegment;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.distributedlog.DistributedLogConstants;
import org.apache.distributedlog.LogSegmentMetadata;
import org.apache.distributedlog.exceptions.UnexpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managing log segments in local cache.
 *
 * <p>
 * Caching of log segment metadata assumes that the data contained in the ZNodes for individual
 * log segments is never updated after creation i.e we never call setData. A log segment
 * is finalized by creating a new ZNode and deleting the in progress node. This code will have
 * to change if we change the behavior
 * </p>
 */
public class PerStreamLogSegmentCache {

	static final Logger LOG = LoggerFactory.getLogger(PerStreamLogSegmentCache.class);

	protected final String streamName;
	protected final boolean validateLogSegmentSequenceNumber;
	protected final Map<String, LogSegmentMetadata> logSegments = new HashMap<String, LogSegmentMetadata>();
	protected final ConcurrentMap<Long, LogSegmentMetadata> lid2LogSegments = new ConcurrentHashMap<Long, LogSegmentMetadata>();

	@VisibleForTesting
	PerStreamLogSegmentCache(String streamName) {
		this(streamName, true);
	}

	public PerStreamLogSegmentCache(String streamName, boolean validateLogSegmentSequenceNumber) {
		this.streamName = streamName;
		this.validateLogSegmentSequenceNumber = validateLogSegmentSequenceNumber;
	}

	/**
	 * Retrieve log segments from the cache.
	 *- first sort the log segments in ascending order
	 * - do validation and assign corresponding sequence id
	 * - apply comparator after validation
	 *
	 * @param comparator
	 *          comparator to sort the returned log segments.
	 * @return list of sorted and filtered log segments.
	 * @throws UnexpectedException if unexpected condition detected (e.g. ledger sequence number gap)
	 */
	public List<LogSegmentMetadata> getLogSegments(Comparator<LogSegmentMetadata> comparator)
			throws UnexpectedException {
		List<LogSegmentMetadata> segmentsToReturn;
		synchronized (logSegments) {
			segmentsToReturn = new ArrayList<LogSegmentMetadata>(logSegments.size());
			segmentsToReturn.addAll(logSegments.values());
		}
		Collections.sort(segmentsToReturn, LogSegmentMetadata.COMPARATOR);

		LogSegmentMetadata prevSegment = null;
		if (validateLogSegmentSequenceNumber) {
			// validation ledger sequence number to ensure the log segments are unique.
			for (int i = 0; i < segmentsToReturn.size(); i++) {
				LogSegmentMetadata segment = segmentsToReturn.get(i);

				if (null != prevSegment && prevSegment
						.getVersion() >= LogSegmentMetadata.LogSegmentMetadataVersion.VERSION_V2_LEDGER_SEQNO.value
						&& segment
								.getVersion() >= LogSegmentMetadata.LogSegmentMetadataVersion.VERSION_V2_LEDGER_SEQNO.value
						&& prevSegment.getLogSegmentSequenceNumber() + 1 != segment.getLogSegmentSequenceNumber()) {
					LOG.error("{} found ledger sequence number gap between log segment {} and {}", streamName,
							prevSegment, segment);
					throw new UnexpectedException(streamName + " found ledger sequence number gap between log segment "
							+ prevSegment.getLogSegmentSequenceNumber() + " and "
							+ segment.getLogSegmentSequenceNumber());
				}
				prevSegment = segment;
			}
		}

		prevSegment = null;
		long startSequenceId = DistributedLogConstants.UNASSIGNED_SEQUENCE_ID;
		for (int i = 0; i < segmentsToReturn.size(); i++) {
			LogSegmentMetadata segment = segmentsToReturn.get(i);
			// assign sequence id
			if (!segment.isInProgress()) {
				if (segment.supportsSequenceId()) {
					startSequenceId = segment.getStartSequenceId() + segment.getRecordCount();
					if (null != prevSegment && prevSegment.supportsSequenceId()
							&& prevSegment.getStartSequenceId() > segment.getStartSequenceId()) {
						LOG.warn("{} found decreasing start sequence id in log segment {}, previous is {}", streamName,
								segment, prevSegment);
					}
				} else {
					startSequenceId = DistributedLogConstants.UNASSIGNED_SEQUENCE_ID;
				}
			} else {
				if (segment.supportsSequenceId()) {
					LogSegmentMetadata newSegment = segment.mutator().setStartSequenceId(
							startSequenceId == DistributedLogConstants.UNASSIGNED_SEQUENCE_ID ? 0L : startSequenceId)
							.build();
					segmentsToReturn.set(i, newSegment);
				}

				break;
			}
			prevSegment = segment;
		}
		if (comparator != LogSegmentMetadata.COMPARATOR) {
			Collections.sort(segmentsToReturn, comparator);
		}
		return segmentsToReturn;
	}

	/**
	 * Add the segment <i>metadata</i> for <i>name</i> in the cache.
	 *
	 * @param name
	 *          segment name.
	 * @param metadata
	 *          segment metadata.
	 */
	public void add(String name, LogSegmentMetadata metadata) {
		synchronized (logSegments) {
			if (!logSegments.containsKey(name)) {
				logSegments.put(name, metadata);
				LOG.info("{} added log segment ({} : {}) to cache.", streamName, name, metadata);
			}
			LogSegmentMetadata oldMetadata = lid2LogSegments.remove(metadata.getLogSegmentId());
			if (null == oldMetadata) {
				lid2LogSegments.put(metadata.getLogSegmentId(), metadata);
			} else {
				if (oldMetadata.isInProgress() && !metadata.isInProgress()) {
					lid2LogSegments.put(metadata.getLogSegmentId(), metadata);
				} else {
					lid2LogSegments.put(oldMetadata.getLogSegmentId(), oldMetadata);
				}
			}
		}
	}

	/**
	 * Retrieve log segment <code>name</code> from the cache.
	 *
	 * @param name
	 *          name of the log segment.
	 * @return log segment metadata
	 */
	public LogSegmentMetadata get(String name) {
		synchronized (logSegments) {
			return logSegments.get(name);
		}
	}

	/**
	 * Update the log segment cache with removed/added segments.
	 *
	 * @param segmentsRemoved
	 *          segments that removed
	 * @param segmentsAdded
	 *          segments that added
	 */
	public void update(Set<String> segmentsRemoved, Map<String, LogSegmentMetadata> segmentsAdded) {
		synchronized (logSegments) {
			for (Map.Entry<String, LogSegmentMetadata> entry : segmentsAdded.entrySet()) {
				add(entry.getKey(), entry.getValue());
			}
			for (String segment : segmentsRemoved) {
				remove(segment);
			}
		}
	}

	/**
	 * Diff with new received segment list <code>segmentReceived</code>.
	 *
	 * @param segmentsReceived
	 *          new received segment list
	 * @return segments added (left) and removed (right).
	 */
	public Pair<Set<String>, Set<String>> diff(Set<String> segmentsReceived) {
		Set<String> segmentsAdded;
		Set<String> segmentsRemoved;
		synchronized (logSegments) {
			Set<String> segmentsCached = logSegments.keySet();
			segmentsAdded = Sets.difference(segmentsReceived, segmentsCached).immutableCopy();
			segmentsRemoved = Sets.difference(segmentsCached, segmentsReceived).immutableCopy();
		}
		return Pair.of(segmentsAdded, segmentsRemoved);
	}

	/**
	 * Remove log segment <code>name</code> from the cache.
	 *
	 * @param name
	 *          name of the log segment.
	 * @return log segment metadata.
	 */
	public LogSegmentMetadata remove(String name) {
		synchronized (logSegments) {
			LogSegmentMetadata metadata = logSegments.remove(name);
			if (null != metadata) {
				lid2LogSegments.remove(metadata.getLogSegmentId(), metadata);
				LOG.debug("Removed log segment ({} : {}) from cache.", name, metadata);
			}
			return metadata;
		}
	}

}
