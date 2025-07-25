package com.qcz.qmplatform.module.sync;

import com.qcz.qmplatform.common.bean.DBProperties;
import com.qcz.qmplatform.common.bean.Observable;
import com.qcz.qmplatform.common.bean.Observed;
import com.qcz.qmplatform.common.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 数据库改变
 */
public class DBChangeCenter implements Observed, Runnable {

	public static final Logger LOGGER = LoggerFactory.getLogger(DBChangeCenter.class);

	/**
	 * 观察者列表
	 */
	private static final List<Observable> OBSERVABLE_LIST = new ArrayList<>();

	/**
	 * 消息队列
	 */
	private static final BlockingQueue<Object> MSG_QUEUE = new LinkedBlockingQueue<>();

	@Override
	public void addObserver(Observable observable) {
		OBSERVABLE_LIST.add(observable);
	}

	@Override
	public void removeObserver(Observable observable) {
		OBSERVABLE_LIST.remove(observable);
	}

	@Override
	public void doNotify(Object msg) {
		MSG_QUEUE.add(msg);
	}

	@Override
	public void run() {
		//noinspection InfiniteLoopStatement
		while (true) {
			try {
				Object msg = MSG_QUEUE.take();
				for (Observable observable : OBSERVABLE_LIST) {
					ThreadPoolUtils.execute(() -> observable.receiveMessage(msg));
				}
			} catch (InterruptedException e) {
				LOGGER.error("An error occurred in MSG_QUEUE.", e);
			}
		}
	}

	/**
	 * 系统消息通知
	 */
	public void notifyMessage() {
		DBNotifyInfo dbNotifyInfo = new DBNotifyInfo();
		dbNotifyInfo.setTableName(DBProperties.Table.SYS_MESSAGE);
		doNotify(dbNotifyInfo);
	}

	public static DBChangeCenter getInstance() {
		return DBChangeCenterHolder.INSTANCE;
	}

	private DBChangeCenter() {

	}

	private static class DBChangeCenterHolder {
		private static final DBChangeCenter INSTANCE = new DBChangeCenter();

		static {
			ThreadPoolUtils.execute(INSTANCE);
		}

	}

	static {
		DBChangeCenter.getInstance().addObserver(new DBChangeWatcher());
	}

}
