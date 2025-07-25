package webfx.kit.mapper.peers.javafxcontrols.base;

import javafx.scene.control.Label;

/**
 * @author Bruno Salmon
 */
public interface LabelPeerMixin<N extends Label, NB extends LabelPeerBase<N, NB, NM>, NM extends LabelPeerMixin<N, NB, NM>>

		extends LabeledPeerMixin<N, NB, NM> {

}
