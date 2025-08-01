package org.immutables.fixture.with;

import org.junit.jupiter.api.Test;

import java.math.RoundingMode;
import java.util.Optional;

import static org.immutables.check.Checkers.check;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WithEnumsTest {
	private static final ImmutableWithEnums TEST_IMMUTABLE = ImmutableWithEnums.builder()
			.roundingMode(RoundingMode.CEILING).maybeRoundingMode(RoundingMode.HALF_DOWN)
			.nullableRoundingMode(RoundingMode.UNNECESSARY).build();
	private static final ImmutableWithEnums TEST_IMMUTABLE_WITH_NULLS = ImmutableWithEnums.builder()
			.roundingMode(RoundingMode.DOWN).maybeRoundingMode(Optional.empty()).nullableRoundingMode(null).build();

	@Test
	public void doNotAllowNullEnumValueInNonNullableWith() {
		assertThrows(NullPointerException.class, () -> TEST_IMMUTABLE.withRoundingMode(null));
	}

	@Test
	public void allowNullEnumValueInNullableWith() {
		ImmutableWithEnums withNullableRoundingMode = TEST_IMMUTABLE.withNullableRoundingMode(null);
		check(withNullableRoundingMode.getNullableRoundingMode()).isNull();
	}

	@Test
	public void withOnEqualEnumValueIsSameInstance() {
		check(TEST_IMMUTABLE.withRoundingMode(RoundingMode.CEILING)).same(TEST_IMMUTABLE);
		check(TEST_IMMUTABLE.withMaybeRoundingMode(RoundingMode.HALF_DOWN)).same(TEST_IMMUTABLE);
		check(TEST_IMMUTABLE.withNullableRoundingMode(RoundingMode.UNNECESSARY)).same(TEST_IMMUTABLE);
	}

	@Test
	public void withOnNullEnumValueIsSameInstance() {
		check(TEST_IMMUTABLE_WITH_NULLS.withNullableRoundingMode(null)).same(TEST_IMMUTABLE_WITH_NULLS);
		check(TEST_IMMUTABLE_WITH_NULLS.withMaybeRoundingMode(Optional.empty())).same(TEST_IMMUTABLE_WITH_NULLS);
	}

	@Test
	public void withOnDifferentEnumValueIsNotSameInstance() {
		check(TEST_IMMUTABLE.withRoundingMode(RoundingMode.FLOOR)).not().same(TEST_IMMUTABLE);
		check(TEST_IMMUTABLE.withNullableRoundingMode(RoundingMode.HALF_UP)).not().same(TEST_IMMUTABLE);
		check(TEST_IMMUTABLE.withMaybeRoundingMode(RoundingMode.CEILING)).not().same(TEST_IMMUTABLE);
	}

	@Test
	public void withOnNullEnumValueWithEnumValueIsNotSameInstance() {
		check(TEST_IMMUTABLE_WITH_NULLS.withNullableRoundingMode(RoundingMode.CEILING)).not()
				.same(TEST_IMMUTABLE_WITH_NULLS);
		check(TEST_IMMUTABLE_WITH_NULLS.withMaybeRoundingMode(RoundingMode.UNNECESSARY)).not()
				.same(TEST_IMMUTABLE_WITH_NULLS);
		check(TEST_IMMUTABLE_WITH_NULLS.withMaybeRoundingMode(Optional.of(RoundingMode.HALF_UP))).not()
				.same(TEST_IMMUTABLE_WITH_NULLS);
	}

	@Test
	public void withOnDifferentEnumValueIsEqualTo() {
		ImmutableWithEnums testImmutableWithFloorRoundingMode = ImmutableWithEnums.builder()
				.roundingMode(RoundingMode.FLOOR).maybeRoundingMode(RoundingMode.HALF_DOWN)
				.nullableRoundingMode(RoundingMode.UNNECESSARY).build();

		check(TEST_IMMUTABLE.withRoundingMode(RoundingMode.FLOOR)).is(testImmutableWithFloorRoundingMode);
	}

	@Test
	public void withOnNullEnumValueWithEnumValueIsEqualTo() {
		ImmutableWithEnums testImmutableWithFloorNullableRoundingMode = ImmutableWithEnums.builder()
				.roundingMode(RoundingMode.DOWN).maybeRoundingMode(Optional.empty())
				.nullableRoundingMode(RoundingMode.FLOOR).build();

		check(TEST_IMMUTABLE_WITH_NULLS.withNullableRoundingMode(RoundingMode.FLOOR))
				.is(testImmutableWithFloorNullableRoundingMode);
	}

	@Test
	public void withOnOptionalEnumValueWithEnumValueIsEqualTo() {
		ImmutableWithEnums testImmutableWithHalfDownMaybeRoundingMode = ImmutableWithEnums.builder()
				.roundingMode(RoundingMode.DOWN).maybeRoundingMode(RoundingMode.HALF_DOWN).nullableRoundingMode(null)
				.build();

		check(TEST_IMMUTABLE_WITH_NULLS.withMaybeRoundingMode(RoundingMode.HALF_DOWN))
				.is(testImmutableWithHalfDownMaybeRoundingMode);
	}

	@Test
	public void withOnOptionalEnumValueWithEmptyOptionalIsEqualTo() {
		ImmutableWithEnums testImmutableWithEmptyMaybeRoundingMode = ImmutableWithEnums.builder()
				.roundingMode(RoundingMode.CEILING).maybeRoundingMode(Optional.empty())
				.nullableRoundingMode(RoundingMode.UNNECESSARY).build();

		check(TEST_IMMUTABLE.withMaybeRoundingMode(Optional.empty())).is(testImmutableWithEmptyMaybeRoundingMode);
	}
}
