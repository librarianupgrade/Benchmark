/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package javafx.scene.control;

import javafx.beans.DefaultProperty;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.skin.TitledPaneSkin;

/**
 * <p>A TitledPane is a panel with a title that can be opened and closed.</p>
 *
 * <p>The panel in a TitledPane can be any {@link Node} such as UI controls or groups
 * of nodes added to a layout container.</p>
 *
 * <p>It is not recommended to set the MinHeight, PrefHeight, or MaxHeight
 * for this control.  Unexpected behavior will occur because the
 * TitledPane's height changes when it is opened or closed.</p>
 *
 * <p>Note that whilst TitledPane extends from Labeled, the inherited properties
 * are used to manipulate the TitledPane header, not the content area itself. If
 * the intent is to modify the content area, consider using a layout container
 * such as {@link javafx.scene.layout.StackPane} and setting your actual content
 * inside of that. You can then manipulate the StackPane to get the layout
 * results you are after.</p>
 *
 * <p>Example:</p>
 * <pre><code>
 *  TitledPane t1 = new TitledPane("T1", new Button("B1"));
 * </code></pre>
 *
 * @since JavaFX 2.0
 */
@DefaultProperty("content")
public class TitledPane extends Labeled {

	/***************************************************************************
	 *                                                                         *
	 * Constructors                                                            *
	 *                                                                         *
	 **************************************************************************/

	/**
	 * Creates a new TitledPane with no title or content.
	 */
	public TitledPane() {
		getStyleClass().setAll(DEFAULT_STYLE_CLASS);
		//setAccessibleRole(AccessibleRole.TITLED_PANE);

		// initialize pseudo-class state
		//pseudoClassStateChanged(PSEUDO_CLASS_EXPANDED, true);
	}

	/**
	 * Creates a new TitledPane with a title and content.
	 * @param title The title of the TitledPane.
	 * @param content The content of the TitledPane.
	 */
	public TitledPane(String title, Node content) {
		this();
		setText(title);
		setContent(content);
	}

	/***************************************************************************
	 *                                                                         *
	 * Properties                                                              *
	 *                                                                         *
	 **************************************************************************/

	// --- Content
	private ObjectProperty<Node> content;

	/**
	 * <p> The content of the TitlePane which can be any Node
	 * such as UI controls or groups of nodes added to a layout container.
	 *
	 * @param value The content for this TitlePane.
	 */
	public final void setContent(Node value) {
		contentProperty().set(value);
	}

	/**
	 * The content of the TitledPane.  {@code Null} is returned when
	 * if there is no content.
	 *
	 * @return The content of this TitledPane.
	 */
	public final Node getContent() {
		return content == null ? null : content.get();
	}

	/**
	 * The content of the TitledPane.
	 *
	 * @return The content of the TitlePane.
	 */
	public final ObjectProperty<Node> contentProperty() {
		if (content == null) {
			content = new SimpleObjectProperty<Node>(this, "content");
		}
		return content;
	}

	// --- Expanded
	private BooleanProperty expanded = new BooleanPropertyBase(true) {
		@Override
		protected void invalidated() {
			final boolean active = get();
			//pseudoClassStateChanged(PSEUDO_CLASS_EXPANDED,   active);
			//pseudoClassStateChanged(PSEUDO_CLASS_COLLAPSED, !active);
			//notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
		}

		@Override
		public Object getBean() {
			return TitledPane.this;
		}

		@Override
		public String getName() {
			return "expanded";
		}
	};

	/**
	 * Sets the expanded state of the TitledPane.  The default is {@code true}.
	 *
	 */
	public final void setExpanded(boolean value) {
		expandedProperty().set(value);
	}

	/*
	 * Returns the expanded state of the TitledPane.
	 *
	 * @return The expanded state of the TitledPane.
	 */
	public final boolean isExpanded() {
		return expanded.get();
	}

	/**
	 * The expanded state of the TitledPane.
	 */
	public final BooleanProperty expandedProperty() {
		return expanded;
	}

	// --- Animated
	private BooleanProperty animated = new /*StyleableBooleanProperty*/SimpleBooleanProperty(true)/* {
																									
																									@Override
																									public Object getBean() {
																									return TitledPane.this;
																									}
																									
																									@Override
																									public String getName() {
																									return "animated";
																									}
																									
																									@Override
																									public CssMetaData<TitledPane,Boolean> getCssMetaData() {
																									return StyleableProperties.ANIMATED;
																									}
																									
																									}*/;

	/**
	 * Specifies how the TitledPane should open and close.  The panel will be
	 * animated out when this value is set to {@code true}.  The default is {@code true}.
	 *
	 */
	public final void setAnimated(boolean value) {
		animatedProperty().set(value);
	}

	/**
	 * Returns the animated state of the TitledPane.
	 *
	 * @return The animated state of the TitledPane.
	 */
	public final boolean isAnimated() {
		return animated.get();
	}

