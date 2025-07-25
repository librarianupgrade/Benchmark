package webfx.kit.mapper.peers.javafxgraphics.markers;

import javafx.beans.property.Property;
import javafx.scene.effect.BlendMode;

/**
 * @author Bruno Salmon
 */
public interface HasBlendModeProperty {

	Property<BlendMode> blendModeProperty();

	default void setBlendMode(BlendMode blendMode) {
		blendModeProperty().setValue(blendMode);
	}

	default BlendMode getBlendMode() {
		return blendModeProperty().getValue();
	}

}
