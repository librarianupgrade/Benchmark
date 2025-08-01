/*
 *
 *  Copyright 2016,2018 IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hyperledger.fabric.sdk.transaction;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;

public class QueryCollectionsConfigBuilder extends LSCCProposalBuilder {
	List<ByteString> argList = new ArrayList<>();

	private QueryCollectionsConfigBuilder() {

		argList.add(ByteString.copyFrom("GetCollectionsConfig", StandardCharsets.UTF_8));
		args(argList);
	}

	public QueryCollectionsConfigBuilder chaincodeName(String chaincodeName) {

		argList.add(ByteString.copyFrom(chaincodeName, StandardCharsets.UTF_8));
		return this;

	}

	@Override
	public QueryCollectionsConfigBuilder context(TransactionContext context) {
		super.context(context);
		return this;
	}

	public static QueryCollectionsConfigBuilder newBuilder() {
		return new QueryCollectionsConfigBuilder();
	}

}
