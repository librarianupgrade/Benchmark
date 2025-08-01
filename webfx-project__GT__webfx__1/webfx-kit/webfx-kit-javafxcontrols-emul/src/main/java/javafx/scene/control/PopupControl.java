package javafx.scene.control;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import webfx.platform.client.services.uischeduler.AnimationFramePass;
import webfx.platform.client.services.uischeduler.UiScheduler;

/**
 * An extension of PopupWindow that allows for CSS styling.
 * @since JavaFX 2.0
 */
public class PopupControl extends PopupWindow implements Skinnable, Styleable {

	/**
	 * Sentinel value which can be passed to a control's setMinWidth(), setMinHeight(),
	 * setMaxWidth() or setMaxHeight() methods to indicate that the preferred dimension
	 * should be used for that max and/or min constraint.
	 */
	public static final double USE_PREF_SIZE = Double.NEGATIVE_INFINITY;

	/**
	 * Sentinel value which can be passed to a control's setMinWidth(), setMinHeight(),
	 * setPrefWidth(), setPrefHeight(), setMaxWidth(), setMaxHeight() methods
	 * to reset the control's size constraint back to it's intrinsic size returned
	 * by computeMinWidth(), computeMinHeight(), computePrefWidth(), computePrefHeight(),
	 * computeMaxWidth(), or computeMaxHeight().
	 */
	public static final double USE_COMPUTED_SIZE = -1;

	/*
	static {
	    // Ensures that the default application user agent stylesheet is loaded
	    if (Application.getUserAgentStylesheet() == null) {
	        PlatformImpl.setDefaultPlatformUserAgentStylesheet();
	    }
	}
	*/

	/**
	 * We need a special root node, except we can't replace the special
	 * root node already in the PopupControl. So we'll set our own
	 * special almost-root node that is a child of the root.
	 *
	 * This special root node is responsible for mapping the id, styleClass,
	 * and style defined on the PopupControl such that CSS will read the
	 * values from the PopupControl, and then apply CSS state to that
	 * special node. The node will then be able to pass impl_cssSet calls
	 * along, such that any subclass of PopupControl will be able to
	 * use the Styleable properties  and we'll be able to style it from
	 * CSS, in such a way that it participates and applies to the skin,
	 * exactly the way that normal Skin's work for normal Controls.
	 * @since JavaFX 2.1
	 */
	protected CSSBridge bridge;

	/**
	 * Create a new empty PopupControl.
	 */
	public PopupControl() {
		super();
		this.bridge = new CSSBridge();
		setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
		getContent().add(bridge);
	}

	// TODO the fact that PopupWindow uses a group for auto-moving things
	// around means that the scene resize semantics don't work if the
	// child is a resizable. I will need to replicate those semantics
	// here sometime, such that if the Skin provides a resizable, it is
	// given to match the popup window's width & height.

	/**
	 * The id of this {@code PopupControl}. This simple string identifier is useful for
	 * finding a specific Node within the scene graph. While the id of a Node
	 * should be unique within the scene graph, this uniqueness is not enforced.
	 * This is analogous to the "id" attribute on an HTML element
	 * (<a href="http://www.w3.org/TR/CSS21/syndata.html#value-def-identifier">CSS ID Specification</a>).
	 *
	 * @defaultValue null
	 */
	public final StringProperty idProperty() {
		return bridge.idProperty();
	}

	/**
	 * Sets the id of this {@code PopupControl}. This simple string identifier is useful for
	 * finding a specific Node within the scene graph. While the id of a Node
	 * should be unique within the scene graph, this uniqueness is not enforced.
	 * This is analogous to the "id" attribute on an HTML element
	 * (<a href="http://www.w3.org/TR/CSS21/syndata.html#value-def-identifier">CSS ID Specification</a>).
	 *
	 * @param value  the id assigned to this {@code PopupControl} using the {@code setId}
	 *         method or {@code null}, if no id has been assigned.
	 * @defaultValue null
	 */
	public final void setId(String value) {
		idProperty().set(value);
	}

	/**
	 * The id of this {@code PopupControl}. This simple string identifier is useful for
	 * finding a specific Node within the scene graph. While the id of a Node
	 * should be unique within the scene graph, this uniqueness is not enforced.
	 * This is analogous to the "id" attribute on an HTML element
	 * (<a href="http://www.w3.org/TR/CSS21/syndata.html#value-def-identifier">CSS ID Specification</a>).
	 *
	 * @return the id assigned to this {@code PopupControl} using the {@code setId}
	 *         method or {@code null}, if no id has been assigned.
	 * @defaultValue null
	 */
	@Override
	public final String getId() {
		return idProperty().get();
	}

