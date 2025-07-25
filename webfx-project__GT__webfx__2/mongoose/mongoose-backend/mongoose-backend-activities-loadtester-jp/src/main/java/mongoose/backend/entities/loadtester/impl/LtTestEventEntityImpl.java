package mongoose.backend.entities.loadtester.impl;

import mongoose.backend.activities.loadtester.drive.listener.EventType;
import mongoose.backend.entities.loadtester.LtTestEventEntity;
import mongoose.backend.entities.loadtester.LtTestSet;
import webfx.framework.shared.orm.entity.EntityId;
import webfx.framework.shared.orm.entity.EntityStore;
import webfx.framework.shared.orm.entity.impl.DynamicEntity;
import webfx.framework.shared.orm.entity.impl.EntityFactoryProviderImpl;

import java.time.Instant;

/**
 * @author Jean-Pierre Alonso.
 */
public final class LtTestEventEntityImpl extends DynamicEntity implements LtTestEventEntity {

	public LtTestEventEntityImpl(EntityId id, EntityStore store) {
		super(id, store);
	}

	@Override
	public LtTestSet getLtTestSet() {
		return getForeignEntity("ltTestSet");
	}

	@Override
	public void setLtTestSet(Object ltTestSet) {
		setForeignField("ltTestSet", ltTestSet);
	}

	@Override
	public Instant getEventTime() {
		return (Instant) getFieldValue("eventTime");
	}

	@Override
	public void setEventTime(Instant eventTime) {
		setFieldValue("eventTime", eventTime);
	}

	@Override
	public EventType getType() {
		return EventType.values()[getIntegerFieldValue("type")];
	}

	@Override
	public void setType(EventType type) {
		setFieldValue("type", type.ordinal());
	}

	@Override
	public Integer getVal() {
		return getIntegerFieldValue("value");
	}

	@Override
	public void setVal(Integer val) {
		setFieldValue("value", val);
	}

	public static final class ProvidedFactory extends EntityFactoryProviderImpl<LtTestEventEntity> {
		public ProvidedFactory() {
			super(LtTestEventEntity.class, LtTestEventEntityImpl::new);
		}
	}
}
