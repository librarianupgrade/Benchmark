/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bookkeeper.stream.storage.impl.metadata;

import java.util.concurrent.CompletableFuture;
import org.apache.bookkeeper.stream.proto.storage.DeleteNamespaceRequest;
import org.apache.bookkeeper.stream.proto.storage.DeleteNamespaceResponse;
import org.apache.bookkeeper.stream.proto.storage.StatusCode;
import org.apache.bookkeeper.stream.storage.impl.AsyncOperationProcessor;

/**
 * The operation process for creating namespace.
 */
class DeleteNamespaceProcessor
		extends AsyncOperationProcessor<DeleteNamespaceRequest, DeleteNamespaceResponse, RootRangeStoreImpl> {

	public static DeleteNamespaceProcessor of() {
		return INSTANCE;
	}

	private static final DeleteNamespaceProcessor INSTANCE = new DeleteNamespaceProcessor();

	private DeleteNamespaceProcessor() {
	}

	@Override
	protected StatusCode verifyRequest(RootRangeStoreImpl state, DeleteNamespaceRequest request) {
		return state.verifyDeleteNamespaceRequest(request);
	}

	@Override
	protected DeleteNamespaceResponse failRequest(StatusCode code) {
		return DeleteNamespaceResponse.newBuilder().setCode(code).build();
	}

	@Override
	protected CompletableFuture<DeleteNamespaceResponse> doProcessRequest(RootRangeStoreImpl state,
			DeleteNamespaceRequest request) {
		return state.doProcessDeleteNamespaceRequest(request);
	}
}
