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
package org.jclouds.location.suppliers.fromconfig;

import static org.jclouds.location.reference.LocationConstants.PROPERTY_ZONES;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.config.ValueOfConfigurationKeyOrNull;
import org.jclouds.location.Provider;
import org.jclouds.location.suppliers.ZoneIdsSupplier;

/**
 * 
 * looks for properties bound to the naming convention jclouds.zones
 */
@Singleton
public class ZoneIdsFromConfiguration extends SplitConfigurationKey implements ZoneIdsSupplier {

	@Inject
	protected ZoneIdsFromConfiguration(ValueOfConfigurationKeyOrNull config, @Provider String provider) {
		super(config, provider, PROPERTY_ZONES);
	}

}
