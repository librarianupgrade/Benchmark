package javafx.scene.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.util.StringConverter;
import webfx.kit.registry.javafxcontrols.JavaFxControlsRegistry;

/**
 * The ChoiceBox is used for presenting the user with a relatively small set of
 * predefined choices from which they may choose. The ChoiceBox, when "showing",
 * will display to the user these choices and allow them to pick exactly one
 * choice. When not showing, the current choice is displayed.
 * <p>
 * By default, the ChoiceBox has no item selected unless otherwise specified.
 * Although the ChoiceBox will only allow a user to select from the predefined
 * list, it is possible for the developer to specify the selected item to be
 * something other than what is available in the predefined list. This is
 * required for several important use cases.
 * <p>
 * It means configuration of the ChoiceBox is order independent. You
 * may either specify the items and then the selected item, or you may
 * specify the selected item and then the items. Either way will function
 * correctly.
 * <p>
 * ChoiceBox item selection is handled by
 * {@link javafx.scene.control.SelectionModel SelectionModel}
 * As with ListView and ComboBox, it is possible to modify the
 * {@link javafx.scene.control.SelectionModel SelectionModel} that is used,
 * although this is likely to be rarely changed. ChoiceBox supports only a
 * single selection model, hence the default used is a {@link SingleSelectionModel}.
 *
 * <pre>
 * import javafx.scene.control.ChoiceBox;
 *
 * ChoiceBox cb = new ChoiceBox();
 * cb.getItems().addAll("item1", "item2", "item3");
 * </pre>
 * @since JavaFX 2.0
 */
//@DefaultProperty("items")
public class ChoiceBox<T> extends Control {

	/***************************************************************************
	 *                                                                         *
	 * Static properties and methods                                           *
	 *                                                                         *
	 **************************************************************************/

	/**
	 * Called prior to the ChoiceBox showing its popup after the user
	 * has clicked or otherwise interacted with the ChoiceBox.
	 * @since JavaFX 8u60
	 */
	public static final EventType<Event> ON_SHOWING = new EventType<Event>(Event.ANY, "CHOICE_BOX_ON_SHOWING");

	/**
	 * Called after the ChoiceBox has shown its popup.
	 * @since JavaFX 8u60
	 */
	public static final EventType<Event> ON_SHOWN = new EventType<Event>(Event.ANY, "CHOICE_BOX_ON_SHOWN");

	/**
	 * Called when the ChoiceBox popup <b>will</b> be hidden.
	 * @since JavaFX 8u60
	 */
	public static final EventType<Event> ON_HIDING = new EventType<Event>(Event.ANY, "CHOICE_BOX_ON_HIDING");

	/**
	 * Called when the ChoiceBox popup has been hidden.
	 * @since JavaFX 8u60
	 */
	public static final EventType<Event> ON_HIDDEN = new EventType<Event>(Event.ANY, "CHOICE_BOX_ON_HIDDEN");

	/***************************************************************************
	 *                                                                         *
	 * Constructors                                                            *
	 *                                                                         *
	 **************************************************************************/

	/**
	 * Create a new ChoiceBox which has an empty list of items.
	 */
	public ChoiceBox() {
		this(FXCollections.<T>observableArrayList());
	}

	/**
	 * Create a new ChoiceBox with the given set of items. Since it is observable,
	 * the content of this list may change over time and the ChoiceBox will
	 * be updated accordingly.
	 * @param items
	 */
	public ChoiceBox(ObservableList<T> items) {
		getStyleClass().setAll("choice-box");
		//setAccessibleRole(AccessibleRole.COMBO_BOX);
		setItems(items);
		setSelectionModel(new ChoiceBoxSelectionModel<T>(this));

		// listen to the value property, if the value is
		// set to something that exists in the items list, update the
		// selection model to indicate that this is the selected item
		valueProperty().addListener((ov, t, t1) -> {
			if (getItems() == null)
				return;
			int index = getItems().indexOf(t1);
			if (index > -1) {
				getSelectionModel().select(index);
			}
		});
	}

