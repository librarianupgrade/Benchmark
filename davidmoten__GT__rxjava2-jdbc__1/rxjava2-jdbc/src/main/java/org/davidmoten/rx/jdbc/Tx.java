package org.davidmoten.rx.jdbc;

import java.sql.Connection;

import io.reactivex.Flowable;
import io.reactivex.Notification;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public interface Tx<T> extends TxWithoutValue {

	boolean isValue();

	T value();

	static <T> Predicate<Tx<T>> valuesOnly() {
		return tx -> tx.isValue();
	}

	static <T> Function<Tx<T>, Flowable<T>> flattenToValuesOnly() {
		return tx -> {
			if (tx.isValue()) {
				return Flowable.just(tx.value());
			} else if (tx.isComplete()) {
				return Flowable.empty();
			} else {
				return Flowable.error(tx.throwable());
			}
		};
	}

	static <T> Function<Tx<T>, T> toValue() {
		return tx -> tx.value();
	}

	static <T> Flowable<Tx<T>> toTx(Notification<T> n, Connection con, Database db) {
		if (n.isOnComplete())
			return Flowable.just(new TxImpl<T>(con, null, null, true, db));
		else if (n.isOnNext())
			return Flowable.just(new TxImpl<T>(con, n.getValue(), null, false, db));
		else
			return Flowable.error(n.getError());
	}
}