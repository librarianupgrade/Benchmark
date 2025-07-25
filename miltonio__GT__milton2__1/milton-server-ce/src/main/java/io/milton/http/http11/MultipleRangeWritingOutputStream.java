/*
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.http.http11;

import io.milton.common.RangeUtils;
import io.milton.http.Range;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Write the full content of yuor resource to this outputstream, and it will
 * write the requested ranges, including multipart boundaries, to the wrapped
 * outputstream
 *
 * @author brad
 */
public class MultipleRangeWritingOutputStream extends OutputStream {

	private final long totalResourceLength;
	private final OutputStream out;
	private final List<Range> ranges;
	private final String boundary;
	private final String contentType;

	private int currentByte;
	private Range currentRange;

	/**
	 *
	 * @param totalResourceLength
	 * @param out
	 * @param ranges
	 * @param boundary
	 * @param contentType
	 */
	public MultipleRangeWritingOutputStream(long totalResourceLength, OutputStream out, List<Range> ranges,
			String boundary, String contentType) {
		this.out = out;
		this.ranges = new ArrayList(ranges);
		this.boundary = boundary;
		this.contentType = contentType;
		this.totalResourceLength = totalResourceLength;
	}

	@Override
	public void write(int b) throws IOException {
		Range range = getCurrentRange();
		if (range != null) {
			out.write(b);
		}
		currentByte++;
	}

	private boolean isValid(Range r) {
		if (r != null) {
			if (r.getStart() == null || r.getStart() <= currentByte) {
				return r.getFinish() == null || r.getFinish() >= currentByte;

			}
		}
		return false;
	}

	private boolean isBeyond(Range r) {
		return r.getFinish() != null && r.getFinish() < currentByte;
	}

	private Range getCurrentRange() throws IOException {
		if (isValid(currentRange)) {
			return currentRange;
		}

		currentRange = null;
		Iterator<Range> i = ranges.iterator();
		while (i.hasNext()) {
			Range r = i.next();
			if (isValid(r)) {
				writeRangeHeader(r);
				currentRange = r;
				break;
			}
			if (isBeyond(r)) {
				i.remove();
			}
		}
		return currentRange;
	}

	private void writeRangeHeader(Range r) throws IOException {
		//--3d6b6a416f9b5
		//Content-Type: text/html
		//Content-Range: bytes 100-200/1270
		out.write(("\n--" + boundary + "\n").getBytes(StandardCharsets.UTF_8));
		if (contentType != null) {
			out.write(("Content-Type: " + contentType + "\n").getBytes(StandardCharsets.UTF_8));
		}
		out.write(
				("Content-Range: " + RangeUtils.toRangeString(currentByte, r.getFinish(), totalResourceLength) + "\n\n")
						.getBytes(StandardCharsets.UTF_8));
	}

}
