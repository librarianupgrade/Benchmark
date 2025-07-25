package io.jboot.components.cache;

import com.jfinal.log.Log;
import com.jfinal.plugin.ehcache.IDataLoader;

import java.util.List;

public class AopCache {

	private static final Log LOG = Log.getLog(AopCache.class);

	private static JbootCache aopCache;

	public static JbootCache setCurrentPrefix(String cacheNamePrefix) {
		return getAopCache().setCurrentCacheNamePrefix(cacheNamePrefix);
	}

	public static void clearCurrentPrefix() {
		getAopCache().removeCurrentCacheNamePrefix();
	}

	static JbootCache getAopCache() {
		if (aopCache == null) {
			aopCache = JbootCacheManager.me().getCache(AopCacheConfig.getInstance().getUseCacheName());
		}
		return aopCache;
	}

	public static void setAopCache(JbootCache aopCache) {
		AopCache.aopCache = aopCache;
	}

	public static void put(String cacheName, Object key, Object value) {
		try {
			getAopCache().put(cacheName, key, value);
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
	}

	public static void put(String cacheName, Object key, Object value, int liveSeconds) {
		try {
			getAopCache().put(cacheName, key, value, liveSeconds);
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
	}

	public static List getKeys(String cacheName) {
		try {
			return getAopCache().getKeys(cacheName);
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
		return null;
	}

	public static void remove(String cacheName, Object key) {
		try {
			getAopCache().remove(cacheName, key);
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
	}

	public static void removeAll(String cacheName) {
		try {
			getAopCache().removeAll(cacheName);
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
	}

	public static <T> T get(String cacheName, Object key) {
		try {
			return getAopCache().get(cacheName, key);
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
			remove(cacheName, key);
		}
		return null;
	}

	public static <T> T get(String cacheName, Object key, IDataLoader dataLoader) {
		try {
			return getAopCache().get(cacheName, key, dataLoader);
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
			remove(cacheName, key);
		}
		return null;
	}

	public static <T> T get(String cacheName, Object key, IDataLoader dataLoader, int liveSeconds) {
		try {
			return getAopCache().get(cacheName, key, dataLoader, liveSeconds);
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
			remove(cacheName, key);
		}
		return null;
	}

	public static Integer getTtl(String cacheName, Object key) {
		try {
			return getAopCache().getTtl(cacheName, key);
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
		return null;
	}

	public static void setTtl(String cacheName, Object key, int seconds) {
		try {
			getAopCache().setTtl(cacheName, key, seconds);
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
	}

	private static final AopCacheConfig CONFIG = AopCacheConfig.getInstance();

	public static void putDataToCache(String cacheName, String cacheKey, Object data, int liveSeconds) {
		liveSeconds = liveSeconds > 0 ? liveSeconds : CONFIG.getLiveSeconds();
		if (liveSeconds > 0) {
			put(cacheName, cacheKey, data, liveSeconds);
		} else {
			put(cacheName, cacheKey, data);
		}
	}
}
