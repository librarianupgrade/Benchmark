package com.github.davidmoten.rx.internal.operators;

import static com.github.davidmoten.rx.internal.operators.OperatorFromTransformer.toOperator;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.github.davidmoten.rx.Functions;

import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public class OperatorFromTransformerTest {

	private static final int AWAIT_SECONDS = 10;

	@Test
	public void testUnsubscribeFromAsynchronousSource() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		Observable
				// every 100ms
				.interval(100, TimeUnit.MILLISECONDS)
				// detect unsubscribe
				.doOnUnsubscribe(countDown(latch))
				// use toOperator
				.lift(toOperator(Functions.<Observable<Long>>identity())).take(1).first()
				// block and get result
				.toBlocking().single();
		// wait for expected unsubscription
		assertTrue(latch.await(AWAIT_SECONDS, TimeUnit.SECONDS));

	}

	@Test
	public void testUnsubscribeFromSynchronousSource() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		PublishSubject<Integer> subject = PublishSubject.create();
		subject
				// detect unsubscribe
				.doOnUnsubscribe(countDown(latch))
				// use toOperator
				.lift(toOperator(Functions.<Observable<Integer>>identity()))
				// get first only
				.take(1)
				// subscribe and ignore events
				.subscribe();
		subject.onNext(1);
		// should have unsubscribed because of take(1)
		assertTrue(latch.await(AWAIT_SECONDS, TimeUnit.SECONDS));
	}

	public static Action0 countDown(final CountDownLatch latch) {
		return new Action0() {

			@Override
			public void call() {
				latch.countDown();
			}
		};
	}

	@Test
	public void testMultipleNonSimultaeousSubscriptions() {
		Observable<Integer> sequence = Observable.range(1, 3)
				.lift(toOperator(Functions.<Observable<Integer>>identity()));
		assertEquals(asList(1, 2, 3), sequence.toList().toBlocking().single());
		assertEquals(asList(1, 2, 3), sequence.toList().toBlocking().single());
	}

	@Test
	public void testMultipleSimultaneousSubscriptions() {
		PublishSubject<Integer> subject = PublishSubject.create();
		Recorder recorder1 = new Recorder();
		Recorder recorder2 = new Recorder();
		subject.subscribe(recorder1);
		subject.subscribe(recorder2);
		subject.onNext(1);
		assertEquals(asList(1), recorder1.list());
		assertEquals(asList(1), recorder2.list());
		subject.onNext(2);
		assertEquals(asList(1, 2), recorder1.list());
		assertEquals(asList(1, 2), recorder2.list());
		assertFalse(recorder1.isCompleted());
		assertFalse(recorder2.isCompleted());
		subject.onCompleted();
		assertTrue(recorder1.isCompleted());
		assertTrue(recorder2.isCompleted());
	}

	@Test
	public void testErrorsPassedThroughToOperator() {
		PublishSubject<Integer> subject = PublishSubject.create();
		Recorder recorder1 = new Recorder();
		Recorder recorder2 = new Recorder();
		subject.subscribe(recorder1);
		subject.subscribe(recorder2);
		subject.onNext(1);
		assertEquals(asList(1), recorder1.list());
		assertEquals(asList(1), recorder2.list());
		subject.onNext(2);
		assertEquals(asList(1, 2), recorder1.list());
		assertEquals(asList(1, 2), recorder2.list());
		Exception e = new Exception("boo");
		assertTrue(recorder1.errors().isEmpty());
		assertTrue(recorder2.errors().isEmpty());
		subject.onError(e);
		assertEquals(asList(e), recorder1.errors());
		assertEquals(asList(e), recorder2.errors());
	}

	private static class Recorder implements Observer<Integer> {

		private final List<Throwable> errors = new ArrayList<Throwable>();
		private final List<Integer> list = new ArrayList<Integer>();
		private boolean completed = false;

		boolean isCompleted() {
			return completed;
		}

		List<Throwable> errors() {
			return errors;
		}

		List<Integer> list() {
			return list;
		}

		@Override
		public void onCompleted() {
			completed = true;
		}

		@Override
		public void onError(Throwable e) {
			errors.add(e);
		}

		@Override
		public void onNext(Integer t) {
			list.add(t);
		}

	}

	@Test
	public void testBackpressure() {
		TestSubscriber<Integer> ts = TestSubscriber.create(1);
		Observable.
		//
				range(1, 1000)
				// use toOperator
				.lift(toOperator(Functions.<Observable<Integer>>identity()))
				// block and get result
				.subscribe(ts);
		// make sure only one value has arrived
		ts.assertValueCount(1);
		ts.requestMore(2);
		ts.assertValueCount(3);
		ts.unsubscribe();
	}

	@Test
	public void testBackpressureWhereOperatorRequestsDontMatchOneToOne() {
		TestSubscriber<List<Integer>> ts = TestSubscriber.create(1);
		Observable.
		//
				range(1, 1000)
				// use toOperator
				.lift(toOperator(new Func1<Observable<Integer>, Observable<List<Integer>>>() {

					@Override
					public Observable<List<Integer>> call(Observable<Integer> o) {
						return o.toList();
					}
				}))
				// block and get result
				.subscribe(ts);
		// make sure only one value has arrived
		ts.assertValueCount(1);
		ts.requestMore(1);
		ts.assertCompleted();
		ts.assertValueCount(1);
	}
}
