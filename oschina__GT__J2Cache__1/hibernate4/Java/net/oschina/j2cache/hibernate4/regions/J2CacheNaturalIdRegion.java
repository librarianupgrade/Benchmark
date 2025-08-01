package net.oschina.j2cache.hibernate4.regions;

import net.oschina.j2cache.hibernate4.CacheRegion;
import net.oschina.j2cache.hibernate4.strategy.J2CacheAccessStrategyFactory;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cfg.Settings;

import java.util.Properties;

public class J2CacheNaturalIdRegion extends J2CacheTransactionalDataRegion implements NaturalIdRegion {

	public J2CacheNaturalIdRegion(J2CacheAccessStrategyFactory accessStrategyFactory, CacheRegion underlyingCache,
			Settings settings, CacheDataDescription metadata, Properties properties) {
		super(accessStrategyFactory, underlyingCache, settings, metadata, properties);
	}

	@Override
	public NaturalIdRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {
		return getAccessStrategyFactory().createNaturalIdRegionAccessStrategy(this, accessType);
	}
}