	/***************************************************************************
	 *                                                                         *
	 * Properties                                                              *
	 *                                                                         *
	 **************************************************************************/

	/**
	 * The selection model for the ChoiceBox. Only a single choice can be made,
	 * hence, the ChoiceBox supports only a SingleSelectionModel. Generally, the
	 * main interaction with the selection model is to explicitly set which item
	 * in the items list should be selected, or to listen to changes in the
	 * selection to know which item has been chosen.
	 */
	private ObjectProperty<SingleSelectionModel<T>> selectionModel = new SimpleObjectProperty<SingleSelectionModel<T>>(
			this, "selectionModel") {
		private SelectionModel<T> oldSM = null;

		@Override
		protected void invalidated() {
			if (oldSM != null) {
				oldSM.selectedItemProperty().removeListener(selectedItemListener);
			}
			SelectionModel<T> sm = get();
			oldSM = sm;
			if (sm != null) {
				sm.selectedItemProperty().addListener(selectedItemListener);
			}
		}
	};

	private ChangeListener<T> selectedItemListener = (ov, t, t1) -> {
		if (!valueProperty().isBound()) {
			setValue(t1);
		}
	};

	public final void setSelectionModel(SingleSelectionModel<T> value) {
		selectionModel.set(value);
	}

	public final SingleSelectionModel<T> getSelectionModel() {
		return selectionModel.get();
	}

	public final ObjectProperty<SingleSelectionModel<T>> selectionModelProperty() {
		return selectionModel;
	}

	/**
	 * Indicates whether the drop down is displaying the list of choices to the
	 * user. This is a readonly property which should be manipulated by means of
	 * the #show and #hide methods.
	 */
	private Property<Boolean> showing = new SimpleObjectProperty<Boolean>(false); /* {
																					@Override protected void invalidated() {
																					pseudoClassStateChanged(SHOWING_PSEUDOCLASS_STATE, get());
																					notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
																					}
																					
																					@Override
																					public Object getBean() {
																					return ChoiceBox.this;
																					}
																					
																					@Override
																					public String getName() {
																					return "showing";
																					}
																					};*/

	public final boolean isShowing() {
		return showing.getValue();
	}

	public final Property<Boolean> showingProperty() {
		return showing;
	}

	private void setShowing(boolean value) {
		// these events will not fire if the showing property is bound
		Event.fireEvent(this, value ? new Event(ComboBoxBase.ON_SHOWING) : new Event(ComboBoxBase.ON_HIDING));
		showing.setValue(value);
		Event.fireEvent(this, value ? new Event(ComboBoxBase.ON_SHOWN) : new Event(ComboBoxBase.ON_HIDDEN));
	}

	/**
	 * The items to display in the choice box. The selected item (as indicated in the
	 * selection model) must always be one of these items.
	 */
	private ObjectProperty<ObservableList<T>> items = new ObjectPropertyBase<ObservableList<T>>() {
		ObservableList<T> old;

		@Override
		protected void invalidated() {
			final ObservableList<T> newItems = get();
			if (old != newItems) {
				// Add and remove listeners
				if (old != null)
					old.removeListener(itemsListener);
				if (newItems != null)
					newItems.addListener(itemsListener);
				// Clear the selection model
				final SingleSelectionModel<T> sm = getSelectionModel();
				if (sm != null) {
					if (newItems != null && newItems.isEmpty()) {
						// RT-29433 - clear selection.
						sm.clearSelection();
					} else if (sm.getSelectedIndex() == -1 && sm.getSelectedItem() != null) {
						int newIndex = getItems().indexOf(sm.getSelectedItem());
						if (newIndex != -1) {
							sm.setSelectedIndex(newIndex);
						}
					} else
						sm.clearSelection();
				}
				//                if (sm != null) sm.setSelectedIndex(-1);
				// Save off the old items
				old = newItems;
			}
		}

		@Override
		public Object getBean() {
			return ChoiceBox.this;
		}

		@Override
		public String getName() {
			return "items";
		}
	};

