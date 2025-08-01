/**
 * Copyright (c) 2014, 2015, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package webfx.framework.client.ui.validation.controlsfx.validation.decoration;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import webfx.framework.client.ui.validation.controlsfx.control.decoration.Decoration;
import webfx.framework.client.ui.validation.controlsfx.control.decoration.GraphicDecoration;
import webfx.framework.client.ui.validation.controlsfx.validation.Severity;
import webfx.framework.client.ui.validation.controlsfx.validation.ValidationMessage;

import java.util.Arrays;
import java.util.Collection;

/**
 * Validation decorator to decorate validation state using images.
 * <br>
 * Validation icons are shown in the bottom-left corner of the control as it is seems to be the most
 * logical location for such information.
 * Required components are marked at the top-left corner with small red triangle.
 * Here is example of such decoration 
 * <br> <br>
 * <img src="GraphicValidationDecorationWithTooltip.png" alt="Screenshot of GraphicValidationDecoration">  
 * 
 */
public class GraphicValidationDecoration extends AbstractValidationDecoration {

	// TODO we shouldn't hardcode this - defer to CSS eventually

	private static final String IMAGES_DIRECTORY = "webfx/framework/client/ui/validation/controlsfx/images/";

	private static final Image ERROR_IMAGE = new Image(IMAGES_DIRECTORY + "decoration-error.png"); //$NON-NLS-1$
	private static final Image WARNING_IMAGE = new Image(IMAGES_DIRECTORY + "decoration-warning.png"); //$NON-NLS-1$
	private static final Image REQUIRED_IMAGE = new Image(IMAGES_DIRECTORY + "required-indicator.png"); //$NON-NLS-1$

	private static final String SHADOW_EFFECT = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"; //$NON-NLS-1$
	private static final String POPUP_SHADOW_EFFECT = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 5);"; //$NON-NLS-1$
	private static final String TOOLTIP_COMMON_EFFECTS = "-fx-font-weight: bold; -fx-padding: 5; -fx-border-width:1;"; //$NON-NLS-1$

	private static final String ERROR_TOOLTIP_EFFECT = POPUP_SHADOW_EFFECT + TOOLTIP_COMMON_EFFECTS
			+ "-fx-background-color: FBEFEF; -fx-text-fill: cc0033; -fx-border-color:cc0033;"; //$NON-NLS-1$

	private static final String WARNING_TOOLTIP_EFFECT = POPUP_SHADOW_EFFECT + TOOLTIP_COMMON_EFFECTS
			+ "-fx-background-color: FFFFCC; -fx-text-fill: CC9900; -fx-border-color: CC9900;"; //$NON-NLS-1$

	/**
	 * Creates default instance
	 */
	public GraphicValidationDecoration() {

	}

	// TODO write javadoc that users should override these methods to customise
	// the error / warning / success nodes to use 
	protected Node createErrorNode() {
		return new ImageView(ERROR_IMAGE);
	}

	protected Node createWarningNode() {
		return new ImageView(WARNING_IMAGE);
	}

	protected Node createDecorationNode(ValidationMessage message) {
		Node graphic = Severity.ERROR == message.getSeverity() ? createErrorNode() : createWarningNode();
		//graphic.setStyle(SHADOW_EFFECT);
		return graphic; // Webfx change (to allow the size change detection on html image load)
		/*
		Label label = new Label();
		label.setGraphic(graphic);
		//label.setTooltip(createTooltip(message));
		label.setAlignment(Pos.CENTER);
		return label;
		*/
	}

	/*
	protected Tooltip createTooltip(ValidationMessage message) {
	    Tooltip tooltip = new Tooltip(message.getText());
	    tooltip.setOpacity(.9);
	    tooltip.setAutoFix(true);
	    tooltip.setStyle( Severity.ERROR == message.getSeverity()? ERROR_TOOLTIP_EFFECT: WARNING_TOOLTIP_EFFECT);
	    return tooltip;
	}
	*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<Decoration> createValidationDecorations(ValidationMessage message) {
		return Arrays.asList(new GraphicDecoration(createDecorationNode(message), Pos.BOTTOM_LEFT));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<Decoration> createRequiredDecorations(Control target) {
		return Arrays.asList(new GraphicDecoration(new ImageView(REQUIRED_IMAGE), Pos.TOP_LEFT, 0, 0, 0.5, 0.5));
	}

}