	/**
	 * Returns the list of String identifiers that make up the styleClass
	 * for this PopupControl.
	 */
	@Override
	public final ObservableList<String> getStyleClass() {
		return bridge.getStyleClass();
	}

	/**
	 * A string representation of the CSS style associated with this
	 * specific {@code PopupControl}. This is analogous to the "style" attribute of an
	 * HTML element. Note that, like the HTML style attribute, this
	 * variable contains style properties and values and not the
	 * selector portion of a style rule.
	 * @param value The inline CSS style to use for this {@code PopupControl}.
	 *         {@code null} is implicitly converted to an empty String.
	 * @defaultValue empty string
	 */
	public final void setStyle(String value) {
		styleProperty().set(value);
	}

	// TODO: javadoc copied from property for the sole purpose of providing a return tag
	/**
	 * A string representation of the CSS style associated with this
	 * specific {@code PopupControl}. This is analogous to the "style" attribute of an
	 * HTML element. Note that, like the HTML style attribute, this
	 * variable contains style properties and values and not the
	 * selector portion of a style rule.
	 * @defaultValue empty string
	 * @return The inline CSS style associated with this {@code PopupControl}.
	 *         If this {@code PopupControl} does not have an inline style,
	 *         an empty String is returned.
	 */
	@Override
	public final String getStyle() {
		return styleProperty().get();
	}

	public final StringProperty styleProperty() {
		return bridge.styleProperty();
	}

	/**
	 * Skin is responsible for rendering this {@code PopupControl}. From the
	 * perspective of the {@code PopupControl}, the {@code Skin} is a black box.
	 * It listens and responds to changes in state in a {@code PopupControl}.
	 * <p>
	 * There is a one-to-one relationship between a {@code PopupControl} and its
	 * {@code Skin}. Every {@code Skin} maintains a back reference to the
	 * {@code PopupControl}.
	 * <p>
	 * A skin may be null.
	 */
	@Override
	public final ObjectProperty<Skin<?>> skinProperty() {
		return skin;
	}

	@Override
	public final void setSkin(Skin<?> value) {
		skinProperty().setValue(value);
	}

	@Override
	public final Skin<?> getSkin() {
		return skinProperty().getValue();
	}

	private final ObjectProperty<Skin<?>> skin = new ObjectPropertyBase<Skin<?>>() {
		// We store a reference to the oldValue so that we can handle
		// changes in the skin properly in the case of binding. This is
		// only needed because invalidated() does not currently take
		// a reference to the old value.
		private Skin<?> oldValue;

		@Override
		public void set(Skin<?> v) {

			if (v == null ? oldValue == null : oldValue != null && v.getClass().equals(oldValue.getClass()))
				return;

			super.set(v);

		}

		@Override
		protected void invalidated() {
			Skin<?> skin = get();

			// Collect the name of the currently installed skin class. We do this
			// so that subsequent updates from CSS to the same skin class will not
			// result in reinstalling the skin
			currentSkinClassName = skin == null ? null : skin.getClass().getName();

			// if someone calls setSkin, we need to make it look like they
			// called set on skinClassName in order to keep CSS from overwriting
			// the skin.
			skinClassNameProperty().set(currentSkinClassName);

			// Let CSS know that this property has been manually changed
			// Dispose of the old skin
			if (oldValue != null) {
				oldValue.dispose();
			}

			// Get the new value, and save it off as the new oldValue
			oldValue = getValue();

			prefWidthCache = -1;
			prefHeightCache = -1;
			minWidthCache = -1;
			minHeightCache = -1;
			maxWidthCache = -1;
			maxHeightCache = -1;
			skinSizeComputed = false;

			final Node n = getSkinNode();
			if (n != null) {
				bridge.getChildren().setAll(n);
			} else {
				bridge.getChildren().clear();
			}

			// calling impl_reapplyCSS() as the styleable properties may now
			// be different, as we will now be able to return styleable properties
			// belonging to the skin. If impl_reapplyCSS() is not called, the
			// getCssMetaData() method is never called, so the
			// skin properties are never exposed.
			//bridge.impl_reapplyCSS();

			// DEBUG: Log that we've changed the skin
			/*
			final PlatformLogger logger = Logging.getControlsLogger();
			if (logger.isLoggable(Level.FINEST)) {
			    logger.finest("Stored skin[" + getValue() + "] on " + this);
			}
			*/
		}

		@Override
		public Object getBean() {
			return PopupControl.this;
		}

		@Override
		public String getName() {
			return "skin";
		}
	};
	/**
	 * Keeps a reference to the name of the class currently acting as the skin.
	 */
	private String currentSkinClassName = null;
	/**
	 * A property that acts as a proxy between the skin property and css.
	 */
	private StringProperty skinClassName = null;

