/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.bookkeeper.tools.perf;

import org.apache.bookkeeper.tools.common.BKFlags;
import org.apache.bookkeeper.tools.framework.CliCommandGroup;
import org.apache.bookkeeper.tools.framework.CliSpec;
import org.apache.bookkeeper.tools.perf.table.BenchmarkCommand;

/**
 * Commands that evaluate performance of table service.
 */
public class TablePerfCommandGroup extends CliCommandGroup<BKFlags> implements PerfCommandGroup<BKFlags> {

	private static final String NAME = "table";
	private static final String DESC = "Commands on evaluating performance of table service";

	private static final CliSpec<BKFlags> spec = CliSpec.<BKFlags>newBuilder().withName(NAME).withDescription(DESC)
			.withParent(BKPerf.NAME).addCommand(new BenchmarkCommand()).build();

	public TablePerfCommandGroup() {
		super(spec);
	}

}
