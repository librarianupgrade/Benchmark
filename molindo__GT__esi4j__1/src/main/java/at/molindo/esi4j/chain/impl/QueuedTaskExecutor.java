/**
 * Copyright 2010 Molindo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.molindo.esi4j.chain.impl;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;

import at.molindo.esi4j.action.BulkResponseWrapper;
import at.molindo.esi4j.chain.Esi4JEntityResolver;
import at.molindo.esi4j.chain.Esi4JEntityTask;
import at.molindo.esi4j.core.Esi4JOperation;
import at.molindo.utils.collections.ArrayUtils;

/**
 * wrapping a {@link ThreadPoolExecutor} to execute {@link Esi4JEntityTask}s
 * asynchronously. This implementations provides a best-effort ordering of
 * executed tasks
 */
public class QueuedTaskExecutor {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QueuedTaskExecutor.class);

	private static final AtomicInteger EXECUTOR_NUMBER = new AtomicInteger(1);

	private final int _executorNumber = EXECUTOR_NUMBER.getAndIncrement();
	private final AtomicInteger _threadNumber = new AtomicInteger(1);

	private final QueuedTaskProcessor _queuedTaskProcessor;
	private final Esi4JEntityResolver _entityResolver;
	private final ThreadPoolExecutor _executorService;

	/*
	 * TODO used to block execution of Esi4JEntityTasks if a
	 * SerializableEsi4JOperation is submitted. However, it's not the time of
	 * submission but the start of execution that determines order
	 */
	private final ReentrantReadWriteLock _executionOrderLock = new ReentrantReadWriteLock(true);

	private final int _poolSize;

	public QueuedTaskExecutor(QueuedTaskProcessor queuedTaskProcessor, Esi4JEntityResolver entityResolver) {
		if (queuedTaskProcessor == null) {
			throw new NullPointerException("queuedTaskProcessor");
		}
		_queuedTaskProcessor = queuedTaskProcessor;
		_entityResolver = entityResolver;

		// TODO make configurable
		_poolSize = (Runtime.getRuntime().availableProcessors() + 1) / 2;
		_executorService = newExecutorService();
	}

	private ThreadPoolExecutor newExecutorService() {
		log.info("creating new QueuedTaskExecutor with " + _poolSize + " threads");

		ThreadFactory factory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new ExecutorThread(r);
			}
		};

		RejectedExecutionHandler handler = new RejectedExecutionHandler() {

			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				log.warn("executor rejected execution of bulk index task");
			}
		};

		return new ThreadPoolExecutor(_poolSize, _poolSize, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), factory, handler);
	}

	public void execute(Esi4JEntityTask[] tasks) {
		if (!ArrayUtils.empty(tasks)) {
			if (_entityResolver != null) {
				for (int i = 0; i < tasks.length; i++) {
					tasks[i].replaceEntity(_entityResolver);
				}
			}
			_executorService.execute(new BulkIndexRunnable(tasks));
		}
	}

	public <T> T submit(final SerializableEsi4JOperation<T> operation) {
		try {
			T value = _executorService.submit(new OperationCallable<T>(operation)).get();

			if (log.isDebugEnabled()) {
				log.debug("finished submitted operation");
			}

			return value;
		} catch (InterruptedException e) {
			// TODO handle
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			// TODO handle
			throw new RuntimeException(e);
		}
	}

	public void close() {
		_executorService.shutdown();
		try {
			_executorService.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("waiting for termination of executor service interrupted", e);
		}
	}

	public QueuedTaskProcessor getTaskProcessor() {
		return _queuedTaskProcessor;
	}

	public Esi4JEntityResolver getEntityResolver() {
		return _entityResolver;
	}

	/**
	 * thread that references this {@link QueuedTaskExecutor}
	 */
	private final class ExecutorThread extends Thread {

		public ExecutorThread(Runnable r) {
			super(r, QueuedTaskProcessor.class.getSimpleName() + "-" + _executorNumber + "-"
					+ _threadNumber.getAndIncrement());
			setDaemon(true);
		}

		public QueuedTaskExecutor getQueuedTaskExecutor() {
			return QueuedTaskExecutor.this;
		}
	}

	private static final class OperationCallable<T> implements Callable<T>, Serializable {

		private static final long serialVersionUID = 1L;

		// discard during serialization
		private final SerializableEsi4JOperation<T> _operation;

		private OperationCallable(SerializableEsi4JOperation<T> operation) {
			if (operation == null) {
				throw new NullPointerException("operation");
			}
			_operation = operation;
		}

		@Override
		public T call() throws Exception {
			if (_operation == null) {
				return null;
			}

			final QueuedTaskExecutor executor = ((ExecutorThread) Thread.currentThread()).getQueuedTaskExecutor();

			// wait for current tasks to complete
			executor._executionOrderLock.writeLock().lock();
			try {
				return executor.getTaskProcessor().getIndex().execute(_operation);
			} finally {
				executor._executionOrderLock.writeLock().unlock();
			}
		}
	}

	/**
	 * execute a bulk of {@link Esi4JEntityTask}s
	 */
	private static final class BulkIndexRunnable implements Runnable, Serializable {

		private static final long serialVersionUID = 1L;

		private final Esi4JEntityTask[] _tasks;

		public BulkIndexRunnable(Esi4JEntityTask[] tasks) {
			_tasks = tasks;
		}

		@Override
		public void run() {
			final QueuedTaskExecutor executor = ((ExecutorThread) Thread.currentThread()).getQueuedTaskExecutor();

			executor._executionOrderLock.readLock().lock();
			try { // ensure unlock()

				executor.getTaskProcessor().onBeforeBulkIndex();
				try { // ensure onAfterBulkIndex()

					index(executor);

				} finally {
					executor.getTaskProcessor().onAfterBulkIndex();
				}
			} finally {
				executor._executionOrderLock.readLock().unlock();
			}
		}

		private void index(final QueuedTaskExecutor executor) {
			Esi4JEntityResolver entityResolver = executor.getEntityResolver();

			if (entityResolver != null) {
				for (int i = 0; i < _tasks.length; i++) {
					_tasks[i].resolveEntity(entityResolver);
				}
			}

			BulkResponseWrapper response = executor.getTaskProcessor().getIndex()
					.executeBulk(new Esi4JOperation<ListenableActionFuture<BulkResponse>>() {

						@Override
						public ListenableActionFuture<BulkResponse> execute(Client client, String indexName,
								OperationContext helper) {
							BulkRequestBuilder bulk = client.prepareBulk();

							for (int i = 0; i < _tasks.length; i++) {
								_tasks[i].addToBulk(bulk, indexName, helper);
							}

							ListenableActionFuture<BulkResponse> response = bulk.execute();

							return response;
						}
					}).actionGet();

			int failed = 0;
			for (BulkItemResponse item : response.getBulkResponse()) {
				if (item.isFailed()) {
					failed++;
				}
			}

			if (failed > 0) {
				log.warn("failed to index " + failed + " items. index might be out of sync");
			}

			if (log.isDebugEnabled()) {
				int indexed = response.getBulkResponse().items().length - failed;
				log.debug("finished bulk indexing " + indexed + " items");
			}
		}
	}

}