	private StringProperty skinClassNameProperty() {
		if (skinClassName == null) {
			skinClassName = new SimpleStringProperty()/* {
														
														@Override
														public void set(String v) {
														// do not allow the skin to be set to null through CSS
														if (v == null || v.isEmpty() || v.equals(get())) return;
														super.set(v);
														}
														
														@Override
														public void invalidated() {
														
														//
														// if the current skin is not null, then
														// see if then check to see if the current skin's class name
														// is the same as skinClassName. If it is, then there is
														// no need to load the skin class. Note that the only time
														// this would be the case is if someone called setSkin since
														// the skin would be set ahead of the skinClassName
														// (skinClassName is set from the skinProperty's invalidated
														// method, so the skin would be set, then the skinClassName).
														// If the skinClassName is set first (via CSS), then this
														// invalidated method won't get called unless the value
														// has changed (thus, we won't reload the same skin).
														//
														if (get() != null) {
														if (!get().equals(currentSkinClassName)) {
														Control.loadSkinClass(PopupControl.this, get());
														}
														// CSS should not set skin to null
														//                    } else {
														//                        setSkin(null);
														}
														}
														
														@Override
														public Object getBean() {
														return PopupControl.this;
														}
														
														@Override
														public String getName() {
														return "skinClassName";
														}
														
														@Override
														public CssMetaData<CSSBridge,String> getCssMetaData() {
														return SKIN;
														}
														
														}*/;
		}
		return skinClassName;
	}

	/**
	 * Gets the Skin's node, or returns null if there is no Skin.
	 * Convenience method for getting the node of the skin. This is null-safe,
	 * meaning if skin is null then it will return null instead of throwing
	 * a NullPointerException.
	 *
	 * @return The Skin's node, or null.
	 */
	private Node getSkinNode() {
		return getSkin() == null ? null : getSkin().getNode();
	}

	/**
	 * Property for overriding the control's computed minimum width.
	 * This should only be set if the control's internally computed minimum width
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMinWidth(forHeight)</code> will return the control's internally
	 * computed minimum width.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMinWidth(forHeight)</code> to return the control's preferred width,
	 * enabling applications to easily restrict the resizability of the control.
	 */
	private DoubleProperty minWidth;

	/**
	 * Property for overriding the control's computed minimum width.
	 * This should only be set if the control's internally computed minimum width
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMinWidth(forHeight)</code> will return the control's internally
	 * computed minimum width.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMinWidth(forHeight)</code> to return the control's preferred width,
	 * enabling applications to easily restrict the resizability of the control.
	 */
	public final void setMinWidth(double value) {
		minWidthProperty().setValue(value);
	}

	/**
	 * Property for overriding the control's computed minimum width.
	 * This should only be set if the control's internally computed minimum width
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMinWidth(forHeight)</code> will return the control's internally
	 * computed minimum width.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMinWidth(forHeight)</code> to return the control's preferred width,
	 * enabling applications to easily restrict the resizability of the control.
	 */
	public final double getMinWidth() {
		return minWidth == null ? USE_COMPUTED_SIZE : minWidth.getValue();
	}

	public final DoubleProperty minWidthProperty() {
		if (minWidth == null) {
			minWidth = new SimpleDoubleProperty(USE_COMPUTED_SIZE)/* {
																	@Override public void invalidated() {
																	if (isShowing()) bridge.requestLayout();
																	}
																	
																	@Override
																	public Object getBean() {
																	return PopupControl.this;
																	}
																	
																	@Override
																	public String getName() {
																	return "minWidth";
																	}
																	}*/;
		}
		return minWidth;
	}

	/**
	 * Property for overriding the control's computed minimum height.
	 * This should only be set if the control's internally computed minimum height
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMinHeight(forWidth)</code> will return the control's internally
	 * computed minimum height.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMinHeight(forWidth)</code> to return the control's preferred height,
	 * enabling applications to easily restrict the resizability of the control.
	 *
	 */
	private DoubleProperty minHeight;

	/**
	 * Property for overriding the control's computed minimum height.
	 * This should only be set if the control's internally computed minimum height
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMinHeight(forWidth)</code> will return the control's internally
	 * computed minimum height.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMinHeight(forWidth)</code> to return the control's preferred height,
	 * enabling applications to easily restrict the resizability of the control.
	 *
	 */
	public final void setMinHeight(double value) {
		minHeightProperty().setValue(value);
	}

	/**
	 * Property for overriding the control's computed minimum height.
	 * This should only be set if the control's internally computed minimum height
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMinHeight(forWidth)</code> will return the control's internally
	 * computed minimum height.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMinHeight(forWidth)</code> to return the control's preferred height,
	 * enabling applications to easily restrict the resizability of the control.
	 *
	 */
	public final double getMinHeight() {
		return minHeight == null ? USE_COMPUTED_SIZE : minHeight.getValue();
	}

