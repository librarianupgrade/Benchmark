/*
   Copyright 2016 Immutables Authors and Contributors

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
package org.immutables.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import static org.immutables.check.Checkers.check;

public class AlternateNameTest {
	private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new GsonAdaptersAlternateNames())
			.registerTypeAdapterFactory(new GsonAdaptersAlternateNamesStrategy()).create();

	@Test
	public void alternateNames() {
		check(gson.fromJson("{\"url\":\"a\"}", AlternateNames.class).url()).is("a");
		check(gson.fromJson("{\"URL\":\"b\"}", AlternateNames.class).url()).is("b");
		check(gson.fromJson("{\"href\":\"c\"}", AlternateNames.class).url()).is("c");
	}

	@Test
	public void alternateNamesStrategy() {
		check(gson.fromJson("{\"url\":\"a\"}", AlternateNamesStrategy.class).url()).is("a");
		check(gson.fromJson("{\"URL\":\"b\"}", AlternateNamesStrategy.class).url()).is("b");
		check(gson.fromJson("{\"href\":\"c\"}", AlternateNamesStrategy.class).url()).is("c");
	}
}
