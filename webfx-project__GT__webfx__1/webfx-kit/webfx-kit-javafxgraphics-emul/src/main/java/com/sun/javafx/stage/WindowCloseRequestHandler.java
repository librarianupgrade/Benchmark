package com.sun.javafx.stage;

import javafx.stage.Window;
import javafx.stage.WindowEvent;
import com.sun.javafx.event.BasicEventDispatcher;
import javafx.event.Event;

public final class WindowCloseRequestHandler extends BasicEventDispatcher {
	private final Window window;

	public WindowCloseRequestHandler(final Window window) {
		this.window = window;
	}

	@Override
	public Event dispatchBubblingEvent(Event event) {
		if (event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
			// the close request hasn't been handled by any user handler,
			// we take care of it here
			window.hide();
			event.consume();
		}

		return event;
	}
}
