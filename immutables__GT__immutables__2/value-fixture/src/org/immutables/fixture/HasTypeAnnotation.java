/*
   Copyright 2014 Immutables Authors and Contributors

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

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Map;
import javax.annotation.Nullable;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@interface TypeA {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
@interface TypeB {
}

/**
 * Compilation and reflection test for preserving type annotations.
 */
@Value.Immutable
@Gson.TypeAdapters
public abstract class HasTypeAnnotation {
	@Nullable
	public abstract @TypeA @TypeB String str();

	@Nullable
	public abstract @TypeA @TypeB byte[] bte();

	@Nullable
	public abstract @TypeA @TypeB Map<@TypeA String, @TypeB String> map();
}
