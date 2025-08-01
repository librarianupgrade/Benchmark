/*
 * Copyright (c) 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.common.truth.extension;

import com.google.common.collect.ImmutableSet;
import com.google.common.truth.extension.Employee.Location;

/**
 * Service for accessing human resources data.
 *
 * <p>This class (and all the classes in this package) are just a demonstration of how to write and
 * use a custom Truth subject. The only implementation of {@code HrDatabase} is {@link
 * FakeHrDatabase}, and the only place we use it is in its own test, whose real purpose is to
 * demonstrate how to use a custom Truth subject.
 */
public interface HrDatabase {
	Employee get(long id);

	void relocate(long id, Location location);

	ImmutableSet<Employee> getByLocation(Location location);
}
