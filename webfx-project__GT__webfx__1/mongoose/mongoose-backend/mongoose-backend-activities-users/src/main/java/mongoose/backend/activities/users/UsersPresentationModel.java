package mongoose.backend.activities.users;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import mongoose.client.activity.eventdependent.EventDependentGenericTablePresentationModel;
import mongoose.shared.entities.Person;
import webfx.extras.visual.VisualResult;
import webfx.extras.visual.VisualSelection;
import webfx.framework.shared.orm.dql.DqlStatement;
import webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualResultProperty;
import webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasGroupVisualSelectionProperty;
import webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualResultProperty;
import webfx.framework.client.orm.reactive.mapping.entities_to_visual.conventions.HasMasterVisualSelectionProperty;
import webfx.framework.client.orm.reactive.dql.statement.conventions.*;
import webfx.framework.shared.orm.expression.builder.ReferenceResolver;

/**
 * @author Bruno Salmon
 */
final class UsersPresentationModel extends EventDependentGenericTablePresentationModel
		implements HasConditionDqlStatementProperty, HasGroupDqlStatementProperty, HasColumnsDqlStatementProperty,
		HasGroupVisualResultProperty, HasGroupVisualSelectionProperty, HasSelectedGroupProperty<Person>,
		HasSelectedGroupConditionDqlStatementProperty, HasSelectedGroupReferenceResolver, HasMasterVisualResultProperty,
		HasMasterVisualSelectionProperty, HasSelectedMasterProperty<Person> {

	private final ObjectProperty<DqlStatement> conditionDqlStatementProperty = new SimpleObjectProperty<>();

	@Override
	public ObjectProperty<DqlStatement> conditionDqlStatementProperty() {
		return conditionDqlStatementProperty;
	}

	private final ObjectProperty<DqlStatement> groupDqlStatementProperty = new SimpleObjectProperty<>();

	@Override
	public ObjectProperty<DqlStatement> groupDqlStatementProperty() {
		return groupDqlStatementProperty;
	}

	private final ObjectProperty<DqlStatement> columnsDqlStatementProperty = new SimpleObjectProperty<>();

	@Override
	public ObjectProperty<DqlStatement> columnsDqlStatementProperty() {
		return columnsDqlStatementProperty;
	}

	private final ObjectProperty<VisualResult> groupVisualResultProperty = new SimpleObjectProperty<>();

	@Override
	public ObjectProperty<VisualResult> groupVisualResultProperty() {
		return groupVisualResultProperty;
	}

	private final ObjectProperty<VisualSelection> groupVisualSelectionProperty = new SimpleObjectProperty<>();

	@Override
	public ObjectProperty<VisualSelection> groupVisualSelectionProperty() {
		return groupVisualSelectionProperty;
	}

	private final ObjectProperty<Person> selectedGroupProperty = new SimpleObjectProperty<>();

	@Override
	public ObjectProperty<Person> selectedGroupProperty() {
		return selectedGroupProperty;
	}

	private final ObjectProperty<DqlStatement> selectedGroupConditionDqlStatementProperty = new SimpleObjectProperty<>();

	@Override
	public ObjectProperty<DqlStatement> selectedGroupConditionDqlStatementProperty() {
		return selectedGroupConditionDqlStatementProperty;
	}

	private ReferenceResolver selectedGroupReferenceResolver;

	@Override
	public ReferenceResolver getSelectedGroupReferenceResolver() {
		return selectedGroupReferenceResolver;
	}

	@Override
	public void setSelectedGroupReferenceResolver(ReferenceResolver referenceResolver) {
		this.selectedGroupReferenceResolver = referenceResolver;
	}

	private final ObjectProperty<VisualResult> masterVisualResultProperty = new SimpleObjectProperty<>();

	@Override
	public ObjectProperty<VisualResult> masterVisualResultProperty() {
		return masterVisualResultProperty;
	}

	private final ObjectProperty<VisualSelection> masterVisualSelectionProperty = new SimpleObjectProperty<>();

	@Override
	public ObjectProperty<VisualSelection> masterVisualSelectionProperty() {
		return masterVisualSelectionProperty;
	}

	private final ObjectProperty<Person> selectedMasterProperty = new SimpleObjectProperty<>();

	@Override
	public ObjectProperty<Person> selectedMasterProperty() {
		return selectedMasterProperty;
	}
}
