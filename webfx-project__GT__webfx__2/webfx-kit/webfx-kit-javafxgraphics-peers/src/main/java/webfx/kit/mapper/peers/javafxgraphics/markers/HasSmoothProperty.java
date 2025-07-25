package webfx.kit.mapper.peers.javafxgraphics.markers;

import javafx.beans.property.Property;

/**
 * @author Bruno Salmon
 */
public interface HasSmoothProperty {

	Property<Boolean> smoothProperty();

	default void setSmooth(Boolean smooth) {
		smoothProperty().setValue(smooth);
	}

	default Boolean isSmooth() {
		return smoothProperty().getValue();
	}

}
