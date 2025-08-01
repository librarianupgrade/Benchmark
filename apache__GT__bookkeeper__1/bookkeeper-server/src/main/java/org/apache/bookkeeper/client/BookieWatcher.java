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
package org.apache.bookkeeper.client;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.bookkeeper.client.BKException.BKNotEnoughBookiesException;
import org.apache.bookkeeper.net.BookieId;
import org.apache.bookkeeper.proto.BookieAddressResolver;

/**
 * Watch for Bookkeeper cluster status.
 */
public interface BookieWatcher {
	Set<BookieId> getBookies() throws BKException;

	Set<BookieId> getAllBookies() throws BKException;

	Set<BookieId> getReadOnlyBookies() throws BKException;

	BookieAddressResolver getBookieAddressResolver();

	/**
	 * Determine if a bookie should be considered unavailable.
	 *
	 * @param id
	 *          Bookie to check
	 * @return whether or not the given bookie is unavailable
	 */
	boolean isBookieUnavailable(BookieId id);

	/**
	 * Create an ensemble with given <i>ensembleSize</i> and <i>writeQuorumSize</i>.
	 *
	 * @param ensembleSize
	 *          Ensemble Size
	 * @param writeQuorumSize
	 *          Write Quorum Size
	 * @return list of bookies for new ensemble.
	 * @throws BKNotEnoughBookiesException
	 */
	List<BookieId> newEnsemble(int ensembleSize, int writeQuorumSize, int ackQuorumSize,
			Map<String, byte[]> customMetadata) throws BKNotEnoughBookiesException;

	/**
	 * Choose a bookie to replace bookie <i>bookieIdx</i> in <i>existingBookies</i>.
	 * @param existingBookies
	 *          list of existing bookies.
	 * @param bookieIdx
	 *          index of the bookie in the list to be replaced.
	 * @return the bookie to replace.
	 * @throws BKNotEnoughBookiesException
	 */
	BookieId replaceBookie(int ensembleSize, int writeQuorumSize, int ackQuorumSize, Map<String, byte[]> customMetadata,
			List<BookieId> existingBookies, int bookieIdx, Set<BookieId> excludeBookies)
			throws BKNotEnoughBookiesException;

	/**
	 * Quarantine <i>bookie</i> so it will not be preferred to be chosen for new ensembles.
	 * @param bookie
	 */
	void quarantineBookie(BookieId bookie);

	/**
	 * Release all quarantined bookies, let it can be chosen for new ensembles.
	 */
	void releaseAllQuarantinedBookies();
}
