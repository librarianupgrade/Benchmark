package webfx.kit.mapper.peers.javafxgraphics.markers;

import javafx.beans.property.Property;

/**
 * @author Bruno Salmon
 */
public interface HasTitleProperty {

	Property<String> titleProperty();

	default void setTitle(String text) {
		titleProperty().setValue(text);
	}

	default String getTitle() {
		return titleProperty().getValue();
	}

}
