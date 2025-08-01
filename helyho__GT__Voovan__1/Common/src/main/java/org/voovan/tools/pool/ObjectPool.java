package org.voovan.tools.pool;

import org.voovan.Global;
import org.voovan.tools.TDateTime;
import org.voovan.tools.hashwheeltimer.HashWheelTask;
import org.voovan.tools.hashwheeltimer.HashWheelTimer;
import org.voovan.tools.json.JSON;
import org.voovan.tools.log.Logger;
import org.voovan.tools.reflect.TReflect;
import org.voovan.tools.reflect.annotation.NotSerialization;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 对象池
 *      支持超时清理,并且支持指定对象的借出和归还操作
 *      仅仅按照时间长短控制对象的存活周期
 *
 * @author helyho
 * <p>
 * Vestful Framework.
 * WebSite: https://github.com/helyho/Vestful
 * Licence: Apache v2 License
 */
public class ObjectPool<T extends IPooledObject> {

	public static HashWheelTimer OBJECT_POOL_HASH_WHEEL = new HashWheelTimer("OBJECT-POOL", 60, 1000);

	static {
		OBJECT_POOL_HASH_WHEEL.rotate();
	}

	//<ID, 缓存的对象>
	private ConcurrentHashMap<Long, InnerObject<T>> objects = new ConcurrentHashMap<Long, InnerObject<T>>();
	//未解出的对象 ID
	private LinkedBlockingDeque<Long> unborrowedIdList = new LinkedBlockingDeque<Long>();

	private long aliveTime = 0;
	private boolean autoRefreshOnGet = true;
	private Function<T, Boolean> destory;
	private Supplier<T> supplier = null;
	private Function<T, Boolean> validator = null;
	private int minSize = 0;
	private int maxSize = Integer.MAX_VALUE;
	private int interval = 5;
	private int maxBorrow = -1;
	private HashWheelTask task;

	/**
	 * 构造一个对象池
	 * @param aliveTime 对象存活时间,小于等于0时为一直存活,单位:秒
	 * @param autoRefreshOnGet 获取对象时刷新对象存活时间
	 */
	public ObjectPool(long aliveTime, boolean autoRefreshOnGet) {
		this.aliveTime = aliveTime;
		this.autoRefreshOnGet = autoRefreshOnGet;
	}

	/**
	 * 构造一个对象池
	 * @param aliveTime 对象存活时间,单位:秒
	 */
	public ObjectPool(long aliveTime) {
		this.aliveTime = aliveTime;
	}

	/**
	 * 构造一个对象池
	 */
	public ObjectPool() {
	}

	public long getAliveTime() {
		return aliveTime;
	}

	public ObjectPool<T> autoRefreshOnGet(boolean autoRefreshOnGet) {
		this.autoRefreshOnGet = autoRefreshOnGet;
		return this;
	}

	public int getMinSize() {
		return minSize;
	}

