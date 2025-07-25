package webfx.kit.mapper.peers.javafxgraphics.markers;

import javafx.beans.property.Property;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public interface HasClipProperty {

	Property<Node> clipProperty();

	default void setClip(Node node) {
		clipProperty().setValue(node);
	}

	default Node getClip() {
		return clipProperty().getValue();
	}
}
