/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
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
package io.zeebe.monitor.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "MESSAGE_SUBSCRIPTION")
public class MessageSubscriptionEntity {

	@Id
	@Column(name = "ID_")
	private String id;

	@Column(name = "MESSAGE_NAME_")
	private String messageName;

	@Column(name = "CORRELATION_KEY_")
	private String correlationKey;

	@Column(name = "WORKFLOW_INSTANCE_KEY_")
	private long workflowInstanceKey;

	@Column(name = "ELEMENT_INSTANCE_KEY_")
	private long elementInstanceKey;

	@Column(name = "STATE_")
	private String state;

	@Column(name = "TIMESTAMP_")
	private long timestamp;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCorrelationKey() {
		return correlationKey;
	}

	public void setCorrelationKey(String correlationKey) {
		this.correlationKey = correlationKey;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessageName() {
		return messageName;
	}

	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}

	public long getWorkflowInstanceKey() {
		return workflowInstanceKey;
	}

	public void setWorkflowInstanceKey(long workflowInstanceKey) {
		this.workflowInstanceKey = workflowInstanceKey;
	}

	public long getElementInstanceKey() {
		return elementInstanceKey;
	}

	public void setElementInstanceKey(long elementInstanceKey) {
		this.elementInstanceKey = elementInstanceKey;
	}
}
