package com.github.davidmoten.rx.jdbc.tuple;

import static com.github.davidmoten.rx.jdbc.Util.getObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import rx.util.functions.Func1;

public final class Tuples {

	private Tuples() {
		// prevent instantiation.
	}

	public static final <T> Func1<ResultSet, T> single(final Class<T> cls) {
		return new Func1<ResultSet, T>() {

			@SuppressWarnings("unchecked")
			@Override
			public T call(ResultSet rs) {
				return (T) getObject(rs, cls, 1);
			}

		};
	}

	public static final <T1, T2> Func1<ResultSet, Tuple2<T1, T2>> tuple(final Class<T1> cls1, final Class<T2> cls2) {
		return new Func1<ResultSet, Tuple2<T1, T2>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Tuple2<T1, T2> call(ResultSet rs) {
				return new Tuple2<T1, T2>((T1) getObject(rs, cls1, 1), (T2) getObject(rs, cls2, 2));
			}
		};
	}

	public static final <T1, T2, T3> Func1<ResultSet, Tuple3<T1, T2, T3>> tuple(final Class<T1> cls1,
			final Class<T2> cls2, final Class<T3> cls3) {
		return new Func1<ResultSet, Tuple3<T1, T2, T3>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Tuple3<T1, T2, T3> call(ResultSet rs) {
				return new Tuple3<T1, T2, T3>((T1) getObject(rs, cls1, 1), (T2) getObject(rs, cls2, 2),
						(T3) getObject(rs, cls3, 3));
			}
		};
	}

	public static final <T1, T2, T3, T4> Func1<ResultSet, Tuple4<T1, T2, T3, T4>> tuple(final Class<T1> cls1,
			final Class<T2> cls2, final Class<T3> cls3, final Class<T4> cls4) {
		return new Func1<ResultSet, Tuple4<T1, T2, T3, T4>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Tuple4<T1, T2, T3, T4> call(ResultSet rs) {
				return new Tuple4<T1, T2, T3, T4>((T1) getObject(rs, cls1, 1), (T2) getObject(rs, cls2, 2),
						(T3) getObject(rs, cls3, 3), (T4) getObject(rs, cls4, 4));
			}
		};
	}

	public static final <T1, T2, T3, T4, T5> Func1<ResultSet, Tuple5<T1, T2, T3, T4, T5>> tuple(final Class<T1> cls1,
			final Class<T2> cls2, final Class<T3> cls3, final Class<T4> cls4, final Class<T5> cls5) {
		return new Func1<ResultSet, Tuple5<T1, T2, T3, T4, T5>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Tuple5<T1, T2, T3, T4, T5> call(ResultSet rs) {
				return new Tuple5<T1, T2, T3, T4, T5>((T1) getObject(rs, cls1, 1), (T2) getObject(rs, cls2, 2),
						(T3) getObject(rs, cls3, 3), (T4) getObject(rs, cls4, 4), (T5) getObject(rs, cls5, 5));
			}
		};
	}

	public static final <T1, T2, T3, T4, T5, T6> Func1<ResultSet, Tuple6<T1, T2, T3, T4, T5, T6>> tuple(
			final Class<T1> cls1, final Class<T2> cls2, final Class<T3> cls3, final Class<T4> cls4,
			final Class<T5> cls5, final Class<T6> cls6) {

		return new Func1<ResultSet, Tuple6<T1, T2, T3, T4, T5, T6>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Tuple6<T1, T2, T3, T4, T5, T6> call(ResultSet rs) {
				return new Tuple6<T1, T2, T3, T4, T5, T6>((T1) getObject(rs, cls1, 1), (T2) getObject(rs, cls2, 2),
						(T3) getObject(rs, cls3, 3), (T4) getObject(rs, cls4, 4), (T5) getObject(rs, cls5, 5),
						(T6) getObject(rs, cls6, 6));
			}
		};
	}

	public static final <T1, T2, T3, T4, T5, T6, T7> Func1<ResultSet, Tuple7<T1, T2, T3, T4, T5, T6, T7>> tuple(
			final Class<T1> cls1, final Class<T2> cls2, final Class<T3> cls3, final Class<T4> cls4,
			final Class<T5> cls5, final Class<T6> cls6, final Class<T7> cls7) {

		return new Func1<ResultSet, Tuple7<T1, T2, T3, T4, T5, T6, T7>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Tuple7<T1, T2, T3, T4, T5, T6, T7> call(ResultSet rs) {
				return new Tuple7<T1, T2, T3, T4, T5, T6, T7>((T1) getObject(rs, cls1, 1), (T2) getObject(rs, cls2, 2),
						(T3) getObject(rs, cls3, 3), (T4) getObject(rs, cls4, 4), (T5) getObject(rs, cls5, 5),
						(T6) getObject(rs, cls6, 6), (T7) getObject(rs, cls7, 7));
			}
		};
	}

	public static final <T> Func1<ResultSet, TupleN<T>> tupleN(final Class<T> cls) {
		return new Func1<ResultSet, TupleN<T>>() {
			@Override
			public TupleN<T> call(ResultSet rs) {
				return toTupleN(cls, rs);
			}
		};
	}

	@SuppressWarnings("unchecked")
	private static <T> TupleN<T> toTupleN(final Class<T> cls, ResultSet rs) {
		try {
			int n = rs.getMetaData().getColumnCount();
			List<T> list = new ArrayList<T>();
			for (int i = 1; i <= n; i++) {
				list.add((T) getObject(rs, cls, i));
			}
			return new TupleN<T>(list);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
