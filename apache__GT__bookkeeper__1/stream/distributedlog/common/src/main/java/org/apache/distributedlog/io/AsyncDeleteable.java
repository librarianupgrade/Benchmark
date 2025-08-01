/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.distributedlog.io;

import java.util.concurrent.CompletableFuture;

/**
 * A {@code AsyncDeleteable} is a source or destination of data that can be deleted asynchronously.
 * This delete method is invoked to delete the source.
 */
public interface AsyncDeleteable {
	/**
	 * Releases any system resources associated with this and delete the source. If the source is
	 * already deleted then invoking this method has no effect.
	 *
	 * @return future representing the deletion result.
	 */
	CompletableFuture<Void> delete();
}