	public final DoubleProperty minHeightProperty() {
		if (minHeight == null) {
			minHeight = new SimpleDoubleProperty(USE_COMPUTED_SIZE)/* {
																	@Override public void invalidated() {
																	if (isShowing()) bridge.requestLayout();
																	}
																	
																	@Override
																	public Object getBean() {
																	return PopupControl.this;
																	}
																	
																	@Override
																	public String getName() {
																	return "minHeight";
																	}
																	}*/;
		}
		return minHeight;
	}

	/**
	 * Convenience method for overriding the control's computed minimum width and height.
	 * This should only be called if the control's internally computed minimum size
	 * doesn't meet the application's layout needs.
	 *
	 * @see #setMinWidth
	 * @see #setMinHeight
	 * @param minWidth  the override value for minimum width
	 * @param minHeight the override value for minimum height
	 */
	public void setMinSize(double minWidth, double minHeight) {
		setMinWidth(minWidth);
		setMinHeight(minHeight);
	}

	/**
	 * Property for overriding the control's computed preferred width.
	 * This should only be set if the control's internally computed preferred width
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getPrefWidth(forHeight)</code> will return the control's internally
	 * computed preferred width.
	 */
	private DoubleProperty prefWidth;

	/**
	 * Property for overriding the control's computed preferred width.
	 * This should only be set if the control's internally computed preferred width
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getPrefWidth(forHeight)</code> will return the control's internally
	 * computed preferred width.
	 */
	public final void setPrefWidth(double value) {
		prefWidthProperty().setValue(value);
	}

	/**
	 * Property for overriding the control's computed preferred width.
	 * This should only be set if the control's internally computed preferred width
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getPrefWidth(forHeight)</code> will return the control's internally
	 * computed preferred width.
	 */
	public final double getPrefWidth() {
		return prefWidth == null ? USE_COMPUTED_SIZE : prefWidth.getValue();
	}

	public final DoubleProperty prefWidthProperty() {
		if (prefWidth == null) {
			prefWidth = new SimpleDoubleProperty(USE_COMPUTED_SIZE)/* {
																	@Override public void invalidated() {
																	if (isShowing()) bridge.requestLayout();
																	}
																	
																	@Override
																	public Object getBean() {
																	return PopupControl.this;
																	}
																	
																	@Override
																	public String getName() {
																	return "prefWidth";
																	}
																	}*/;
		}
		return prefWidth;
	}

	/**
	 * Property for overriding the control's computed preferred height.
	 * This should only be set if the control's internally computed preferred height
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getPrefHeight(forWidth)</code> will return the control's internally
	 * computed preferred width.
	 *
	 */
	private DoubleProperty prefHeight;

	/**
	 * Property for overriding the control's computed preferred height.
	 * This should only be set if the control's internally computed preferred height
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getPrefHeight(forWidth)</code> will return the control's internally
	 * computed preferred width.
	 *
	 */
	public final void setPrefHeight(double value) {
		prefHeightProperty().setValue(value);
	}

	/**
	 * Property for overriding the control's computed preferred height.
	 * This should only be set if the control's internally computed preferred height
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getPrefHeight(forWidth)</code> will return the control's internally
	 * computed preferred width.
	 *
	 */
	public final double getPrefHeight() {
		return prefHeight == null ? USE_COMPUTED_SIZE : prefHeight.getValue();
	}

	public final DoubleProperty prefHeightProperty() {
		if (prefHeight == null) {
			prefHeight = new SimpleDoubleProperty(USE_COMPUTED_SIZE)/* {
																	@Override public void invalidated() {
																	if (isShowing()) bridge.requestLayout();
																	}
																	
																	@Override
																	public Object getBean() {
																	return PopupControl.this;
																	}
																	
																	@Override
																	public String getName() {
																	return "prefHeight";
																	}
																	}*/;
		}
		return prefHeight;
	}

	/**
	 * Convenience method for overriding the control's computed preferred width and height.
	 * This should only be called if the control's internally computed preferred size
	 * doesn't meet the application's layout needs.
	 *
	 * @see #setPrefWidth
	 * @see #setPrefHeight
	 * @param prefWidth the override value for preferred width
	 * @param prefHeight the override value for preferred height
	 */
	public void setPrefSize(double prefWidth, double prefHeight) {
		setPrefWidth(prefWidth);
		setPrefHeight(prefHeight);
	}

	/**
	 * Property for overriding the control's computed maximum width.
	 * This should only be set if the control's internally computed maximum width
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMaxWidth(forHeight)</code> will return the control's internally
	 * computed maximum width.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMaxWidth(forHeight)</code> to return the control's preferred width,
	 * enabling applications to easily restrict the resizability of the control.
	 */
	private DoubleProperty maxWidth;

