/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.foundation.metrics.publish;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.distribution.CountAtBucket;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;

// like select * from meters group by ......
// but output a tree not a table
public class MeasurementTree extends MeasurementNode {
	public MeasurementTree() {
		super(null, null, null);
	}

	// groupConfig:
	//   key: id name
	//   value: id tag keys
	// only id name exists in groupConfig will accept, others will be ignored
	public void from(Iterator<Meter> meters, MeasurementGroupConfig groupConfig) {
		meters.forEachRemaining(meter -> {
			// This code snip is not very good design. But DistributionSummary is quite special.
			if (meter instanceof DistributionSummary distributionSummary) {
				HistogramSnapshot snapshot = distributionSummary.takeSnapshot();
				CountAtBucket[] countAtBuckets = snapshot.histogramCounts();
				List<Measurement> distributions = new ArrayList<>(countAtBuckets.length);
				for (CountAtBucket countAtBucket : countAtBuckets) {
					final double value = countAtBucket.count();
					distributions.add(new Measurement(() -> value, Statistic.COUNT));
				}

				from(meter.getId(), distributions, groupConfig);
				return;
			}

			Iterable<Measurement> measurements = meter.measure();
			from(meter.getId(), measurements, groupConfig);
		});
	}

	public void from(Id id, Iterable<Measurement> measurements, MeasurementGroupConfig groupConfig) {
		for (Measurement measurement : measurements) {
			MeasurementNode node = addChild(id.getName(), id, measurement);

			List<TagFinder> tagFinders = groupConfig.findTagFinders(id.getName());
			if (tagFinders == null) {
				continue;
			}

			for (TagFinder tagFinder : tagFinders) {
				Tag tag = tagFinder.find(id.getTags());
				if (tag == null) {
					if (tagFinder.skipOnNull()) {
						break;
					}
					throw new IllegalStateException(
							String.format("tag key \"%s\" not exist in %s", tagFinder.getTagKey(), id));
				}

				node = node.addChild(tag.getValue(), id, measurement);
			}

			node.addChild(measurement.getStatistic().name(), id, measurement);
		}
	}
}
