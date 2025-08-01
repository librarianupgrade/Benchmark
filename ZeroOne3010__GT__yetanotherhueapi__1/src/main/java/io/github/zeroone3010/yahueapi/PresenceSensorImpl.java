package io.github.zeroone3010.yahueapi;

import io.github.zeroone3010.yahueapi.domain.SensorDto;

import java.net.URL;
import java.util.Map;
import java.util.function.Supplier;

final class PresenceSensorImpl extends BasicSensor implements PresenceSensor {

	PresenceSensorImpl(final String id, final SensorDto sensor, final URL url,
			final Supplier<Map<String, Object>> stateProvider) {
		super(id, sensor, url, stateProvider);
	}

	@Override
	public String toString() {
		return "PresenceSensor{" + "id='" + super.id + '\'' + ", name='" + super.name + '\'' + ", type=" + super.type
				+ '}';
	}

	@Override
	public boolean isPresence() {
		return readStateValue("presence", Boolean.class);
	}
}
