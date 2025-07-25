package webfx.platform.shared.services.shutdown;

import webfx.platform.shared.services.shutdown.spi.ShutdownProvider;
import webfx.platform.shared.util.serviceloader.SingleServiceProvider;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public final class Shutdown {

	public static ShutdownProvider getProvider() {
		return SingleServiceProvider.getProvider(ShutdownProvider.class,
				() -> ServiceLoader.load(ShutdownProvider.class));
	}

	public static void addShutdownHook(Runnable hook) {
		getProvider().addShutdownHook(hook);
	}

	public static void removeShutdownHook(Runnable hook) {
		getProvider().removeShutdownHook(hook);
	}

	public static void softwareShutdown(boolean exit, int exitStatus) {
		getProvider().softwareShutdown(exit, exitStatus);
	}

	public static boolean isShuttingDown() {
		return getProvider().isShuttingDown();
	}

	public static boolean isSoftwareShutdown() {
		return getProvider().isSoftwareShutdown();
	}
}
