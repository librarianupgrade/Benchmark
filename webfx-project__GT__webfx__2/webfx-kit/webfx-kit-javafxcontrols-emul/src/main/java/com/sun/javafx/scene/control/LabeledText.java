package com.sun.javafx.scene.control;

import javafx.scene.control.Labeled;
import javafx.scene.text.Text;
import webfx.kit.registry.javafxcontrols.JavaFxControlsRegistry;

/**
 * LabeledText allows the Text to be styled by the CSS properties of Labeled
 * that are meant to style the textual component of the Labeled.
 *
 * LabeledText has the style class "text"
 */
public class LabeledText extends Text {

	private final Labeled labeled;

	public LabeledText(Labeled labeled) {
		super();

		if (labeled == null) {
			throw new IllegalArgumentException("labeled cannot be null");
		}

		this.labeled = labeled;

		//
		// init the state of this Text object to that of the Labeled
		//
		this.setFill(this.labeled.getTextFill());
		this.setFont(this.labeled.getFont());
		this.setTextAlignment(this.labeled.getTextAlignment());
		/*
		this.setUnderline(this.labeled.isUnderline());
		this.setLineSpacing(this.labeled.getLineSpacing());
		*/

		//
		// Bind the state of this Text object to that of the Labeled.
		// Binding these properties prevents CSS from setting them
		//
		this.fillProperty().bind(this.labeled.textFillProperty());
		this.fontProperty().bind(this.labeled.fontProperty());
		// do not bind text - Text doesn't have -fx-text
		this.textAlignmentProperty().bind(this.labeled.textAlignmentProperty());
		/*
		this.underlineProperty().bind(this.labeled.underlineProperty());
		this.lineSpacingProperty().bind(this.labeled.lineSpacingProperty());
		*/

		getStyleClass().addAll("text");
	}

	/**
	 * @return The CssMetaData associated with this class, which may include the
	 * CssMetaData of its super classes.
	 */
	/*
	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
	    return STYLEABLES;
	}
	*/

	/**
	 * {@inheritDoc}
	 */
	/*
	@Override
	public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
	    return getClassCssMetaData();
	}
	*/

	//
	// Replace all of Text's CssMetaData instances that overlap with Labeled
	// with instances of CssMetaData that redirect to Labeled. Thus, when
	// the Labeled is styled,
	//

