package com.oneandone.snmpman.configuration.modifier;

import com.oneandone.snmpman.configuration.type.ModifierProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.smi.Integer32;

import java.util.Optional;

/** This modifier instance modifies {@link Integer32} variables. */
@Slf4j
public class Integer32Modifier implements VariableModifier<Integer32> {

	/** The minimum allowed number for the resulting modified variable. */
	@Getter
	private Integer minimum;

	/** The maximum allowed number for the resulting modified variable. */
	@Getter
	private Integer maximum;

	/** The minimal step by which a variable will be incremented. */
	@Getter
	private Integer minimumStep;

	/** The maximal step by which a variable will be incremented. */
	@Getter
	private Integer maximumStep;

	@Override
	public void init(final ModifierProperties properties) {
		this.minimum = Optional.ofNullable(properties.getInteger("minimum")).orElse(Integer.MIN_VALUE);
		this.maximum = Optional.ofNullable(properties.getInteger("maximum")).orElse(Integer.MAX_VALUE);

		this.minimumStep = Optional.ofNullable(properties.getInteger("minimumStep")).orElse(-1);
		this.maximumStep = Optional.ofNullable(properties.getInteger("maximumStep")).orElse(1);
	}

	/**
	 * Increments the current value by a random number between the minimum and maximum step.
	 * <p>
	 * An overflow can occur and will be considered in the minimum and maximum interval.
	 *
	 * @param currentValue the current value to modify
	 * @param minimum      {@link #minimum}
	 * @param maximum      {@link #maximum}
	 * @param minimumStep  {@link #minimumStep}
	 * @param maximumStep  {@link #maximumStep}
	 * @return the modified variable value
	 */
	protected int modify(final int currentValue, final int minimum, final int maximum, final int minimumStep,
			final int maximumStep) {
		int currentValidValue = currentValue;
		if (currentValue < minimum || currentValue > maximum) {
			currentValidValue = minimum;
		}
		int step = (int) (Math.round(Math.random() * (maximumStep - minimumStep)) + minimumStep);

		int stepUntilMaximum = maximum - currentValidValue;
		int newValue;
		if (Math.abs(step) > Math.abs(stepUntilMaximum)) {
			newValue = minimum + (step - stepUntilMaximum - 1);
		} else {
			newValue = currentValidValue + step;
		}

		if (newValue < minimum) {
			newValue = minimum;
		} else if (newValue > maximum) {
			newValue = maximum;
		}

		return newValue;
	}

	@Override
	public Integer32 modify(final Integer32 variable) {
		final int newValue = this.modify(variable.getValue(), minimum, maximum, minimumStep, maximumStep);
		log.trace("Counter32 variable {} will be tuned to {}", variable.getValue(), newValue);
		return new Integer32(newValue);
	}
}
