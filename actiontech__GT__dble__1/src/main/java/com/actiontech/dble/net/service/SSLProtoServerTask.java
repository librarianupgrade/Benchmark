/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.net.service;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class SSLProtoServerTask extends ServiceTask {

	@Nonnull
	private final byte[] orgData;

	public SSLProtoServerTask(@Nonnull byte[] orgData, Service service) {
		super(service);
		this.orgData = orgData;
	}

	@NotNull
	@Override
	public ServiceTaskType getType() {
		return ServiceTaskType.SSL;
	}

	@Nonnull
	public byte[] getOrgData() {
		return orgData;
	}
}
