/*
 *
 *  Copyright 2016,2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
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

package org.hyperledger.fabric.sdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.ByteString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.helper.Utils;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import static java.lang.String.format;

public class SDKUtils {
	private static final Log logger = LogFactory.getLog(SDKUtils.class);
	private static final boolean IS_DEBUG_LEVEL = logger.isDebugEnabled();

	private SDKUtils() {

	}

	public static CryptoSuite suite = null;

	/**
	 * used asn1 and get hash
	 *
	 * @param blockNumber
	 * @param previousHash
	 * @param dataHash
	 * @return byte[]
	 * @throws IOException
	 * @throws InvalidArgumentException
	 */
	public static byte[] calculateBlockHash(HFClient client, long blockNumber, byte[] previousHash, byte[] dataHash)
			throws IOException, InvalidArgumentException {

		if (previousHash == null) {
			throw new InvalidArgumentException("previousHash parameter is null.");
		}
		if (dataHash == null) {
			throw new InvalidArgumentException("dataHash parameter is null.");
		}
		if (null == client) {
			throw new InvalidArgumentException("client parameter is null.");
		}

		CryptoSuite cryptoSuite = client.getCryptoSuite();
		if (null == cryptoSuite) {
			throw new InvalidArgumentException("Client crypto suite has not  been set.");
		}

		ByteArrayOutputStream s = new ByteArrayOutputStream();
		DERSequenceGenerator seq = new DERSequenceGenerator(s);
		seq.addObject(new ASN1Integer(blockNumber));
		seq.addObject(new DEROctetString(previousHash));
		seq.addObject(new DEROctetString(dataHash));
		seq.close();
		return cryptoSuite.hash(s.toByteArray());

	}

	/**
	 * Check that the proposals all have consistent read write sets
	 *
	 * @param proposalResponses
	 * @return A Collection of sets where each set has consistent proposals.
	 * @throws InvalidArgumentException
	 */

	public static Collection<Set<ProposalResponse>> getProposalConsistencySets(
			Collection<ProposalResponse> proposalResponses) throws InvalidArgumentException {

		return getProposalConsistencySets(proposalResponses, new HashSet<>());

	}

	/**
	 * Check that the proposals all have consistent read write sets
	 *
	 * @param proposalResponses
	 * @param invalid           proposals that were found to be invalid.
	 * @return A Collection of sets where each set has consistent proposals.
	 * @throws InvalidArgumentException
	 */

	public static Collection<Set<ProposalResponse>> getProposalConsistencySets(
			Collection<? extends ProposalResponse> proposalResponses, Set<ProposalResponse> invalid)
			throws InvalidArgumentException {

		if (proposalResponses == null) {
			throw new InvalidArgumentException("proposalResponses collection is null");
		}

		if (proposalResponses.isEmpty()) {
			throw new InvalidArgumentException("proposalResponses collection is empty");
		}

		if (null == invalid) {
			throw new InvalidArgumentException("invalid set is null.");
		}

		HashMap<ByteString, Set<ProposalResponse>> ret = new HashMap<>();

		for (ProposalResponse proposalResponse : proposalResponses) {

			if (proposalResponse.isInvalid()) {
				invalid.add(proposalResponse);
			} else {
				// payload bytes is what's being signed over so it must be consistent.
				final ByteString payloadBytes = proposalResponse.getPayloadBytes();

				if (payloadBytes == null) {
					throw new InvalidArgumentException(format(
							"proposalResponse.getPayloadBytes() was null from peer: %s.", proposalResponse.getPeer()));
				} else if (payloadBytes.isEmpty()) {
					throw new InvalidArgumentException(format(
							"proposalResponse.getPayloadBytes() was empty from peer: %s.", proposalResponse.getPeer()));
				}
				Set<ProposalResponse> set = ret.computeIfAbsent(payloadBytes, k -> new HashSet<>());
				set.add(proposalResponse);
			}
		}

		if (IS_DEBUG_LEVEL && ret.size() > 1) {

			StringBuilder sb = new StringBuilder(1000);

			int i = 0;
			String sep = "";

			for (Map.Entry<ByteString, Set<ProposalResponse>> entry : ret.entrySet()) {
				ByteString bytes = entry.getKey();
				Set<ProposalResponse> presp = entry.getValue();

				sb.append(sep).append("Consistency set: ").append(i++).append(" bytes size: ").append(bytes.size())
						.append(" bytes: ").append(Utils.toHexString(bytes.toByteArray())).append(" [");

				String psep = "";

				for (ProposalResponse proposalResponse : presp) {
					sb.append(psep).append(proposalResponse.getPeer());
					psep = ", ";
				}
				sb.append("]");
				sep = ", ";
			}

			logger.debug(sb.toString());

		}

		return ret.values();

	}
}
