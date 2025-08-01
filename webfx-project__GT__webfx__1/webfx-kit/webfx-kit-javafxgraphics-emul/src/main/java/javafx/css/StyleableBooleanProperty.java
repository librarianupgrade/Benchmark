package javafx.css;

import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.value.ObservableValue;

/**
 * This class extends {@code BooleanPropertyBase} and provides a partial
 * implementation of a {@code StyleableProperty}. The method
 * {@link StyleableProperty#getCssMetaData()} is not implemented.
 *
 * This class is used to make a {@link javafx.beans.property.BooleanProperty},
 * that would otherwise be implemented as a {@link BooleanPropertyBase},
 * style&#8209;able by CSS.
 *
 * @see javafx.beans.property.BooleanPropertyBase
 * @see CssMetaData
 * @see StyleableProperty
 * @since JavaFX 8.0
 */
public abstract class StyleableBooleanProperty extends BooleanPropertyBase implements StyleableProperty<Boolean> {

	/**
	 * The constructor of the {@code StyleableBooleanProperty}.
	 */
	public StyleableBooleanProperty() {
		super();
	}

	/**
	 * The constructor of the {@code StyleableBooleanProperty}.
	 *
	 * @param initialValue
	 *            the initial value of the wrapped {@code Object}
	 */
	public StyleableBooleanProperty(boolean initialValue) {
		super(initialValue);
	}

	/** {@inheritDoc} */
	@Override
	public void applyStyle(StyleOrigin origin, Boolean v) {
		// call set here in case it has been overridden in the javafx.beans.property
		set(v.booleanValue());
		this.origin = origin;
	}

	/** {@inheritDoc} */
	@Override
	public void bind(ObservableValue<? extends Boolean> observable) {
		super.bind(observable);
		origin = StyleOrigin.USER;
	}

	/** {@inheritDoc} */
	@Override
	public void set(boolean v) {
		super.set(v);
		origin = StyleOrigin.USER;
	}

	/** {@inheritDoc} */
	@Override
	public StyleOrigin getStyleOrigin() {
		return origin;
	}

	private StyleOrigin origin = null;
}