	/**
	 *  The animated state of the TitledPane.
	 */
	public final BooleanProperty animatedProperty() {
		return animated;
	}

	// --- Collapsible
	private BooleanProperty collapsible = new /*StyleableBooleanProperty*/SimpleBooleanProperty(true)/* {
																										
																										@Override
																										public Object getBean() {
																										return TitledPane.this;
																										}
																										
																										@Override
																										public String getName() {
																										return "collapsible";
																										}
																										
																										@Override
																										public CssMetaData<TitledPane,Boolean> getCssMetaData() {
																										return StyleableProperties.COLLAPSIBLE;
																										}
																										
																										}*/;

	/**
	 * Specifies if the TitledPane can be collapsed.  The default is {@code true}.
	 *
	 */
	public final void setCollapsible(boolean value) {
		collapsibleProperty().set(value);
	}

	/**
	 * Returns the collapsible state of the TitlePane.
	 *
	 * @return The collapsible state of the TitledPane.
	 */
	public final boolean isCollapsible() {
		return collapsible.get();
	}

	/**
	 * The collapsible state of the TitledPane.
	 */
	public final BooleanProperty collapsibleProperty() {
		return collapsible;
	}

	/***************************************************************************
	 *                                                                         *
	 * Methods                                                                 *
	 *                                                                         *
	 **************************************************************************/

	/** {@inheritDoc} */
	@Override
	protected Skin<?> createDefaultSkin() {
		return new TitledPaneSkin(this);
	}

	/***************************************************************************
	 *                                                                         *
	 * Stylesheet Handling                                                     *
	 *                                                                         *
	 **************************************************************************/

	private static final String DEFAULT_STYLE_CLASS = "titled-pane";
	/*
	
	private static final PseudoClass PSEUDO_CLASS_EXPANDED =
	        PseudoClass.getPseudoClass("expanded");
	private static final PseudoClass PSEUDO_CLASS_COLLAPSED =
	        PseudoClass.getPseudoClass("collapsed");
	
	
	private static class StyleableProperties {
	
	    private static final CssMetaData<TitledPane,Boolean> COLLAPSIBLE =
	            new CssMetaData<TitledPane,Boolean>("-fx-collapsible",
	                    BooleanConverter.getInstance(), Boolean.TRUE) {
	
	                @Override
	                public boolean isSettable(TitledPane n) {
	                    return n.collapsible == null || !n.collapsible.isBound();
	                }
	
	                @Override
	                public StyleableProperty<Boolean> getStyleableProperty(TitledPane n) {
	                    return (StyleableProperty<Boolean>)(WritableValue<Boolean>)n.collapsibleProperty();
	                }
	            };
	
	    private static final CssMetaData<TitledPane,Boolean> ANIMATED =
	            new CssMetaData<TitledPane,Boolean>("-fx-animated",
	                    BooleanConverter.getInstance(), Boolean.TRUE) {
	
	                @Override
	                public boolean isSettable(TitledPane n) {
	                    return n.animated == null || !n.animated.isBound();
	                }
	
	                @Override
	                public StyleableProperty<Boolean> getStyleableProperty(TitledPane n) {
	                    return (StyleableProperty<Boolean>)(WritableValue<Boolean>)n.animatedProperty();
	                }
	            };
	
	    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
	    static {
	        final List<CssMetaData<? extends Styleable, ?>> styleables =
	                new ArrayList<CssMetaData<? extends Styleable, ?>>(Labeled.getClassCssMetaData());
	        styleables.add(COLLAPSIBLE);
	        styleables.add(ANIMATED);
	        STYLEABLES = Collections.unmodifiableList(styleables);
	    }
	}
	*/

	/**
	 * @return The CssMetaData associated with this class, which may include the
	 * CssMetaData of its super classes.
	 * @since JavaFX 8.0
	 */
	/*
	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
	    return StyleableProperties.STYLEABLES;
	}
	
	*/
	/**
	 * {@inheritDoc}
	 * @since JavaFX 8.0
	 *//*
		
		@Override
		public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		 return getClassCssMetaData();
		}
		
		@Override
		public Orientation getContentBias() {
		 final Node c = getContent();
		 return c == null ? super.getContentBias() : c.getContentBias();
		}
		*/

	/***************************************************************************
	 *                                                                         *
	 * Accessibility handling                                                  *
	 *                                                                         *
	 **************************************************************************/

	/*
	@Override
	public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
	    switch (attribute) {
	        case TEXT: {
	            String accText = getAccessibleText();
	            if (accText != null && !accText.isEmpty()) return accText;
	            return getText();
	        }
	        case EXPANDED: return isExpanded();
	        default: return super.queryAccessibleAttribute(attribute, parameters);
	    }
	}
	
	@Override
	public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
	    switch (action) {
	        case EXPAND: setExpanded(true); break;
	        case COLLAPSE: setExpanded(false); break;
	        default: super.executeAccessibleAction(action);
	    }
	}
	*/
}
