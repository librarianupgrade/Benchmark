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
package org.apache.bookkeeper.tools.cli.commands;

import static org.apache.bookkeeper.tools.common.BKCommandCategories.CATEGORY_INFRA_SERVICE;

import org.apache.bookkeeper.tools.cli.commands.health.SwitchOfHealthCheckCommand;
import org.apache.bookkeeper.tools.common.BKFlags;
import org.apache.bookkeeper.tools.framework.CliCommandGroup;
import org.apache.bookkeeper.tools.framework.CliSpec;

/**
 * Commands on some specific operation.
 */
public class HealthCheckCommandGroup extends CliCommandGroup<BKFlags> {

	private static final String NAME = "healthCheck";
	private static final String DESC = "Command on some specific operation.";

	private static final CliSpec<BKFlags> spec = CliSpec.<BKFlags>newBuilder().withName(NAME).withDescription(DESC)
			.withCategory(CATEGORY_INFRA_SERVICE).addCommand(new SwitchOfHealthCheckCommand()).build();

	public HealthCheckCommandGroup() {
		super(spec);
	}
}
