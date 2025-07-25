package webfx.kit.launcher.spi.gwt;

import com.sun.javafx.application.ParametersImpl;
import elemental2.dom.DataTransfer;
import elemental2.dom.DomGlobal;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.stage.Screen;
import webfx.kit.mapper.peers.javafxgraphics.gwt.util.DragboardDataTransferHolder;
import webfx.kit.launcher.spi.base.WebFxKitLauncherProviderBase;
import webfx.platform.shared.services.log.Logger;
import webfx.platform.shared.util.Strings;
import webfx.platform.shared.util.collection.Collections;
import webfx.platform.shared.util.function.Factory;

import java.util.Map;

/**
 * @author Bruno Salmon
 */
public final class GwtWebFxKitLauncherProvider extends WebFxKitLauncherProviderBase {

	private Application application;
	private HostServices hostServices;

	public GwtWebFxKitLauncherProvider() {
		super(DomGlobal.navigator.userAgent);
	}

	@Override
	public HostServices getHostServices() {
		if (hostServices == null)
			hostServices = uri -> DomGlobal.window.open(uri);
		return hostServices;
	}

	@Override
	public Clipboard getSystemClipboard() {
		return new Clipboard() {
			@Override
			public boolean setContent(Map<DataFormat, Object> content) {
				setClipboardContent((String) content.get(DataFormat.PLAIN_TEXT));
				super.setContent(content);
				return true;
			}

			@Override
			public Object getContentImpl(DataFormat dataFormat) {
				if (dataFormat == DataFormat.PLAIN_TEXT)
					return getClipboardContent();
				return super.getContent(dataFormat);
			}
		};
	}

	private static void setClipboardContent(String text) {
		callClipboardCommand(text, "copy");
	}

	private static String getClipboardContent() {
		return callClipboardCommand(" ", "paste");
	}

	private static native String callClipboardCommand(String text, String command) /*-{
        var textArea = document.createElement('textarea');
        textArea.setAttribute('style','width:1px;border:0;opacity:0;');
        document.body.appendChild(textArea);
        textArea.value = text;
        textArea.select();
        document.execCommand(command);
        document.body.removeChild(textArea);
        return textArea.value;
    }-*/;

	@Override
	public Dragboard createDragboard(Scene scene) {
		return new Dragboard(scene) {
			@Override
			public boolean setContent(Map<DataFormat, Object> content) {
				boolean result = false;
				DataTransfer dragBoardDataTransfer = DragboardDataTransferHolder.getDragboardDataTransfer();
				dragBoardDataTransfer.clearData();
				if (content != null)
					for (Map.Entry<DataFormat, Object> entry : content.entrySet()) {
						String value = Strings.toString(entry.getValue());
						for (String formatIdentifier : entry.getKey().getIdentifiers())
							if (dragBoardDataTransfer.setData(formatIdentifier, value))
								result = true;
					}
				return result;
			}

			@Override
			public Object getContentImpl(DataFormat dataFormat) {
				return DragboardDataTransferHolder.getDragboardDataTransfer()
						.getData(Collections.first(dataFormat.getIdentifiers()));
			}
		};
	}

	@Override
	public Screen getPrimaryScreen() {
		elemental2.dom.Screen screen = DomGlobal.screen;
		return Screen.from(toRectangle2D(screen.width, screen.height),
				toRectangle2D(screen.availWidth, screen.availHeight));
	}

	private static Rectangle2D toRectangle2D(double width, double height) {
		return new Rectangle2D(0, 0, width, height);
	}

	@Override
	public void launchApplication(Factory<Application> applicationFactory, String... args) {
		application = applicationFactory.create();
		if (application != null)
			try {
				ParametersImpl.registerParameters(application, new ParametersImpl(args));
				application.init();
				application.start(getPrimaryStage());
			} catch (Exception e) {
				Logger.log("Error while launching the JavaFx application", e);
			}
	}

	@Override
	public Application getApplication() {
		return application;
	}
}