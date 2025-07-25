package webfx.kit.mapper.peers.javafxgraphics.markers;

import javafx.beans.property.Property;
import javafx.geometry.Insets;

/**
 * @author Bruno Salmon
 */
public interface HasPaddingProperty {

	Property<Insets> paddingProperty();

	default void setPadding(Insets insets) {
		paddingProperty().setValue(insets);
	}

	default Insets getPadding() {
		return paddingProperty().getValue();
	}

}
