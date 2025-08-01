/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.delta.lib;

import java.io.IOException;
import java.io.OutputStream;

/** Write all output to two {@link OutputStream}s.
 * Each write operation is duplicated.
 * Not designed to work when both outputs are the same or wrappers over the same output. 
 */

public class OutputStream2 extends OutputStream {
	private final OutputStream out1;
	private final OutputStream out2;

	public OutputStream2(OutputStream out1, OutputStream out2) {
		if (out1 == out2)
			throw new IllegalArgumentException("Same OutputStream for both outputs");
		this.out1 = out1;
		this.out2 = out2;
	}

	@Override
	public void write(int b) throws IOException {
		out1.write(b);
		out2.write(b);
	}

	@Override
	public void write(byte b[]) throws IOException {
		out1.write(b);
		out2.write(b);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		out1.write(b, off, len);
		out2.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		out1.flush();
		out2.flush();
	}

	@Override
	public void close() throws IOException {
		out1.close();
		out2.close();
	}
}
