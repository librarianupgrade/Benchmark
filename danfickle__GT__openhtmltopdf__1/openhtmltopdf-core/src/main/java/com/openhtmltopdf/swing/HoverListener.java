/*
 * {{{ header & license
 * Copyright (c) 2007 xhtmlrenderer.dev.java.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.openhtmltopdf.swing;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.openhtmltopdf.context.StyleReference;
import com.openhtmltopdf.layout.LayoutContext;
import com.openhtmltopdf.layout.PaintingInfo;
import com.openhtmltopdf.render.Box;

/**
 * A HoverListener is used to respond to a mouse hovering over a Box in a {@link com.openhtmltopdf.swing.BasicPanel}.
 * In particular, it applies any :hover selectors that apply to the Box in question, and resets those styles
 * as the mouse exits the Box.
 */
public class HoverListener extends DefaultFSMouseListener {
	private Box _previouslyHovered;

	/**
	 * {@inheritDoc}
	 */
	public void onMouseOut(BasicPanel panel, Box box) {
		// Since we keep track of the most recently hovered element, we do not
		// need to explicitly handle mouseout events.  This way we only try to
		// restyle elements that were actually hoverable to begin with.
	}

	/**
	 * {@inheritDoc}
	 */
	public void onMouseOver(BasicPanel panel, Box box) {
		LayoutContext c = panel.getLayoutContext();

		if (c == null) {
			return;
		}

		boolean needRepaint = false;

		Element currentlyHovered = getHoveredElement(c.getCss(), box);

		if (currentlyHovered == panel.hovered_element) {
			return;
		}

		panel.hovered_element = currentlyHovered;

		boolean targetedRepaint = true;
		Rectangle repaintRegion = null;

		// If we moved out of the old block then unstyle it
		if (_previouslyHovered != null) {
			needRepaint = true;
			_previouslyHovered.restyle(c);

			PaintingInfo paintInfo = _previouslyHovered.getPaintingInfo();

			if (paintInfo == null) {
				targetedRepaint = false;
			} else {
				repaintRegion = new Rectangle(paintInfo.getAggregateBounds());
			}

			_previouslyHovered = null;
		}

		if (currentlyHovered != null) {
			needRepaint = true;
			Box target = box.getRestyleTarget();
			target.restyle(c);

			if (targetedRepaint) {
				PaintingInfo paintInfo = target.getPaintingInfo();

				if (paintInfo == null) {
					targetedRepaint = false;
				} else {
					if (repaintRegion == null) {
						repaintRegion = new Rectangle(paintInfo.getAggregateBounds());
					} else {
						repaintRegion.add(paintInfo.getAggregateBounds());
					}
				}
			}

			_previouslyHovered = target;
		}

		if (needRepaint) {
			if (targetedRepaint) {
				panel.repaint(repaintRegion);
			} else {
				panel.repaint();
			}
		}
	}

	// look up the Element that corresponds to the Box we are hovering over
	private Element getHoveredElement(StyleReference style, Box ib) {
		if (ib == null) {
			return null;
		}

		Element element = ib.getElement();

		while (element != null && !style.isHoverStyled(element)) {
			Node node = element.getParentNode();
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				element = (Element) node;
			} else {
				element = null;
			}
		}

		return element;
	}

	/**
	 * Resets the tracking information related to the currently hovered element.
	 */
	public void reset() {
		_previouslyHovered = null;
	}
}
