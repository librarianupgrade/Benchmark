package webfx.kit.mapper.peers.javafxgraphics.markers;

import javafx.beans.property.Property;
import javafx.scene.layout.Background;

/**
 * @author Bruno Salmon
 */
public interface HasBackgroundProperty {

	Property<Background> backgroundProperty();

	default void setBackground(Background background) {
		backgroundProperty().setValue(background);
	}

	default Background getBackground() {
		return backgroundProperty().getValue();
	}

}
