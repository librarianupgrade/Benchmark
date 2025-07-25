package org.voovan.tools.collection;

import org.voovan.tools.TPerformance;
import org.voovan.tools.TProperties;
import org.voovan.tools.log.Logger;
import org.voovan.tools.serialize.TSerialize;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存静态类
 *  用于准备 memcached 和 redis 的连接池
 *
 * @author: helyho
 * Project: Framework
 * Create: 2017/9/26 18:30
 */
public class CacheStatic {

	private static final String DEFAULT = "system_default";
	private static ConcurrentHashMap<String, JedisPool> REDIS_POOL_CACHE = new ConcurrentHashMap<String, JedisPool>();

	/**
	 * 获取一个 RedisPool 连接池
	 * @return JedisPool 对象
	 */
	public static JedisPool getDefaultRedisPool() {
		JedisPool redisPool = REDIS_POOL_CACHE.get(DEFAULT);
		if (redisPool == null) {
			try {
				String host = TProperties.getString("redis", "Host");
				int port = TProperties.getInt("redis", "Port");
				int timeout = TProperties.getInt("redis", "Timeout");
				String password = TProperties.getString("redis", "Password");
				int poolSize = TProperties.getInt("redis", "PoolSize");

				redisPool = createRedisPool(DEFAULT, host, port, timeout, password, poolSize);
			} catch (Exception e) {
				Logger.error("Read ./classes/Memcached.properties error");
			}
		}

		return redisPool;
	}

	/**
	 * 根据名称获取一个 Redis 连接池
	 * @param name Redis 连接池名称
	 * @return Redis 连接池
	 */

	public static JedisPool getRedisPool(String name) {
		return REDIS_POOL_CACHE.get(name);
	}

	/**
	 * 获取一个 RedisPool 连接池
	 * @return JedisPool 对象
	 */
	@Deprecated
	public static JedisPool getRedisPool() {
		return getDefaultRedisPool();
	}

	/**
	 * 获取一个 RedisPool 连接池
	 * @param name 连接池名称
	 * @param host 连接地址
	 * @param port 连接端口
	 * @param timeout 超时时间
	 * @param password redis 密码
	 * @param poolSize 池的大小
	 * @return JedisPool 对象
	 */
	public synchronized static JedisPool createRedisPool(String name, String host, int port, int timeout,
			String password, int poolSize) {

		JedisPool redisPool = REDIS_POOL_CACHE.get(name);

		if (redisPool != null) {
			return redisPool;
		}

		if (host == null) {
			return null;
		}

		if (poolSize == 0) {
			poolSize = defaultPoolSize();
		}

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(poolSize);
		poolConfig.setMaxIdle(poolSize);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);

		if (password == null) {
			redisPool = new JedisPool(poolConfig, host, port, timeout);
		} else {
			redisPool = new JedisPool(poolConfig, host, port, timeout, password);
		}

		REDIS_POOL_CACHE.put(name, redisPool);
		return redisPool;
	}

	/**
	 * 获取系统自动计算的连接池的大小
	 * @return 连接池的大小
	 */
	public static int defaultPoolSize() {
		return TPerformance.getProcessorCount() * 10;
	}
}