	/*
	private StyleablePropertyMirror<Font> fontMirror = null;
	private StyleableProperty<Font> fontMirror() {
	    if (fontMirror == null) {
	        fontMirror = new StyleablePropertyMirror<Font>(FONT, "fontMirror", Font.getDefault(), (StyleableProperty<Font>)(WritableValue<Font>)labeled.fontProperty());
	        fontProperty().addListener(fontMirror);
	    }
	    return fontMirror;
	}
	
	private static final CssMetaData<LabeledText,Font> FONT =
	        new FontCssMetaData<LabeledText>("-fx-font", Font.getDefault()) {
	
	            @Override
	            public boolean isSettable(LabeledText node) {
	                return node.labeled != null ? node.labeled.fontProperty().isBound() == false : true;
	            }
	
	            @Override
	            public StyleableProperty<Font> getStyleableProperty(LabeledText node) {
	                return node.fontMirror();
	            }
	        };
	
	private StyleablePropertyMirror<Paint> fillMirror;
	private StyleableProperty<Paint> fillMirror() {
	    if (fillMirror == null) {
	        fillMirror = new StyleablePropertyMirror<Paint>(FILL, "fillMirror", Color.BLACK, (StyleableProperty<Paint>)(WritableValue<Paint>)labeled.textFillProperty());
	        fillProperty().addListener(fillMirror);
	    }
	    return fillMirror;
	}
	
	private static final CssMetaData<LabeledText,Paint> FILL =
	        new CssMetaData<LabeledText,Paint>("-fx-fill",
	                PaintConverter.getInstance(), Color.BLACK) {
	
	            @Override
	            public boolean isSettable(LabeledText node) {
	                return node.labeled.textFillProperty().isBound() == false;
	            }
	
	            @Override
	            public StyleableProperty<Paint> getStyleableProperty(LabeledText node) {
	                return node.fillMirror();
	            }
	        };
	
	private StyleablePropertyMirror<TextAlignment> textAlignmentMirror;
	private StyleableProperty<TextAlignment> textAlignmentMirror() {
	    if (textAlignmentMirror == null) {
	        textAlignmentMirror = new StyleablePropertyMirror<TextAlignment>(TEXT_ALIGNMENT, "textAlignmentMirror", TextAlignment.LEFT, (StyleableProperty<TextAlignment>)(WritableValue<TextAlignment>)labeled.textAlignmentProperty());
	        textAlignmentProperty().addListener(textAlignmentMirror);
	    }
	    return textAlignmentMirror;
	}
	
	private static final CssMetaData<LabeledText,TextAlignment> TEXT_ALIGNMENT =
	        new CssMetaData<LabeledText,TextAlignment>("-fx-text-alignment",
	                new EnumConverter<TextAlignment>(TextAlignment.class),
	                TextAlignment.LEFT) {
	
	            @Override
	            public boolean isSettable(LabeledText node) {
	                return node.labeled.textAlignmentProperty().isBound() == false;
	            }
	
	            @Override
	            public StyleableProperty<TextAlignment> getStyleableProperty(LabeledText node) {
	                return node.textAlignmentMirror();
	            }
	        };
	
	private StyleablePropertyMirror<Boolean> underlineMirror;
	private StyleableProperty<Boolean> underlineMirror() {
	    if (underlineMirror == null) {
	        underlineMirror = new StyleablePropertyMirror<Boolean>(UNDERLINE, "underLineMirror", Boolean.FALSE, (StyleableProperty<Boolean>)(WritableValue<Boolean>)labeled.underlineProperty());
	        underlineProperty().addListener(underlineMirror);
	    }
	    return underlineMirror;
	}
	
	private static final CssMetaData<LabeledText,Boolean> UNDERLINE =
	        new CssMetaData<LabeledText,Boolean>("-fx-underline",
	                BooleanConverter.getInstance(),
	                Boolean.FALSE) {
	
	            @Override
	            public boolean isSettable(LabeledText node) {
	                return node.labeled.underlineProperty().isBound() == false;
	            }
	
	            @Override
	            public StyleableProperty<Boolean> getStyleableProperty(LabeledText node) {
	                return node.underlineMirror();
	            }
	        };
	
	private StyleablePropertyMirror<Number> lineSpacingMirror;
	private StyleableProperty<Number> lineSpacingMirror() {
	    if (lineSpacingMirror == null) {
	        lineSpacingMirror = new StyleablePropertyMirror<Number>(LINE_SPACING, "lineSpacingMirror", 0d, (StyleableProperty<Number>)(WritableValue<Number>)labeled.lineSpacingProperty());
	        lineSpacingProperty().addListener(lineSpacingMirror);
	    }
	    return lineSpacingMirror;
	}
	
	private static final CssMetaData<LabeledText,Number> LINE_SPACING =
	        new CssMetaData<LabeledText,Number>("-fx-line-spacing",
	                SizeConverter.getInstance(),
	                0) {
	
	            @Override
	            public boolean isSettable(LabeledText node) {
	                return node.labeled.lineSpacingProperty().isBound() == false;
	            }
	
	            @Override
	            public StyleableProperty<Number> getStyleableProperty(LabeledText node) {
	                return node.lineSpacingMirror();
	            }
	        };
	
	private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
	static {
	
	    final List<CssMetaData<? extends Styleable, ?>> styleables =
	            new ArrayList<CssMetaData<? extends Styleable, ?>>(Text.getClassCssMetaData());
	
	    for (int n=0,nMax=styleables.size(); n<nMax; n++) {
	        final String prop = styleables.get(n).getProperty();
	
	        if ("-fx-fill".equals(prop)) {
	            styleables.set(n, FILL);
	        } else if ("-fx-font".equals(prop)) {
	            styleables.set(n, FONT);
	        } else if ("-fx-text-alignment".equals(prop)) {
	            styleables.set(n, TEXT_ALIGNMENT);
	        } else if ("-fx-underline".equals(prop)) {
	            styleables.set(n, UNDERLINE);
	        } else if ("-fx-line-spacing".equals(prop)) {
	            styleables.set(n, LINE_SPACING);
	        }
	    }
	
	    STYLEABLES = Collections.unmodifiableList(styleables);
	}
	
	private class StyleablePropertyMirror<T> extends SimpleStyleableObjectProperty<T> implements InvalidationListener {
	
	    private StyleablePropertyMirror(CssMetaData<LabeledText, T> cssMetaData, String name, T initialValue, StyleableProperty<T> property) {
	        super(cssMetaData, LabeledText.this, name, initialValue);
	        this.property = property;
	        this.applying = false;
	    }
	
	    @Override
	    public void invalidated(Observable observable) {
	        // if Text's property is changing but not because a style is being applied,
	        // then it's either because the set method was called on the Labeled's property or
	        // because CSS is resetting Labeled's property to its initial value
	        // (see CssStyleHelper#resetToInitialValues(Styleable))
	        if (applying == false) {
	            super.applyStyle(null, ((ObservableValue<T>)observable).getValue());
	        }
	    }
	
	    @Override
	    public void applyStyle(StyleOrigin newOrigin, T value) {
	
	        applying = true;
	        //
	        // In the case where the Labeled's property was set by an
	        // inline style, this inline style should override values
	        // from lesser origins.
	        //
	        StyleOrigin propOrigin = property.getStyleOrigin();
	
	        //
	        // if propOrigin is null, then the property is in init state
	        // if newOrigin is null, then CSS is resetting this property -
	        //    but don't let CSS overwrite a user set value
	        // if propOrigin is greater than origin, then the style should
	        //    not override
	        //
	        if (propOrigin == null ||
	                (newOrigin != null
	                        ? propOrigin.compareTo(newOrigin) <= 0
	                        : propOrigin != StyleOrigin.USER)) {
	            super.applyStyle(newOrigin, value);
	            property.applyStyle(newOrigin, value);
	        }
	        applying = false;
	    }
	
	    @Override public StyleOrigin getStyleOrigin() {
	        return property.getStyleOrigin();
	    }
	
	    boolean applying;
	    private final StyleableProperty<T> property;
	}
	*/

	static {
		JavaFxControlsRegistry.registerLabeledText();
	}
}