	public final void setItems(ObservableList<T> value) {
		items.set(value);
	}

	public final ObservableList<T> getItems() {
		return items.get();
	}

	public final ObjectProperty<ObservableList<T>> itemsProperty() {
		return items;
	}

	private final ListChangeListener<T> itemsListener = c -> {
		final SingleSelectionModel<T> sm = getSelectionModel();
		if (sm != null) {
			if (getItems() == null || getItems().isEmpty()) {
				sm.clearSelection();
			} else {
				int newIndex = getItems().indexOf(sm.getSelectedItem());
				sm.setSelectedIndex(newIndex);
			}
		}
		if (sm != null) {

			// Look for the selected item as having been removed. If it has been,
			// then we need to clear the selection in the selection model.
			final T selectedItem = sm.getSelectedItem();
			while (c.next()) {
				if (selectedItem != null && c.getRemoved().contains(selectedItem)) {
					sm.clearSelection();
					break;
				}
			}
		}
	};

	/**
	 * Allows a way to specify how to represent objects in the items list. When
	 * a StringConverter is set, the object toString method is not called and
	 * instead its toString(object T) is called, passing the objects in the items list.
	 * This is useful when using domain objects in a ChoiceBox as this property
	 * allows for customization of the representation. Also, any of the pre-built
	 * Converters available in the {@link javafx.util.converter} package can be set.
	 * @since JavaFX 2.1
	 */
	public ObjectProperty<StringConverter<T>> converterProperty() {
		return converter;
	}

