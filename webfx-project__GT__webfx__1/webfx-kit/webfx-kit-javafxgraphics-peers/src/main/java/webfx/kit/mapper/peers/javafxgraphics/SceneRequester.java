package webfx.kit.mapper.peers.javafxgraphics;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public interface SceneRequester {

	void requestNodePeerPropertyUpdate(Node node, ObservableValue changedProperty);

	void requestNodePeerListUpdate(Node node, ObservableList changedList, ListChangeListener.Change change);

}