	/**
	 * Property for overriding the control's computed maximum width.
	 * This should only be set if the control's internally computed maximum width
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMaxWidth(forHeight)</code> will return the control's internally
	 * computed maximum width.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMaxWidth(forHeight)</code> to return the control's preferred width,
	 * enabling applications to easily restrict the resizability of the control.
	 */
	public final void setMaxWidth(double value) {
		maxWidthProperty().setValue(value);
	}

	/**
	 * Property for overriding the control's computed maximum width.
	 * This should only be set if the control's internally computed maximum width
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMaxWidth(forHeight)</code> will return the control's internally
	 * computed maximum width.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMaxWidth(forHeight)</code> to return the control's preferred width,
	 * enabling applications to easily restrict the resizability of the control.
	 */
	public final double getMaxWidth() {
		return maxWidth == null ? USE_COMPUTED_SIZE : maxWidth.getValue();
	}

	public final DoubleProperty maxWidthProperty() {
		if (maxWidth == null) {
			maxWidth = new SimpleDoubleProperty(USE_COMPUTED_SIZE)/* {
																	@Override public void invalidated() {
																	if (isShowing()) bridge.requestLayout();
																	}
																	
																	@Override
																	public Object getBean() {
																	return PopupControl.this;
																	}
																	
																	@Override
																	public String getName() {
																	return "maxWidth";
																	}
																	}*/;
		}
		return maxWidth;
	}

	/**
	 * Property for overriding the control's computed maximum height.
	 * This should only be set if the control's internally computed maximum height
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMaxHeight(forWidth)</code> will return the control's internally
	 * computed maximum height.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMaxHeight(forWidth)</code> to return the control's preferred height,
	 * enabling applications to easily restrict the resizability of the control.
	 *
	 */
	private DoubleProperty maxHeight;

	/**
	 * Property for overriding the control's computed maximum height.
	 * This should only be set if the control's internally computed maximum height
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMaxHeight(forWidth)</code> will return the control's internally
	 * computed maximum height.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMaxHeight(forWidth)</code> to return the control's preferred height,
	 * enabling applications to easily restrict the resizability of the control.
	 *
	 */
	public final void setMaxHeight(double value) {
		maxHeightProperty().setValue(value);
	}

	/**
	 * Property for overriding the control's computed maximum height.
	 * This should only be set if the control's internally computed maximum height
	 * doesn't meet the application's layout needs.
	 * <p>
	 * Defaults to the <code>USE_COMPUTED_SIZE</code> flag, which means that
	 * <code>getMaxHeight(forWidth)</code> will return the control's internally
	 * computed maximum height.
	 * <p>
	 * Setting this value to the <code>USE_PREF_SIZE</code> flag will cause
	 * <code>getMaxHeight(forWidth)</code> to return the control's preferred height,
	 * enabling applications to easily restrict the resizability of the control.
	 *
	 */
	public final double getMaxHeight() {
		return maxHeight == null ? USE_COMPUTED_SIZE : maxHeight.getValue();
	}

	public final DoubleProperty maxHeightProperty() {
		if (maxHeight == null) {
			maxHeight = new SimpleDoubleProperty(USE_COMPUTED_SIZE)/* {
																	@Override public void invalidated() {
																	if (isShowing()) bridge.requestLayout();
																	}
																	
																	@Override
																	public Object getBean() {
																	return PopupControl.this;
																	}
																	
																	@Override
																	public String getName() {
																	return "maxHeight";
																	}
																	}*/;
		}
		return maxHeight;
	}

	/**
	 * Convenience method for overriding the control's computed maximum width and height.
	 * This should only be called if the control's internally computed maximum size
	 * doesn't meet the application's layout needs.
	 *
	 * @see #setMaxWidth
	 * @see #setMaxHeight
	 * @param maxWidth  the override value for maximum width
	 * @param maxHeight the override value for maximum height
	 */
	public void setMaxSize(double maxWidth, double maxHeight) {
		setMaxWidth(maxWidth);
		setMaxHeight(maxHeight);
	}

	/**
	 * Cached prefWidth, prefHeight, minWidth, minHeight. These
	 * results are repeatedly sought during the layout pass,
	 * and caching the results leads to a significant decrease
	 * in overhead.
	 */
	private double prefWidthCache = -1;
	private double prefHeightCache = -1;
	private double minWidthCache = -1;
	private double minHeightCache = -1;
	private double maxWidthCache = -1;
	private double maxHeightCache = -1;
	private boolean skinSizeComputed = false;