	public ObjectPool<T> minSize(int minSize) {
		this.minSize = minSize;
		return this;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public ObjectPool<T> maxSize(int maxSize) {
		this.maxSize = maxSize;
		return this;
	}

	public int getInterval() {
		return interval;
	}

	public ObjectPool<T> interval(int interval) {
		this.interval = interval;
		return this;
	}

	/**
	 * 获取对象构造函数
	 *      在对象被构造工作
	 * @return 对象构造函数
	 */
	public Supplier<T> getSupplier() {
		return supplier;
	}

	/**
	 * 设置对象构造函数
	 *      对象被构造是用的函数
	 * @param supplier 对象构造函数
	 * @return ObjectPool 对象
	 */
	public ObjectPool<T> supplier(Supplier<T> supplier) {
		this.supplier = supplier;
		return this;
	}

	/**
	 * 验证器
	 *  在获取对象时验证
	 * @return Function 对象
	 */
	public Function<T, Boolean> validator() {
		return validator;
	}

	/**
	 * 设置验证器
	 *  在获取对象时验证
	 * @param validator Function 对象
	 * @return ObjectPool 对象
	 */
	public ObjectPool<T> validator(Function<T, Boolean> validator) {
		this.validator = validator;
		return this;
	}

	/**
	 * 获取对象销毁函数
	 *      在对象被销毁前工作
	 * @return 对象销毁函数
	 */
	public Function<T, Boolean> destory() {
		return destory;
	}

	/**
	 * 设置对象销毁函数
	 *      在对象被销毁前工作
	 * @param destory 对象销毁函数
	 * @return ObjectPool 对象
	 */
	public ObjectPool<T> destory(Function<T, Boolean> destory) {
		this.destory = destory;
		return this;
	}

	/**
	 * 设置对象池的对象存活时间
	 * @param aliveTime 对象存活时间,单位:秒
	 * @return ObjectPool 对象
	 */
	public ObjectPool<T> aliveTime(long aliveTime) {
		this.aliveTime = aliveTime;
		return this;
	}

	public int maxBorrow() {
		return maxBorrow;
	}

	public ObjectPool<T> maxBorrow(int maxBorrow) {
		this.maxBorrow = maxBorrow;
		return this;
	}

	/**
	 * 生成ObjectId
	 * @return 生成的ObjectId
	 */
	private long genObjectId() {
		return Global.UNIQUE_ID.nextNumber();
	}

	/**
	 * 是否获取对象时刷新对象存活时间
	 * @return 是否获取对象时刷新对象存活时间
	 */
	public boolean isAutoRefreshOnGet() {
		return autoRefreshOnGet;
	}

	/**
	 * 增加池中的对象
	 * @param obj 增加到池中的对象
	 * @return 对象的 id 值
	 */
	public Long add(T obj) {
		return add(obj, false);
	}

	/**
	 * 增加池中的对象
	 * @param obj 增加到池中的对象
	 * @parma 是否默认为借出状态
	 * @return 对象的 id 值
	 */
	private Long add(T obj, boolean isBorrow) {
		if (obj == null) {
			return null;
		}

		if (objects.size() >= maxSize) {
			return null;
		}

		if (validator != null && !validator.apply(obj)) {
			throw new RuntimeException("add object invalidator ...");
		}

		long id = genObjectId();
		((IPooledObject) obj).setPoolObjectId(id);

		InnerObject innerObject = new InnerObject<T>(this, id, obj);

		objects.put(id, innerObject);

		//默认借出状态不加入未借出队列
		if (!isBorrow) {
			unborrowedIdList.offer(id);
		}

		return id;
	}

	/**
	 * 获取池中的对象
	 * @param id 对象的id
	 * @return 池中的对象
	 */
	private T get(Long id) {
		if (id != null) {
			InnerObject<T> innerObject = objects.get(id);
			if (innerObject != null) {
				return innerObject.getObject();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 借出这个对象
	 *         如果有提供 supplier 函数, 在没有可借出对象时会构造一个新的对象, 否则返回 null
	 * @return 借出的对象
	 */
	public T borrow() {
		try {
			return borrow(0);
		} catch (TimeoutException e) {
			return null;
		}
	}

	/**
	 * 借出这个对象
	 *         如果有提供 supplier 函数, 在没有可借出对象时会构造一个新的对象, 否则返回 null
	 * @param waitTime 超时时间
	 * @return 借出的对象
	 * @throws TimeoutException 超时异常
	 */
	public T borrow(long waitTime) throws TimeoutException {
		while (true) {
			boolean useSupplier = false;

			Long id = null;

			if (waitTime <= 0) {
				id = unborrowedIdList.poll();
			} else {
				try {
					id = unborrowedIdList.poll(waitTime, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					throw new TimeoutException("borrow failed.");
				}
			}

			//检查是否有重复借出
			if (id != null && objects.get(id).isBorrow()) {
				throw new RuntimeException("Object already borrowed");
			}

			T result = null;

			if (id != null) {
				InnerObject innerObject = objects.get(id);
				innerObject.setBorrow(true);
				result = (T) innerObject.getObject();
			}

			if (result == null && supplier != null) {
				if (objects.size() < maxSize) {
					id = add(supplier.get(), true);
					if (id != null) {
						InnerObject innerObject = objects.get(id);
						innerObject.setBorrow(true);
						result = (T) innerObject.getObject();

						useSupplier = true;
					}
				} else {
					return null;
				}
			}

			//检查是否可用, 不可用则移除并重新获取
			if (validator != null && result != null && !validator.apply(result)) {
				remove(result);
				if (useSupplier) {
					return null;
				} else {
					continue;
				}
			}

			return result;
		}

	}

	/**
	 * 归还借出的对象
	 * @param obj 借出的对象
	 */
	public void restitution(T obj) {
		if (obj == null) {
			return;
		}

		if (validator != null && !validator.apply(obj)) {
			remove(obj);
		} else {
			Long id = ((IPooledObject) obj).getPoolObjectId();

			InnerObject innerObject = objects.get(id);
			if (innerObject == null) {
				if (destory != null) {
					destory.apply(obj);
				}
			} else if (maxBorrow > 0 && innerObject.getBorrowCount() >= this.maxBorrow) {
				remove(id);
				if (supplier != null) {
					add(supplier.get());
				}
			} else if (!innerObject.isRemoved() && objects.get(id).setBorrow(false)) {
				unborrowedIdList.offer(id);
			}
		}
	}

	/**
	 * 判断池中是否存在对象
	 * @param id 对象的 hash 值[
	 * @return true: 存在, false: 不存在
	 */
	public boolean contains(long id) {
		return unborrowedIdList.contains(id);
	}

	/**
	 * 移除池中的对象
	 * @param obj 清理的对象
	 */
	private void remove(T obj) {
		Long id = obj.getPoolObjectId();
		remove(id);
	}

	/**
	 * 移除池中的对象
	 * @param id 对象的 hash 值
	 */
	private void remove(long id) {
		unborrowedIdList.remove(id);

		InnerObject innerObject = objects.remove(id);
		if (innerObject != null) {
			if (destory != null) {
				destory.apply((T) innerObject.getObject());
			}
			innerObject.remove();
		}
	}

	/**
	 * 获取当前对象池的大小
	 * @return 对象池的大小
	 */
	public int size() {
		return objects.size();
	}

	/**
	 * 出借的对象数
	 * @return 出借的对象数
	 */
	public int borrowedSize() {
		return objects.size() - unborrowedIdList.size();
	}

	/**
	 * 可用的对象数
	 * @return 可用的对象数
	 */
	public int avaliableSize() {
		return unborrowedIdList.size();
	}

	/**
	 * 清理池中所有的对象
	 */
	public void clear() {
		for (InnerObject innerObject : objects.values()) {
			if (destory != null) {
				destory.apply((T) innerObject.getObject());
			}
			innerObject.remove();
		}

		unborrowedIdList.clear();
		objects.clear();
	}

	/**
	 * 按照 minSize 初始化最小容量的对象
	 */
	public void initObjects() {
		//初始化最小对象池
		if (supplier != null && minSize > 0) {
			for (int i = 0; i < minSize; i++) {
				this.add(supplier.get());
			}

			Logger.fremawork("Object pool init " + minSize + " objects");
		}

	}

	/**
	 * 创建ObjectPool
	 * @return ObjectPool 对象
	 */
	public ObjectPool<T> create() {
		//按照 minSize 初始化最小容量的对象
		initObjects();

		if (interval > 0) {
			final ObjectPool finalobjectPool = this;

			task = new HashWheelTask() {
				@Override
				public void run() {
					try {
						Iterator<InnerObject<T>> iterator = objects.values().iterator();
						int totalSize = objects.size();

						int avaliableSize = 0;
						while (iterator.hasNext()) {

							InnerObject<T> innerObject = iterator.next();

							//1. 借出次数控制
							if (Global.IS_DEBUG_MODE && innerObject.isBorrow()) {
								long usingTime = innerObject.getBorrowTimestamp() > 0
										? System.currentTimeMillis() - innerObject.getBorrowTimestamp()
										: 0;
								if (usingTime > interval * 1000) {
									Logger.warnf(
											"Object borrowed too long time: {id:{}, time:{}, class:{}}\r\nPool info: {}\r\n{}",
											innerObject.getId(), usingTime / 1000, innerObject.getObject().getClass(),
											finalobjectPool, innerObject);
								}
							}

							//2. 不可用的对象移除
							if (validator != null && !validator.apply(innerObject.object)) {
								iterator.remove();
								remove(innerObject.getId());
							}

							//3. 保留最小可用对象
							if (avaliableSize > 0 && avaliableSize <= minSize) {
								innerObject.refresh();
								avaliableSize++;
								continue;
							}

							//4. 未借出且非存活对象使用 destory 进行处理
							if (!innerObject.isBorrow() && !innerObject.isAlive()) {
								if (destory != null) {
									//如果返回 null 则 清理对象, 如果返回为非 null 则刷新对象
									if (destory.apply(innerObject.object)) {
										iterator.remove();
										remove(innerObject.getId());
									} else {
										innerObject.refresh();
									}
								} else {
									iterator.remove();
									remove(innerObject.getId());
								}
							}
						}

						//5. 补齐最小对象数
						int sizeDiff = minSize - totalSize;
						if (sizeDiff > 0 && supplier != null) {
							for (int i = 0; i < sizeDiff; i++) {
								try {
									add(supplier.get());
								} catch (Exception e) {
									Logger.error("Create object failed", e);
									break;
								}
							}
						}
					} catch (Exception e) {
						Logger.error(e);
					}
				}
			};

			OBJECT_POOL_HASH_WHEEL.addTask(task, this.interval, true);
		}

		return this;
	}

	public void shutdown() {
		OBJECT_POOL_HASH_WHEEL.removeTask(task);
		clear();
	}

	/**
	 * 池中缓存的对象模型
	 */
	public class InnerObject<T extends IPooledObject> {
		private volatile long lastVisiediTime;
		private long id;
		private long borrowTimestamp = -1;
		@NotSerialization
		private T object;
		@NotSerialization
		private ObjectPool objectCachedPool;
		private AtomicBoolean isBorrow = new AtomicBoolean(false);
		private AtomicBoolean isRemoved = new AtomicBoolean(false);
		private AtomicInteger borrowCount = new AtomicInteger(0);

		public InnerObject(ObjectPool objectCachedPool, long id, T object) {
			this.objectCachedPool = objectCachedPool;
			this.lastVisiediTime = System.currentTimeMillis();
			this.id = id;
			this.object = object;
		}

		protected boolean setBorrow(Boolean isBorrow) {
			if (isBorrow) {
				borrowCount.getAndIncrement();
				borrowTimestamp = System.currentTimeMillis();
			}
			return this.isBorrow.compareAndSet(!isBorrow, isBorrow);
		}

		protected boolean isBorrow() {
			return isBorrow.get();
		}

		public boolean remove() {
			return this.isRemoved.compareAndSet(false, true);
		}

		public boolean isRemoved() {
			return isRemoved.get();
		}

		public int getBorrowCount() {
			return borrowCount.get();
		}

		public long getBorrowTimestamp() {
			return borrowTimestamp;
		}

		/**
		 * 刷新对象
		 */
		public void refresh() {
			lastVisiediTime = System.currentTimeMillis();
		}

		/**
		 * 获取对象
		 * @return 池中的对象
		 */
		public T getObject() {
			if (objectCachedPool.isAutoRefreshOnGet()) {
				refresh();
			}
			return object;
		}

		/**
		 * 设置对象
		 * @param object 池中的对象
		 */
		public void setObject(T object) {
			this.object = object;
		}

		/**
		 * 缓存的 id
		 * @return 缓存的 id
		 */
		public Long getId() {
			return id;
		}

		/**
		 * 判断对象是否存活
		 * @return true: 对象存活, false: 对象超时
		 */
		public boolean isAlive() {
			if (objectCachedPool.aliveTime <= 0) {
				return true;
			}

			long currentAliveTime = System.currentTimeMillis() - lastVisiediTime;
			if (objectCachedPool.aliveTime > 0 && currentAliveTime >= objectCachedPool.aliveTime * 1000) {
				return false;
			} else {
				return true;
			}
		}

		public String toString() {
			return JSON.toJSON(this).replace("\"", "");
		}
	}

	public String toString() {
		return "{Total:" + objects.size() + ", unborrow:" + unborrowedIdList.size() + "}";
	}
}
