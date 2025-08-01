package javafx.scene.control;

import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.collections.VetoableListDecorator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.HashMap;
import java.util.List;

/**
 * A class which contains a reference to all {@code Toggles} whose
 * {@code selected} variables should be managed such that only a single
 * <code>{@link Toggle}</code> within the {@code ToggleGroup} may be selected at
 * any one time.
 * <p>
 * Generally {@code ToggleGroups} are managed automatically simply by specifying
 * the name of a {@code ToggleGroup} on the <code>{@link Toggle}</code>, but in
 * some situations it is desirable to explicitly manage which
 * {@code ToggleGroup} is used by <code>{@link Toggle Toggles}</code>.
 * </p>
 * @since JavaFX 2.0
 */
public class ToggleGroup {

	/**
	 * Creates a default ToggleGroup instance.
	 */
	public ToggleGroup() {

	}

	/**
	 * The list of toggles within the ToggleGroup.
	 */
	public final ObservableList<Toggle> getToggles() {
		return toggles;
	}

	private final ObservableList<Toggle> toggles = new VetoableListDecorator<Toggle>(
			new TrackableObservableList<Toggle>() {
				@Override
				protected void onChanged(ListChangeListener.Change<Toggle> c) {
					while (c.next()) {
						// Look through the removed toggles, and if any of them was the
						// one and only selected toggle, then we will clear the selected
						// toggle property.
						for (Toggle t : c.getRemoved()) {
							if (t.isSelected()) {
								selectToggle(null);
							}
						}

						// A Toggle can only be in one group at any one time. If the
						// group is changed, then the toggle is removed from the old group prior to
						// being added to the new group.
						for (Toggle t : c.getAddedSubList()) {
							if (!ToggleGroup.this.equals(t.getToggleGroup())) {
								if (t.getToggleGroup() != null) {
									t.getToggleGroup().getToggles().remove(t);
								}
								t.setToggleGroup(ToggleGroup.this);
							}
						}

						// Look through all the added toggles and the very first selected
						// toggle we encounter will become the one we make the selected
						// toggle for this group.
						for (Toggle t : c.getAddedSubList()) {
							if (t.isSelected()) {
								selectToggle(t);
								break;
							}
						}
					}
				}
			}) {
		@Override
		protected void onProposedChange(List<Toggle> toBeAdded, int... indexes) {
			for (Toggle t : toBeAdded) {
				if (indexes[0] == 0 && indexes[1] == size()) {
					// we don't need to check for duplicates because this is a setAll.
					break;
				}
				if (toggles.contains(t)) {
					throw new IllegalArgumentException("Duplicate toggles are not allow in a ToggleGroup.");
				}
			}
		}
	};

	private final ObjectProperty<Toggle> selectedToggle = new SimpleObjectProperty<Toggle>() {
		// Note: "set" is really what I want here. If the selectedToggle property
		// is bound, then this whole chunk of code is bypassed, which is exactly
		// what I want to do.
		@Override
		public void set(final Toggle newSelectedToggle) {
			if (isBound()) {
				throw new java.lang.RuntimeException("A bound value cannot be set.");
			}
			final Toggle old = get();
			if (old == newSelectedToggle) {
				return;
			}
			if (setSelected(newSelectedToggle, true)
					|| (newSelectedToggle != null && newSelectedToggle.getToggleGroup() == ToggleGroup.this)
					|| (newSelectedToggle == null)) {
				if (old == null || old.getToggleGroup() == ToggleGroup.this || !old.isSelected()) {
					setSelected(old, false);
				}
				super.set(newSelectedToggle);
			}
		}
	};

	/**
	 * Selects the toggle.
	 *
	 * @param value The {@code Toggle} that is to be selected.
	 */
	// Note that since selectedToggle is a read-only property, the selectToggle method is some
	// other method than setSelectedToggle, even though it is in essence doing the same thing
	public final void selectToggle(Toggle value) {
		selectedToggle.setValue(value);
	}

	/**
	 * Gets the selected {@code Toggle}.
	 * @return Toggle The selected toggle.
	 */
	public final Toggle getSelectedToggle() {
		return selectedToggle.getValue();
	}

	/**
	 * The selected toggle.
	 */
	public final ReadOnlyObjectProperty<Toggle> selectedToggleProperty() {
		return selectedToggle;
	}

	private boolean setSelected(Toggle toggle, boolean selected) {
		if (toggle != null && toggle.getToggleGroup() == this && !toggle.selectedProperty().isBound()) {
			toggle.setSelected(selected);
			return true;
		}
		return false;
	}

	// Clear the selected toggle only if there are no other toggles selected.
	final void clearSelectedToggle() {
		if (!selectedToggle.getValue().isSelected()) {
			for (Toggle toggle : getToggles()) {
				if (toggle.isSelected()) {
					return;
				}
			}
		}
		selectedToggle.setValue(null);
	}

	/*************************************************************************
	 *                                                                        *
	 *                                                                        *
	 *                                                                        *
	 *************************************************************************/

	private static final Object USER_DATA_KEY = new Object();
	// A map containing a set of properties for this scene
	private ObservableMap<Object, Object> properties;

	/**
	 * Returns an observable map of properties on this node for use primarily
	 * by application developers.
	 *
	 * @return an observable map of properties on this node for use primarily
	 * by application developers
	 *
	 * @since JavaFX 8u40
	 */
	public final ObservableMap<Object, Object> getProperties() {
		if (properties == null) {
			properties = FXCollections.observableMap(new HashMap<Object, Object>());
		}
		return properties;
	}

	/**
	 * Tests if ToggleGroup has properties.
	 * @return true if node has properties.
	 *
	 * @since JavaFX 8u40
	 */
	public boolean hasProperties() {
		return properties != null && !properties.isEmpty();
	}

	/**
	 * Convenience method for setting a single Object property that can be
	 * retrieved at a later date. This is functionally equivalent to calling
	 * the getProperties().put(Object key, Object value) method. This can later
	 * be retrieved by calling {@link ToggleGroup#getUserData()}.
	 *
	 * @param value The value to be stored - this can later be retrieved by calling
	 *          {@link ToggleGroup#getUserData()}.
	 *
	 * @since JavaFX 8u40
	 */
	public void setUserData(Object value) {
		getProperties().put(USER_DATA_KEY, value);
	}

	/**
	 * Returns a previously set Object property, or null if no such property
	 * has been set using the {@link ToggleGroup#setUserData(java.lang.Object)} method.
	 *
	 * @return The Object that was previously set, or null if no property
	 *          has been set or if null was set.
	 *
	 * @since JavaFX 8u40
	 */
	public Object getUserData() {
		return getProperties().get(USER_DATA_KEY);
	}
}
