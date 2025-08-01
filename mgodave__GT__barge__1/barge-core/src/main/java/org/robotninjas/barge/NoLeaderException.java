/**
 * Copyright 2013 David Rusek <dave dot rusek at gmail dot com>
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

package org.robotninjas.barge;

import javax.annotation.concurrent.Immutable;

@Immutable
public class NoLeaderException extends RaftException {

	public NoLeaderException() {
	}

	public NoLeaderException(String s) {
		super(s);
	}

	public NoLeaderException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public NoLeaderException(Throwable throwable) {
		super(throwable);
	}
}
