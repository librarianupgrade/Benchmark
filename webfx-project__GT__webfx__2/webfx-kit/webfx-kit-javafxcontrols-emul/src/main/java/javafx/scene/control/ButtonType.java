package javafx.scene.control;

import static javafx.scene.control.ButtonBar.ButtonData;

/**
 * The ButtonType class is used as part of the JavaFX {@link Dialog} API (more
 * specifically, the {@link DialogPane} API) to specify which buttons should be
 * shown to users in the dialogs. Refer to the {@link DialogPane} class javadoc
 * for more information on how to use this class.
 *
 * @see Alert
 * @see Dialog
 * @see DialogPane
 * @since JavaFX 8u40
 */
public final class ButtonType {

	/**
	 * A pre-defined {@link ButtonType} that displays "Apply" and has a
	 * {@link ButtonData} of {@link ButtonData#APPLY}.
	 */
	public static final ButtonType APPLY = new ButtonType(/*ControlResources.getString("Dialog.apply.button")*/ "Apply",
			ButtonData.APPLY);

	/**
	 * A pre-defined {@link ButtonType} that displays "OK" and has a
	 * {@link ButtonData} of {@link ButtonData#OK_DONE}.
	 */
	public static final ButtonType OK = new ButtonType(/*ControlResources.getString("Dialog.ok.button")*/ "Ok",
			ButtonData.OK_DONE);

	/**
	 * A pre-defined {@link ButtonType} that displays "Cancel" and has a
	 * {@link ButtonData} of {@link ButtonData#CANCEL_CLOSE}.
	 */
	public static final ButtonType CANCEL = new ButtonType(
			/*ControlResources.getString("Dialog.cancel.button")*/ "Cancel", ButtonData.CANCEL_CLOSE);

	/**
	 * A pre-defined {@link ButtonType} that displays "Close" and has a
	 * {@link ButtonData} of {@link ButtonData#CANCEL_CLOSE}.
	 */
	public static final ButtonType CLOSE = new ButtonType(/*ControlResources.getString("Dialog.close.button")*/ "Close",
			ButtonData.CANCEL_CLOSE);

	/**
	 * A pre-defined {@link ButtonType} that displays "Yes" and has a
	 * {@link ButtonData} of {@link ButtonData#YES}.
	 */
	public static final ButtonType YES = new ButtonType(/*ControlResources.getString("Dialog.yes.button")*/ "Yes",
			ButtonData.YES);

	/**
	 * A pre-defined {@link ButtonType} that displays "No" and has a
	 * {@link ButtonData} of {@link ButtonData#NO}.
	 */
	public static final ButtonType NO = new ButtonType(/*ControlResources.getString("Dialog.no.button")*/ "No",
			ButtonData.NO);

	/**
	 * A pre-defined {@link ButtonType} that displays "Finish" and has a
	 * {@link ButtonData} of {@link ButtonData#FINISH}.
	 */
	public static final ButtonType FINISH = new ButtonType(
			/*ControlResources.getString("Dialog.finish.button")*/ "Finish", ButtonData.FINISH);

	/**
	 * A pre-defined {@link ButtonType} that displays "Next" and has a
	 * {@link ButtonData} of {@link ButtonData#NEXT_FORWARD}.
	 */
	public static final ButtonType NEXT = new ButtonType(/*ControlResources.getString("Dialog.next.button")*/"Next",
			ButtonData.NEXT_FORWARD);

	/**
	 * A pre-defined {@link ButtonType} that displays "Previous" and has a
	 * {@link ButtonData} of {@link ButtonData#BACK_PREVIOUS}.
	 */
	public static final ButtonType PREVIOUS = new ButtonType(
			/*ControlResources.getString("Dialog.previous.button")*/ "Previous", ButtonData.BACK_PREVIOUS);

	private final String text;
	private final ButtonData buttonData;

	/**
	 * Creates a ButtonType instance with the given text, and the ButtonData set
	 * as {@link ButtonData#OTHER}.
	 *
	 * @param text The string to display in the text property of controls such
	 *      as {@link Button#textProperty() Button}.
	 */
	public ButtonType(String text) {
		this(text, ButtonData.OTHER);
	}

	/**
	 * Creates a ButtonType instance with the given text, and the ButtonData set
	 * as specified.
	 *
	 * @param text The string to display in the text property of controls such
	 *      as {@link Button#textProperty() Button}.
	 * @param buttonData The type of button that should be created from this ButtonType.
	 */
	public ButtonType(String text, ButtonData buttonData) {
		this.text = text;
		this.buttonData = buttonData;
	}

	/**
	 * Returns the ButtonData specified for this ButtonType in the constructor.
	 */
	public final ButtonData getButtonData() {
		return this.buttonData;
	}

	/**
	 * Returns the text specified for this ButtonType in the constructor;
	 */
	public final String getText() {
		return text;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ButtonType [text=" + getText() + ", buttonData=" + getButtonData() + "]";
	}
}