	private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this,
			"converter", null);

	public final void setConverter(StringConverter<T> value) {
		converterProperty().set(value);
	}

	public final StringConverter<T> getConverter() {
		return converterProperty().get();
	}

	/**
	 * The value of this ChoiceBox is defined as the selected item in the ChoiceBox
	 * selection model. The valueProperty is synchronized with the selectedItem.
	 * This property allows for bi-directional binding of external properties to the
	 * ChoiceBox and updates the selection model accordingly.
	 * @since JavaFX 2.1
	 */
	public ObjectProperty<T> valueProperty() {
		return value;
	}

	private ObjectProperty<T> value = new SimpleObjectProperty<T>(this, "value") {
		@Override
		protected void invalidated() {
			super.invalidated();
			fireEvent(new ActionEvent());
			// Update selection
			final SingleSelectionModel<T> sm = getSelectionModel();
			if (sm != null) {
				sm.select(super.getValue());
			}
			//notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
		}
	};

	public final void setValue(T value) {
		valueProperty().set(value);
	}

	public final T getValue() {
		return valueProperty().get();
	}

	// --- On Action
	/**
	 * The ChoiceBox action, which is invoked whenever the ChoiceBox
	 * {@link #valueProperty() value} property is changed. This
	 * may be due to the value property being programmatically changed or when the
	 * user selects an item in a popup menu.
	 *
	 * @since JavaFX 8u60
	 */
	public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
		return onAction;
	}

	public final void setOnAction(EventHandler<ActionEvent> value) {
		onActionProperty().set(value);
	}

	public final EventHandler<ActionEvent> getOnAction() {
		return onActionProperty().get();
	}

	private ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<EventHandler<ActionEvent>>() {
		@Override
		protected void invalidated() {
			setEventHandler(ActionEvent.ACTION, get());
		}

		@Override
		public Object getBean() {
			return ChoiceBox.this;
		}

		@Override
		public String getName() {
			return "onAction";
		}
	};

	// --- On Showing
	/**
	 * Called just prior to the {@code ChoiceBox} popup being shown.
	 * @since JavaFX 8u60
	 */
	public final ObjectProperty<EventHandler<Event>> onShowingProperty() {
		return onShowing;
	}

	public final void setOnShowing(EventHandler<Event> value) {
		onShowingProperty().set(value);
	}

	public final EventHandler<Event> getOnShowing() {
		return onShowingProperty().get();
	}

	private ObjectProperty<EventHandler<Event>> onShowing = new ObjectPropertyBase<EventHandler<Event>>() {
		@Override
		protected void invalidated() {
			setEventHandler(ON_SHOWING, get());
		}

		@Override
		public Object getBean() {
			return ChoiceBox.this;
		}

		@Override
		public String getName() {
			return "onShowing";
		}
	};

	// -- On Shown
	/**
	 * Called just after the {@link ChoiceBox} popup is shown.
	 * @since JavaFX 8u60
	 */
	public final ObjectProperty<EventHandler<Event>> onShownProperty() {
		return onShown;
	}

	public final void setOnShown(EventHandler<Event> value) {
		onShownProperty().set(value);
	}

	public final EventHandler<Event> getOnShown() {
		return onShownProperty().get();
	}

	private ObjectProperty<EventHandler<Event>> onShown = new ObjectPropertyBase<EventHandler<Event>>() {
		@Override
		protected void invalidated() {
			setEventHandler(ON_SHOWN, get());
		}

		@Override
		public Object getBean() {
			return ChoiceBox.this;
		}

		@Override
		public String getName() {
			return "onShown";
		}
	};

	// --- On Hiding
	/**
	 * Called just prior to the {@link ChoiceBox} popup being hidden.
	 * @since JavaFX 8u60
	 */
	public final ObjectProperty<EventHandler<Event>> onHidingProperty() {
		return onHiding;
	}

	public final void setOnHiding(EventHandler<Event> value) {
		onHidingProperty().set(value);
	}

	public final EventHandler<Event> getOnHiding() {
		return onHidingProperty().get();
	}

	private ObjectProperty<EventHandler<Event>> onHiding = new ObjectPropertyBase<EventHandler<Event>>() {
		@Override
		protected void invalidated() {
			setEventHandler(ON_HIDING, get());
		}

		@Override
		public Object getBean() {
			return ChoiceBox.this;
		}

		@Override
		public String getName() {
			return "onHiding";
		}
	};

	// --- On Hidden
	/**
	 * Called just after the {@link ChoiceBox} popup has been hidden.
	 * @since JavaFX 8u60
	 */
	public final ObjectProperty<EventHandler<Event>> onHiddenProperty() {
		return onHidden;
	}

	public final void setOnHidden(EventHandler<Event> value) {
		onHiddenProperty().set(value);
	}

	public final EventHandler<Event> getOnHidden() {
		return onHiddenProperty().get();
	}

	private ObjectProperty<EventHandler<Event>> onHidden = new ObjectPropertyBase<EventHandler<Event>>() {
		@Override
		protected void invalidated() {
			setEventHandler(ON_HIDDEN, get());
		}

		@Override
		public Object getBean() {
			return ChoiceBox.this;
		}

		@Override
		public String getName() {
			return "onHidden";
		}
	};

	/***************************************************************************
	 *                                                                         *
	 * Methods                                                                 *
	 *                                                                         *
	 **************************************************************************/

	/**
	 * Opens the list of choices.
	 */
	public void show() {
		if (!isDisabled())
			setShowing(true);
	}

	/**
	 * Closes the list of choices.
	 */
	public void hide() {
		setShowing(false);
	}

	/** {@inheritDoc} */
	/* @Override protected Skin<?> createDefaultSkin() {
	    return new ChoiceBoxSkin<T>(this);
	}*/

	/***************************************************************************
	 *                                                                         *
	 * Stylesheet Handling                                                     *
	 *                                                                         *
	 **************************************************************************/

	//private static final PseudoClass SHOWING_PSEUDOCLASS_STATE =
	//        PseudoClass.getPseudoClass("showing");

	// package for testing
	static class ChoiceBoxSelectionModel<T> extends SingleSelectionModel<T> {
		private final ChoiceBox<T> choiceBox;

		public ChoiceBoxSelectionModel(final ChoiceBox<T> cb) {
			if (cb == null) {
				throw new NullPointerException("ChoiceBox can not be null");
			}
			this.choiceBox = cb;

			/*
			 * The following two listeners are used in conjunction with
			 * SelectionModel.select(T obj) to allow for a developer to select
			 * an item that is not actually in the data model. When this occurs,
			 * we actively try to find an index that matches this object, going
			 * so far as to actually watch for all changes to the items list,
			 * rechecking each time.
			 */

			// watching for changes to the items list content
			final ListChangeListener<T> itemsContentObserver = c -> {
				if (choiceBox.getItems() == null || choiceBox.getItems().isEmpty()) {
					setSelectedIndex(-1);
				} else if (getSelectedIndex() == -1 && getSelectedItem() != null) {
					int newIndex = choiceBox.getItems().indexOf(getSelectedItem());
					if (newIndex != -1) {
						setSelectedIndex(newIndex);
					}
				}
			};
			if (this.choiceBox.getItems() != null) {
				this.choiceBox.getItems().addListener(itemsContentObserver);
			}

			// watching for changes to the items list
			ChangeListener<ObservableList<T>> itemsObserver = (valueModel, oldList, newList) -> {
				if (oldList != null) {
					oldList.removeListener(itemsContentObserver);
				}
				if (newList != null) {
					newList.addListener(itemsContentObserver);
				}
				setSelectedIndex(-1);
				if (getSelectedItem() != null) {
					int newIndex = choiceBox.getItems().indexOf(getSelectedItem());
					if (newIndex != -1) {
						setSelectedIndex(newIndex);
					}
				}
			};
			this.choiceBox.itemsProperty().addListener(itemsObserver);
		}

		// API Implementation
		@Override
		protected T getModelItem(int index) {
			final ObservableList<T> items = choiceBox.getItems();
			if (items == null)
				return null;
			if (index < 0 || index >= items.size())
				return null;
			return items.get(index);
		}

		@Override
		protected int getItemCount() {
			final ObservableList<T> items = choiceBox.getItems();
			return items == null ? 0 : items.size();
		}

		/**
		 * Selects the given row. Since the SingleSelectionModel can only support having
		 * a single row selected at a time, this also causes any previously selected
		 * row to be unselected.
		 * This method is overridden here so that we can move past a Separator
		 * in a ChoiceBox and select the next valid menuitem.
		 */
		@Override
		public void select(int index) {
			// this does not sound right, we should let the superclass handle it.
			final T value = getModelItem(index);
			/* if (value instanceof Separator) {
			    select(++index);
			} else {*/
			super.select(index);
			//}

			if (choiceBox.isShowing()) {
				choiceBox.hide();
			}
		}
	}

	/***************************************************************************
	 *                                                                         *
	 * Accessibility handling                                                  *
	 *                                                                         *
	 **************************************************************************/

	/* @Override
	public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
	    switch(attribute) {
	        case TEXT:
	            String accText = getAccessibleText();
	            if (accText != null && !accText.isEmpty()) return accText;
	
	            //let the skin first.
	            Object title = super.queryAccessibleAttribute(attribute, parameters);
	            if (title != null) return title;
	            StringConverter<T> converter = getConverter();
	            if (converter == null) {
	                return getValue() != null ? getValue().toString() : "";
	            }
	            return converter.toString(getValue());
	        case EXPANDED: return isShowing();
	        default: return super.queryAccessibleAttribute(attribute, parameters);
	    }
	}
	
	@Override
	public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
	    switch (action) {
	        case COLLAPSE: hide(); break;
	        case EXPAND: show(); break;
	        default: super.executeAccessibleAction(action); break;
	    }
	}*/

	static {
		JavaFxControlsRegistry.registerChoiceBox();
	}
}
