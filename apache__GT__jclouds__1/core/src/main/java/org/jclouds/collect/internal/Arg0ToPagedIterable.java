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
package org.jclouds.collect.internal;

import java.util.List;

import org.jclouds.collect.IterableWithMarker;
import org.jclouds.rest.internal.GeneratedHttpRequest;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;

/**
 * Used to propagate {@code arg0} during an advance in a {@link PagedIterable}. For example, in the call
 * {@code api.getUserApi().listPathPrefix("/users")}, the arg0 is the value {@code "/users"}.
 */
@Beta
public abstract class Arg0ToPagedIterable<T, I extends Arg0ToPagedIterable<T, I>> extends ArgsToPagedIterable<T, I> {

	@Override
	protected Function<Object, IterableWithMarker<T>> markerToNextForArgs(List<Object> args) {
		Optional<Object> arg0 = Optional.fromNullable(!args.isEmpty() ? args.get(0) : null);
		return markerToNextForArg0(arg0);
	}

	/**
	* @param arg0
	*           present when there was an arg0
	*/
	protected abstract Function<Object, IterableWithMarker<T>> markerToNextForArg0(Optional<Object> arg0);

	/**
	* Used to propagate caller {@code arg0} to a callee during an advance in a {@link PagedIterable}. For example, in
	* the call {@code api.getUserApi(region).list()}, the caller arg0 is the value of {@code region}, and the callee
	* is {@code UserApi.list()}
	*
	*/
	public abstract static class FromCaller<T, I extends FromCaller<T, I>> extends Arg0ToPagedIterable<T, I> {
		@Override
		protected List<Object> getArgs(GeneratedHttpRequest request) {
			return request.getCaller().get().getArgs();
		}
	}
}
