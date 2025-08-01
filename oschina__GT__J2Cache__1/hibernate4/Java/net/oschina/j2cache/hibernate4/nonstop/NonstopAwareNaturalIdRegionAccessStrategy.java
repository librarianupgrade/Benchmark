package net.oschina.j2cache.hibernate4.nonstop;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;

public class NonstopAwareNaturalIdRegionAccessStrategy implements NaturalIdRegionAccessStrategy {
	private final NaturalIdRegionAccessStrategy actualStrategy;
	private final HibernateNonstopCacheExceptionHandler hibernateNonstopExceptionHandler;

	public NonstopAwareNaturalIdRegionAccessStrategy(NaturalIdRegionAccessStrategy actualStrategy,
			HibernateNonstopCacheExceptionHandler hibernateNonstopExceptionHandler) {
		this.actualStrategy = actualStrategy;
		this.hibernateNonstopExceptionHandler = hibernateNonstopExceptionHandler;
	}

	@Override
	public boolean insert(Object key, Object value) throws CacheException {
		try {
			return actualStrategy.insert(key, value);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
			return false;
		}
	}

	@Override
	public boolean afterInsert(Object key, Object value) throws CacheException {
		try {
			return actualStrategy.afterInsert(key, value);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
			return false;
		}
	}

	@Override
	public boolean update(Object key, Object value) throws CacheException {
		try {
			return actualStrategy.update(key, value);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
			return false;
		}
	}

	@Override
	public boolean afterUpdate(Object key, Object value, SoftLock lock) throws CacheException {
		try {
			return actualStrategy.afterUpdate(key, value, lock);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
			return false;
		}
	}

	@Override
	public NaturalIdRegion getRegion() {
		return actualStrategy.getRegion();
	}

	@Override
	public void evict(Object key) throws CacheException {
		try {
			actualStrategy.evict(key);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
		}
	}

	@Override
	public void evictAll() throws CacheException {
		try {
			actualStrategy.evictAll();
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
		}
	}

	@Override
	public Object get(Object key, long txTimestamp) throws CacheException {
		try {
			return actualStrategy.get(key, txTimestamp);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
			return null;
		}
	}

	@Override
	public SoftLock lockItem(Object key, Object version) throws CacheException {
		try {
			return actualStrategy.lockItem(key, version);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
			return null;
		}
	}

	@Override
	public SoftLock lockRegion() throws CacheException {
		try {
			return actualStrategy.lockRegion();
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
			return null;
		}
	}

	@Override
	public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride)
			throws CacheException {
		try {
			return actualStrategy.putFromLoad(key, value, txTimestamp, version, minimalPutOverride);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
			return false;
		}
	}

	@Override
	public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version) throws CacheException {
		try {
			return actualStrategy.putFromLoad(key, value, txTimestamp, version);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
			return false;
		}
	}

	@Override
	public void remove(Object key) throws CacheException {
		try {
			actualStrategy.remove(key);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
		}
	}

	@Override
	public void removeAll() throws CacheException {
		try {
			actualStrategy.removeAll();
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
		}
	}

	@Override
	public void unlockItem(Object key, SoftLock lock) throws CacheException {
		try {
			actualStrategy.unlockItem(key, lock);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
		}
	}

	@Override
	public void unlockRegion(SoftLock lock) throws CacheException {
		try {
			actualStrategy.unlockRegion(lock);
		} catch (NonStopCacheException nonStopCacheException) {
			hibernateNonstopExceptionHandler.handleNonstopCacheException(nonStopCacheException);
		}
	}

}