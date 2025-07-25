package webfx.kit.mapper.peers.javafxgraphics.gwt.html;

import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLElement;
import javafx.scene.shape.Line;
import webfx.kit.mapper.peers.javafxgraphics.gwt.util.HtmlUtil;
import webfx.kit.mapper.peers.javafxgraphics.base.LinePeerBase;
import webfx.kit.mapper.peers.javafxgraphics.base.LinePeerMixin;

/**
 * This temporary implementation works only for horizontal and vertical lines (not diagonals)
 * @author Bruno Salmon
 */
public final class HtmlLinePeer<N extends Line, NB extends LinePeerBase<N, NB, NM>, NM extends LinePeerMixin<N, NB, NM>>

		extends HtmlShapePeer<N, NB, NM> implements LinePeerMixin<N, NB, NM> {

	public HtmlLinePeer() {
		this((NB) new LinePeerBase(), HtmlUtil.createElement("fx-line"));
	}

	public HtmlLinePeer(NB base, HTMLElement element) {
		super(base, element);
	}

	@Override
	public void updateStartX(Double startX) {
		updateElement();
	}

	@Override
	public void updateStartY(Double startY) {
		updateElement();
	}

	@Override
	public void updateEndX(Double endX) {
		updateElement();
	}

	@Override
	public void updateEndY(Double endY) {
		updateElement();
	}

	private void updateElement() {
		N n = getNode();
		Double startX = n.getStartX();
		Double endX = n.getEndX();
		Double startY = n.getStartY();
		Double endY = n.getEndY();
		HTMLElement e = getElement();
		e.style.left = toPx(Math.min(startX, endX));
		e.style.top = toPx(Math.min(startY, endY));
		e.style.width = CSSProperties.WidthUnionType.of(toPx(Math.abs(endX - startX)));
		e.style.height = CSSProperties.HeightUnionType.of(toPx(Math.abs(endY - startY)));
	}
}
