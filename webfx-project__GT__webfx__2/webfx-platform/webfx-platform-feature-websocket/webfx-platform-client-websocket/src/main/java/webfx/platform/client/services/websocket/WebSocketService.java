package webfx.platform.client.services.websocket;

import webfx.platform.client.services.websocket.spi.WebSocketServiceProvider;
import webfx.platform.shared.services.json.JsonObject;
import webfx.platform.shared.util.serviceloader.SingleServiceProvider;

import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
public final class WebSocketService {

	public static WebSocketServiceProvider getProvider() {
		return SingleServiceProvider.getProvider(WebSocketServiceProvider.class,
				() -> ServiceLoader.load(WebSocketServiceProvider.class));
	}

	public static WebSocket createWebSocket(String url, JsonObject options) {
		return getProvider().createWebSocket(url, options);
	}

}
