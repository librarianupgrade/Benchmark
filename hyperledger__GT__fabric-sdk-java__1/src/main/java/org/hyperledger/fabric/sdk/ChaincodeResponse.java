/*
 *  Copyright 2016 DTCC, Fujitsu Australia Software Technology - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.hyperledger.fabric.sdk;

public class ChaincodeResponse {

	protected int statusReturnCode = -1;

	public enum Status {
		UNDEFINED(0), SUCCESS(200), FAILURE(500);

		private int status = 0;

		Status(int status) {
			this.status = status;
		}

		public int getStatus() {
			return this.status;
		}
	}

	private final Status status;
	private final String message;
	private final String transactionID;

	public boolean isInvalid() {
		return status != Status.SUCCESS;
	}

	ChaincodeResponse(String transactionID, String chaincodeID, int istatus, String message) {

		status = istatus < 400 ? Status.SUCCESS : Status.FAILURE;

		statusReturnCode = istatus;

		this.message = message;
		this.transactionID = transactionID;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the transactionID
	 */
	public String getTransactionID() {
		return transactionID;
	}

}
