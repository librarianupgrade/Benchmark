package webfx.kit.mapper.peers.javafxgraphics.markers;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public interface HasGraphicProperty {

	ObjectProperty<Node> graphicProperty();

	default HasGraphicProperty setGraphic(Node graphic) {
		graphicProperty().setValue(graphic);
		return this;
	}

	default Node getGraphic() {
		return graphicProperty().getValue();
	}
}
