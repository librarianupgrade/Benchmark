/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.scurrilous.circe.checksum;

import static com.scurrilous.circe.params.CrcParameters.CRC32C;
import com.scurrilous.circe.IncrementalIntHash;
import com.scurrilous.circe.crc.StandardCrcProvider;
import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.util.zip.Checksum;

public class Java8IntHash implements IntHash {

	private final IncrementalIntHash hash = new StandardCrcProvider().getIncrementalInt(CRC32C);

	@Override
	public int calculate(ByteBuf buffer) {
		return resume(0, buffer);
	}

	@Override
	public int resume(int current, ByteBuf buffer) {
		if (buffer.hasArray()) {
			return hash.resume(current, buffer.array(), buffer.arrayOffset() + buffer.readerIndex(),
					buffer.readableBytes());
		} else {
			return hash.resume(current, buffer.nioBuffer());
		}
	}
}
