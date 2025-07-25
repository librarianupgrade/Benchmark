package io.github.zeroone3010.yahueapi.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zeroone3010.yahueapi.HueApiException;
import io.github.zeroone3010.yahueapi.v2.domain.ButtonResource;
import io.github.zeroone3010.yahueapi.v2.domain.ButtonResourceRoot;
import io.github.zeroone3010.yahueapi.v2.domain.DeviceResource;
import io.github.zeroone3010.yahueapi.v2.domain.ResourceIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.zeroone3010.yahueapi.v2.domain.ResourceType.BUTTON;
import static java.util.stream.Collectors.toMap;

public class SwitchFactory {

	private final Hue hue;
	private final ObjectMapper objectMapper;

	private static final Predicate<ResourceIdentifier> BUTTON_FILTER = s -> BUTTON == s.getResourceType();

	public SwitchFactory(final Hue hue, final ObjectMapper objectMapper) {
		this.hue = hue;
		this.objectMapper = objectMapper;
	}

	public SwitchImpl buildSwitch(final DeviceResource resource, final Map<UUID, ButtonResource> allButtons) {
		if (resource.getServices().stream().noneMatch(BUTTON_FILTER)) {
			return null;
		}
		final Map<UUID, Button> buttons = resource.getServices().stream().filter(BUTTON_FILTER)
				.map(ResourceIdentifier::getResourceId).map(allButtons::get)
				.map(button -> new ButtonImpl(createButtonStateProvider(button.getId()), button))
				.collect(toMap(ButtonImpl::getId, button -> button));
		final SwitchImpl result = new SwitchImpl(resource.getId(), buttons, resource.getMetadata().getName());
		buttons.values().forEach(button -> ((ButtonImpl) button).setOwner(result));
		return result;
	}

	private Supplier<ButtonResource> createButtonStateProvider(final UUID buttonId) {
		return () -> {
			// TODO: Implement the caching part again
			//      if (hue.isCaching()) {
			//        return hue.getRaw().getSensors().get(id).getState();
			//      }
			try (final InputStream inputStream = hue.getUrlConnection("/button/" + buttonId.toString())
					.getInputStream()) {
				return objectMapper.readValue(inputStream, ButtonResourceRoot.class).getData().get(0);
			} catch (final IOException e) {
				throw new HueApiException(e);
			}
		};
	}
}
