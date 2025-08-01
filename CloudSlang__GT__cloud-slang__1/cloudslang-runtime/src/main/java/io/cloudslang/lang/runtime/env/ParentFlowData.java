/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.env;

import io.cloudslang.lang.entities.WorkerGroupMetadata;

import java.io.Serializable;

/**
 * User: stoneo
 * Date: 22/10/2014
 * Time: 15:38
 */
public class ParentFlowData implements Serializable {

	private final Long runningExecutionPlanId;

	private final Long position;

	private final WorkerGroupMetadata workerGroup;

	public ParentFlowData(Long runningExecutionPlanId, Long position, WorkerGroupMetadata workerGroup) {
		this.runningExecutionPlanId = runningExecutionPlanId;
		this.position = position;
		this.workerGroup = workerGroup;
	}

	public Long getRunningExecutionPlanId() {
		return runningExecutionPlanId;
	}

	public Long getPosition() {
		return position;
	}

	public WorkerGroupMetadata getWorkerGroup() {
		return workerGroup;
	}
}
