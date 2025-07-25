package webfx.platform.gwt.services.log.spi.impl;

import webfx.platform.shared.services.log.spi.LoggerProvider;

/**
 * @author Bruno Salmon
 */
public class GwtLoggerProvider implements LoggerProvider {

	@Override
	public void log(String message, Throwable throwable) {
		if (message != null)
			logConsole(message);
		if (throwable != null)
			logConsole(throwable);
	}

	private static native void logConsole(Object message) /*-{ $wnd.console.log(message); }-*/;

}
