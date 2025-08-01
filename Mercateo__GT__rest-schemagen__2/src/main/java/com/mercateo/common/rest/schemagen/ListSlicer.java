package com.mercateo.common.rest.schemagen;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mercateo.common.rest.schemagen.types.PaginatedList;

public class ListSlicer {

	private static final int DEFAULT_MIN_LIMIT = 5;

	private static final int DEFAULT_MAX_LIMIT = 2000;

	private static final int DEFAULT_OFFSET = 0;

	private final int offset;

	private final int limit;

	public <E> PaginatedList<E> createSliceOf(List<E> list) {
		List<E> slicedList = list.stream().skip(offset).limit(limit).collect(Collectors.toList());
		return new PaginatedList<>(list.size(), offset, limit, slicedList);
	}

	private ListSlicer(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
	}

	public static ListSlicerBuilder withInterval(int minLimit, int maxLimit) {
		return new ListSlicerBuilder(minLimit, maxLimit, DEFAULT_OFFSET);
	}

	public static ListSlicerBuilder withDefaultInterval() {
		return withInterval(DEFAULT_MIN_LIMIT, DEFAULT_MAX_LIMIT);
	}

	public static SliceDefaults createDefaults(final int defaultLimit, final int defaultOffset) {
		return new SliceDefaults(defaultLimit, defaultOffset);
	}

	public static class ListSlicerBuilder {

		private final int minLimit;

		private final int maxLimit;

		private final SliceDefaults slicerDefault;

		private ListSlicerBuilder(int minLimit, int maxLimit, int defaultOffset) {
			this.minLimit = minLimit;
			this.maxLimit = maxLimit;
			this.slicerDefault = new SliceDefaults(maxLimit, defaultOffset);
		}

		public ListSlicer create(Integer offset, Integer limit) {
			return new ListSlicer(getConstrainedOffset(offset), getConstrainedLimit(limit));
		}

		@SuppressWarnings("boxing")
		private int getConstrainedOffset(Integer offset) {
			return slicerDefault.determineOffset(offset, o -> Math.max(o.intValue(), 0));
		}

		@SuppressWarnings("boxing")
		private int getConstrainedLimit(Integer limit) {
			return slicerDefault.determineLimit(limit, l -> Math.min(Math.max(l.intValue(), minLimit), maxLimit));
		}
	}

	public static class SliceDefaults {

		private final int defaultLimit;

		private final int defaultOffset;

		private SliceDefaults(final int defaultLimit, final int defaultOffset) {
			this.defaultLimit = defaultLimit;
			this.defaultOffset = defaultOffset;
		}

		public int determineOffset(Integer offset) {
			return determineOffset(offset, Function.<Integer>identity());
		}

		@SuppressWarnings({ "unboxing", "boxing" })
		public int determineOffset(Integer offset, Function<Integer, Integer> function) {
			return offset == null ? defaultOffset : function.apply(offset);
		}

		public int determineLimit(Integer limit) {
			return determineLimit(limit, Function.<Integer>identity());
		}

		@SuppressWarnings({ "unboxing", "boxing" })
		public int determineLimit(Integer limit, Function<Integer, Integer> function) {
			return limit == null ? defaultLimit : function.apply(limit);
		}
	}

	@Override
	public String toString() {
		return "ListSlicer [offset=" + offset + ", limit=" + limit + "]";
	}
}
