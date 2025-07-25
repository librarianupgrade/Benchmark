package webfx.platform.shared.services.resource;

import webfx.platform.shared.services.resource.spi.ResourceServiceProvider;
import webfx.platform.shared.util.async.Future;
import webfx.platform.shared.util.serviceloader.SingleServiceProvider;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public final class ResourceService {

	private static ResourceServiceProvider PROVIDER;

	public static ResourceServiceProvider getProvider() {
		if (PROVIDER == null)
			registerProvider(SingleServiceProvider.getProvider(ResourceServiceProvider.class,
					() -> ServiceLoader.load(ResourceServiceProvider.class)));
		return PROVIDER;
	}

	public static void registerProvider(ResourceServiceProvider provider) {
		PROVIDER = provider;
	}

	public static Future<String> getText(String resourcePath) {
		return getProvider().getText(resourcePath);
	}
}