	/**
	 * Called during layout to determine the minimum width for this node.
	 * Returns the value from <code>minWidth(forHeight)</code> unless
	 * the application overrode the minimum width by setting the minWidth property.
	 *
	 * @param height the height
	 * @see #setMinWidth
	 * @return the minimum width that this node should be resized to during layout
	 */
	public final double minWidth(double height) {
		double override = getMinWidth();
		if (override == USE_COMPUTED_SIZE) {
			if (minWidthCache == -1)
				minWidthCache = recalculateMinWidth(height);
			return minWidthCache;
		} else if (override == USE_PREF_SIZE) {
			return prefWidth(height);
		}
		return override;
	}

	/**
	 * Called during layout to determine the minimum height for this node.
	 * Returns the value from <code>minHeight(forWidth)</code> unless
	 * the application overrode the minimum height by setting the minHeight property.
	 *
	 * @param width The width
	 * @see #setMinHeight
	 * @return the minimum height that this node should be resized to during layout
	 */
	public final double minHeight(double width) {
		double override = getMinHeight();
		if (override == USE_COMPUTED_SIZE) {
			if (minHeightCache == -1)
				minHeightCache = recalculateMinHeight(width);
			return minHeightCache;
		} else if (override == USE_PREF_SIZE) {
			return prefHeight(width);
		}
		return override;
	}

	/**
	 * Called during layout to determine the preferred width for this node.
	 * Returns the value from <code>prefWidth(forHeight)</code> unless
	 * the application overrode the preferred width by setting the prefWidth property.
	 *
	 * @param height the height
	 * @see #setPrefWidth
	 * @return the preferred width that this node should be resized to during layout
	 */
	public final double prefWidth(double height) {
		double override = getPrefWidth();
		if (override == USE_COMPUTED_SIZE) {
			if (prefWidthCache == -1)
				prefWidthCache = recalculatePrefWidth(height);
			return prefWidthCache;
		} else if (override == USE_PREF_SIZE) {
			return prefWidth(height);
		}
		return override;
	}

	/**
	 * Called during layout to determine the preferred height for this node.
	 * Returns the value from <code>prefHeight(forWidth)</code> unless
	 * the application overrode the preferred height by setting the prefHeight property.
	 *
	 * @param width the width
	 * @see #setPrefHeight
	 * @return the preferred height that this node should be resized to during layout
	 */
	public final double prefHeight(double width) {
		double override = getPrefHeight();
		if (override == USE_COMPUTED_SIZE) {
			if (prefHeightCache == -1)
				prefHeightCache = recalculatePrefHeight(width);
			return prefHeightCache;
		} else if (override == USE_PREF_SIZE) {
			return prefHeight(width);
		}
		return override;
	}

	/**
	 * Called during layout to determine the maximum width for this node.
	 * Returns the value from <code>maxWidth(forHeight)</code> unless
	 * the application overrode the maximum width by setting the maxWidth property.
	 *
	 * @param height the height
	 * @see #setMaxWidth
	 * @return the maximum width that this node should be resized to during layout
	 */
	public final double maxWidth(double height) {
		double override = getMaxWidth();
		if (override == USE_COMPUTED_SIZE) {
			if (maxWidthCache == -1)
				maxWidthCache = recalculateMaxWidth(height);
			return maxWidthCache;
		} else if (override == USE_PREF_SIZE) {
			return prefWidth(height);
		}
		return override;
	}

	/**
	 * Called during layout to determine the maximum height for this node.
	 * Returns the value from <code>maxHeight(forWidth)</code> unless
	 * the application overrode the maximum height by setting the maxHeight property.
	 *
	 * @param width the width
	 * @see #setMaxHeight
	 * @return the maximum height that this node should be resized to during layout
	 */
	public final double maxHeight(double width) {
		double override = getMaxHeight();
		if (override == USE_COMPUTED_SIZE) {
			if (maxHeightCache == -1)
				maxHeightCache = recalculateMaxHeight(width);
			return maxHeightCache;
		} else if (override == USE_PREF_SIZE) {
			return prefHeight(width);
		}
		return override;
	}

	// Implementation of the Resizable interface.
	// Because only the skin can know the min, pref, and max sizes, these
	// functions are implemented to delegate to skin. If there is no skin then
	// we simply return 0 for all the values since a Control without a Skin
	// doesn't render
	private double recalculateMinWidth(double height) {
		recomputeSkinSize();
		return getSkinNode() == null ? 0 : getSkinNode().minWidth(height);
	}

	private double recalculateMinHeight(double width) {
		recomputeSkinSize();
		return getSkinNode() == null ? 0 : getSkinNode().minHeight(width);
	}

	private double recalculateMaxWidth(double height) {
		recomputeSkinSize();
		return getSkinNode() == null ? 0 : getSkinNode().maxWidth(height);
	}

