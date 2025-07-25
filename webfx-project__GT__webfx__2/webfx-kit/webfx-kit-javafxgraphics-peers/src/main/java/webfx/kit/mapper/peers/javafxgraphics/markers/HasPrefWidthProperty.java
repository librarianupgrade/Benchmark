package webfx.kit.mapper.peers.javafxgraphics.markers;

import javafx.beans.property.DoubleProperty;

/**
 * @author Bruno Salmon
 */
public interface HasPrefWidthProperty {

	DoubleProperty prefWidthProperty();

	default void setPrefWidth(Number prefWidth) {
		prefWidthProperty().setValue(prefWidth);
	}

	default Double getPrefWidth() {
		return prefWidthProperty().getValue();
	}

}
