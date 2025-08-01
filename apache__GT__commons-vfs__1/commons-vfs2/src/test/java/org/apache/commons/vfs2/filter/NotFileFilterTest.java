/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link NotFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class NotFileFilterTest extends BaseFilterTest {

	@SuppressWarnings("deprecation")
	@Test
	public void testAccept() throws FileSystemException {

		final FileSelectInfo any = createFileSelectInfo(new File("test1.txt"));

		assertFalse(new NotFileFilter(TrueFileFilter.INSTANCE).accept(any));
		assertFalse(new NotFileFilter(TrueFileFilter.TRUE).accept(any));

		assertTrue(new NotFileFilter(FalseFileFilter.INSTANCE).accept(any));
		assertTrue(new NotFileFilter(FalseFileFilter.FALSE).accept(any));
	}

}
// CHECKSTYLE:ON
