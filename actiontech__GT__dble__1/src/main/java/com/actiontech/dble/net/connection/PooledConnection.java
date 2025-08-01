/*
 * Copyright (C) 2016-2023 ActionTech.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */

package com.actiontech.dble.net.connection;

import com.actiontech.dble.backend.pool.ConnectionPool;
import com.actiontech.dble.net.SocketWR;
import com.actiontech.dble.net.service.AuthService;

import java.nio.channels.NetworkChannel;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PooledConnection extends AbstractConnection {

	protected volatile long lastTime;
	private volatile long poolDestroyedTime;
	private volatile String schema = null;
	private volatile String oldSchema;
	private volatile ConnectionPool poolRelated;
	private AtomicBoolean isCreateFail = new AtomicBoolean(false);

	private AtomicInteger state = new AtomicInteger(INITIAL);

	public static final int STATE_REMOVED = -4;
	public static final int STATE_HEARTBEAT = -3;
	public static final int STATE_RESERVED = -2;
	public static final int STATE_IN_USE = -1;
	public static final int INITIAL = 0;
	public static final int STATE_NOT_IN_USE = 1;

	public static final Comparator<PooledConnection> LAST_ACCESS_COMPARABLE;

	private AtomicBoolean createByWaiter = new AtomicBoolean(false);

	static {
		LAST_ACCESS_COMPARABLE = Comparator.comparingLong(entryOne -> entryOne.lastTime);
	}

	public PooledConnection(NetworkChannel channel, SocketWR socketWR) {
		super(channel, socketWR);
	}

	@Override
	public synchronized void cleanup(String reason) {
		if (getService() instanceof AuthService) {
			((AuthService) getService()).onConnectFailed(new Exception(reason == null ? "unknow reason" : reason));
		} else {
			if (this.poolRelated != null) {
				poolRelated.close(this);
			}
		}
		super.cleanup(reason);
	}

	@Override
	public void onConnectFailed(Throwable e) {
		if (getService() instanceof AuthService) {
			((AuthService) getService()).onConnectFailed(e);
		}
	}

	public boolean compareAndSet(int expect, int update) {
		return state.compareAndSet(expect, update);
	}

	public void lazySet(int update) {
		state.lazySet(update);
	}

	public int getState() {
		return state.get();
	}

	public abstract void release();

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long time) {
		this.lastTime = time;
	}

	public long getPoolDestroyedTime() {
		return poolDestroyedTime;
	}

	public void setPoolDestroyedTime(long poolDestroyedTime) {
		this.poolDestroyedTime = poolDestroyedTime;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public ConnectionPool getPoolRelated() {
		return poolRelated;
	}

	public void setPoolRelated(ConnectionPool poolRelated) {
		this.poolRelated = poolRelated;
	}

	public String getOldSchema() {
		return oldSchema;
	}

	public void setOldSchema(String oldSchema) {
		this.oldSchema = oldSchema;
	}

	public AtomicBoolean getIsCreateFail() {
		return isCreateFail;
	}

	public AtomicBoolean getCreateByWaiter() {
		return createByWaiter;
	}
}