	private double recalculateMaxHeight(double width) {
		recomputeSkinSize();
		return getSkinNode() == null ? 0 : getSkinNode().maxHeight(width);
	}

	private double recalculatePrefWidth(double height) {
		recomputeSkinSize();
		return getSkinNode() == null ? 0 : getSkinNode().prefWidth(height);
	}

	private double recalculatePrefHeight(double width) {
		recomputeSkinSize();
		return getSkinNode() == null ? 0 : getSkinNode().prefHeight(width);
	}

	private void recomputeSkinSize() {
		if (!skinSizeComputed) {
			// RT-14094, RT-16754: We need the skins of the popup
			// and it children before the stage is visible so we
			// can calculate the popup position based on content
			// size.
			//bridge.applyCss();
			skinSizeComputed = true;
		}
	}
	//    public double getBaselineOffset() { return getSkinNode() == null? 0 : getSkinNode().getBaselineOffset(); }

	/**
	 * Create a new instance of the default skin for this control. This is called to create a skin for the control if
	 * no skin is provided via CSS {@code -fx-skin} or set explicitly in a sub-class with {@code  setSkin(...)}.
	 *
	 * @return  new instance of default skin for this control. If null then the control will have no skin unless one
	 *          is provided by css.
	 * @since JavaFX 8.0
	 */
	protected Skin<?> createDefaultSkin() {
		return null;
	}

	/***************************************************************************
	 *                                                                         *
	 *                         StyleSheet Handling                             *
	 *                                                                         *
	 **************************************************************************/

	/*
	private static final CssMetaData<CSSBridge,String> SKIN =
	        new CssMetaData<CSSBridge,String>("-fx-skin",
	                StringConverter.getInstance()) {
	
	            @Override
	            public boolean isSettable(CSSBridge cssBridge) {
	                return !cssBridge.popupControl.skinProperty().isBound();
	            }
	
	            @Override
	            public StyleableProperty<String> getStyleableProperty(CSSBridge cssBridge) {
	                return (StyleableProperty<String>)(WritableValue<String>)cssBridge.popupControl.skinClassNameProperty();
	            }
	        };
	
	private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
	static {
	    final List<CssMetaData<? extends Styleable, ?>> styleables =
	            new ArrayList<CssMetaData<? extends Styleable, ?>>();
	    Collections.addAll(styleables,
	            SKIN
	    );
	    STYLEABLES = Collections.unmodifiableList(styleables);
	}
	*/

	/**
	 * @return The CssMetaData associated with this class, which may include the
	 * CssMetaData of its super classes.
	 * @since JavaFX 8.0
	 */
	/*
	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
	    return STYLEABLES;
	}
	*/

	/**
	 * {@inheritDoc}
	 * @since JavaFX 8.0
	 */
	/*
	@Override
	public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
	    return getClassCssMetaData();
	}
	*/

	/**
	 * @see Node#pseudoClassStateChanged(javafx.css.PseudoClass, boolean)
	 * @since JavaFX 8.0
	 */
	/*
	public final void pseudoClassStateChanged(PseudoClass pseudoClass, boolean active) {
	    bridge.pseudoClassStateChanged(pseudoClass, active);
	}
	*/

	/**
	 * {@inheritDoc}
	 * @return "PopupControl"
	 * @since JavaFX 8.0
	 */
	/*
	@Override
	public String getTypeSelector() {
	    return "PopupControl";
	}
	*/

	/**
	 * {@inheritDoc}
	 *
	 * A PopupControl&apos;s styles are based on the popup &quot;owner&quot; which is the
	 * {@link javafx.stage.PopupWindow#getOwnerNode() ownerNode} or,
	 * if the ownerNode is not set, the root of the {@link javafx.stage.PopupWindow#getOwnerWindow() ownerWindow&apos;s}
	 * scene. If the popup has not been shown, both ownerNode and ownerWindow will be null and {@code null} will be returned.
	 *
	 * Note that the PopupWindow&apos;s scene root is not returned because there is no way to guarantee that the
	 * PopupWindow&apos;s scene root would properly return the ownerNode or ownerWindow.
	 *
	 * @return {@link javafx.stage.PopupWindow#getOwnerNode()}, {@link javafx.stage.PopupWindow#getOwnerWindow()},
	 * or null.
	 * @since JavaFX 8.0
	 */
	/*
	@Override
	public Styleable getStyleableParent() {
	
	    final Node ownerNode = getOwnerNode();
	    if (ownerNode != null) {
	        return ownerNode;
	
	    } else {
	
	        final Window ownerWindow = getOwnerWindow();
	        if (ownerWindow != null) {
	
	            final Scene ownerScene = ownerWindow.getScene();
	            if (ownerScene != null) {
	                return ownerScene.getRoot();
	            }
	        }
	    }
	
	    return bridge.getParent();
	
	}
	*/

