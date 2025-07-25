/*
   Copyright 2013-2018 Immutables Authors and Contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.immutables.fixture;

import io.atlassian.fugue.Option;
import com.google.common.base.Optional;
import java.util.List;
import org.immutables.fixture.subpack.SillySubstructure;
import org.immutables.gson.Gson;
import org.immutables.mongo.Mongo;
import org.immutables.value.Value;

@Value.Immutable
@Mongo.Repository
@Gson.TypeAdapters
public interface SillyStructureWithId {

	@Mongo.Id
	String id();

	String attr1();

	boolean flag2();

	Optional<Integer> opt3();

	Option<Integer> opt5();

	long very4();

	double wet5();

	List<SillySubstructure> subs6();

	SillySubstructure nest7();

	Optional<SillyTuplie> tup3();

	int int9();
}