	/**
	 * {@inheritDoc}
	 * @since JavaFX 8.0
	 */
	/*
	public final ObservableSet<PseudoClass> getPseudoClassStates() {
	    return FXCollections.emptyObservableSet();
	}
	*/

	/**
	 * @treatAsPrivate implementation detail
	 * @deprecated This is an internal API that is not intended for use and will be removed in the next version
	 */
	@Deprecated
	// SB-dependency: RT-21094 has been filed to track this
	public Node impl_styleableGetNode() {
		return bridge;
	}

	/**
	 * The link between the popup window and the scenegraph.
	 *
	 * @since JavaFX 2.1
	 */
	protected class CSSBridge extends Pane {

		//private final PopupControl popupControl = PopupControl.this;

		/**
		 * Requests a layout pass to be performed before the next scene is
		 * rendered. This is batched up asynchronously to happen once per
		 * "pulse", or frame of animation.
		 * <p/>
		 * If this parent is either a layout root or unmanaged, then it will be
		 * added directly to the scene's dirty layout list, otherwise requestLayout
		 * will be invoked on its parent.
		 */
		@Override
		public void requestLayout() {
			prefWidthCache = -1;
			prefHeightCache = -1;
			minWidthCache = -1;
			minHeightCache = -1;
			maxWidthCache = -1;
			maxHeightCache = -1;
			//skinSizeComputed = false; -- RT-33073 disabled this
			super.requestLayout();
		}

		/**
		 * This method should be treated as final and should not be overridden by any subclasses of CSSBridge.
		 */
		/*
		@Override
		public Styleable getStyleableParent() {
		    return PopupControl.this.getStyleableParent();
		}
		*/

		/**
		 * @treatAsPrivate implementation detail
		 * @deprecated This is an internal API that is not intended for use and will be removed in the next version
		 */
		@Deprecated
		protected void setSkinClassName(String skinClassName) {
			/* no-op - retain for binary compatibility */ }

		/*
		@Override
		public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
		    return PopupControl.this.getCssMetaData();
		}
		*/

		/**
		 * @treatAsPrivate implementation detail
		 * @deprecated This is an internal API that is not intended for use and will be removed in the next version
		 */
		/*
		@Deprecated
		@Override public List<String> impl_getAllParentStylesheets() {
		    Styleable styleable = getStyleableParent();
		    if (styleable instanceof Parent) {
		        return ((Parent)styleable).impl_getAllParentStylesheets();
		    }
		    return null;
		}
		*/

		/**
		 * @treatAsPrivate implementation detail
		 * @deprecated This is an internal API that is not intended for use and will be removed in the next version
		 */
		/*
		@Deprecated
		@Override protected void impl_processCSS(WritableValue<Boolean> unused) {
		    super.impl_processCSS(unused);
		
		    if (getSkin() == null) {
		        // try to create default skin
		        final Skin<?> defaultSkin = createDefaultSkin();
		        if (defaultSkin != null) {
		            skinProperty().set(defaultSkin);
		            super.impl_processCSS(unused);
		        } else {
		            final String msg = "The -fx-skin property has not been defined in CSS for " + this +
		                    " and createDefaultSkin() returned null.";
		            final List<CssError> errors = StyleManager.getErrors();
		            if (errors != null) {
		                CssError error = new CssError(msg);
		                errors.add(error); // RT-19884
		            }
		            Logging.getControlsLogger().severe(msg);
		        }
		    }
		}
		*/

	}

	// WebFx addition

	@Override
	public void show(Window owner) {
		showWhenReady(() -> super.show(owner));
	}

	@Override
	public void show(Window ownerWindow, double anchorX, double anchorY) {
		showWhenReady(() -> super.show(ownerWindow, anchorX, anchorY));
	}

	@Override
	public void show(Node ownerNode, double anchorX, double anchorY) {
		showWhenReady(() -> super.show(ownerNode, anchorX, anchorY));
	}

	private void showWhenReady(Runnable showRunnable) {
		// The skin needs to be set and mapped to the DOM to have a correct window positioning
		if (getSkin() != null) // Ok already set
			showRunnable.run();
		else { // Not yet set (probably first time showing)
			setSkin(createDefaultSkin()); // Setting the skin to the default one
			// Postponing the showRunnable to the shortest delay after the DOM has been updated with the skin
			// Note: Platform.runLater() is too short (next animation frame), but skipping an additional frame seems to work
			UiScheduler.scheduleInFutureAnimationFrame(1, showRunnable, AnimationFramePass.SCENE_PULSE_LAYOUT_PASS);
		}
	}
}
