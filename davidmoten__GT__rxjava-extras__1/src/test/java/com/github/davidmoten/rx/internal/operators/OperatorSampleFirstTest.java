package com.github.davidmoten.rx.internal.operators;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.github.davidmoten.rx.Transformers;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;

public class OperatorSampleFirstTest {

	private TestScheduler scheduler;
	private Scheduler.Worker innerScheduler;
	private Observer<String> observer;

	@Before
	@SuppressWarnings("unchecked")
	public void before() {
		scheduler = new TestScheduler();
		innerScheduler = scheduler.createWorker();
		observer = Mockito.mock(Observer.class);
	}

	@Test
	public void testThrottlingWithCompleted() {
		Observable<String> source = Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> observer) {
				publishNext(observer, 100, "one"); // publish as it's first
				publishNext(observer, 300, "two"); // skip as it's last within
													// the first 400
				publishNext(observer, 900, "three"); // publish
				publishNext(observer, 905, "four"); // skip
				publishCompleted(observer, 1000); // Should be published as soon
													// as the timeout expires.
			}
		});

		Observable<String> sampled = source
				.compose(Transformers.<String>sampleFirst(400, TimeUnit.MILLISECONDS, scheduler));
		sampled.subscribe(observer);

		InOrder inOrder = inOrder(observer);

		scheduler.advanceTimeTo(1000, TimeUnit.MILLISECONDS);
		inOrder.verify(observer, times(1)).onNext("one");
		inOrder.verify(observer, times(0)).onNext("two");
		inOrder.verify(observer, times(1)).onNext("three");
		inOrder.verify(observer, times(0)).onNext("four");
		inOrder.verify(observer, times(1)).onCompleted();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testThrottlingWithError() {
		Observable<String> source = Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> observer) {
				Exception error = new TestException();
				publishNext(observer, 100, "one"); // Should be published since
													// it is first
				publishNext(observer, 200, "two"); // Should be skipped since
													// onError will arrive before
													// the timeout expires
				publishError(observer, 300, error); // Should be published as
													// soon as the timeout
													// expires.
			}
		});

		Observable<String> sampled = source
				.compose(Transformers.<String>sampleFirst(400, TimeUnit.MILLISECONDS, scheduler));
		sampled.subscribe(observer);

		InOrder inOrder = inOrder(observer);

		scheduler.advanceTimeTo(400, TimeUnit.MILLISECONDS);
		inOrder.verify(observer).onNext("one");
		inOrder.verify(observer).onError(any(TestException.class));
		inOrder.verifyNoMoreInteractions();
	}

	private <T> void publishCompleted(final Observer<T> observer, long delay) {
		innerScheduler.schedule(new Action0() {
			@Override
			public void call() {
				observer.onCompleted();
			}
		}, delay, TimeUnit.MILLISECONDS);
	}

	private <T> void publishError(final Observer<T> observer, long delay, final Exception error) {
		innerScheduler.schedule(new Action0() {
			@Override
			public void call() {
				observer.onError(error);
			}
		}, delay, TimeUnit.MILLISECONDS);
	}

	@SuppressWarnings("serial")
	private static class TestException extends Exception {

	}

	private <T> void publishNext(final Observer<T> observer, long delay, final T value) {
		innerScheduler.schedule(new Action0() {
			@Override
			public void call() {
				observer.onNext(value);
			}
		}, delay, TimeUnit.MILLISECONDS);
	}

	@Test
	public void testSampleFirst() {
		@SuppressWarnings("unchecked")
		Observer<Integer> observer = mock(Observer.class);
		TestScheduler s = new TestScheduler();
		PublishSubject<Integer> o = PublishSubject.create();
		o.compose(Transformers.<Integer>sampleFirst(500, TimeUnit.MILLISECONDS, s)).subscribe(observer);

		// send events with simulated time increments
		s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
		o.onNext(1); // deliver
		o.onNext(2); // skip
		s.advanceTimeTo(501, TimeUnit.MILLISECONDS);
		o.onNext(3); // deliver
		s.advanceTimeTo(600, TimeUnit.MILLISECONDS);
		o.onNext(4); // skip
		s.advanceTimeTo(700, TimeUnit.MILLISECONDS);
		o.onNext(5); // skip
		o.onNext(6); // skip
		s.advanceTimeTo(1001, TimeUnit.MILLISECONDS);
		o.onNext(7); // deliver
		s.advanceTimeTo(1501, TimeUnit.MILLISECONDS);
		o.onCompleted();

		InOrder inOrder = inOrder(observer);
		inOrder.verify(observer).onNext(1);
		inOrder.verify(observer).onNext(3);
		inOrder.verify(observer).onNext(7);
		inOrder.verify(observer).onCompleted();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void testSampleWindowIsConstantDuration() {
		@SuppressWarnings("unchecked")
		Observer<Integer> observer = mock(Observer.class);
		TestScheduler s = new TestScheduler();
		PublishSubject<Integer> o = PublishSubject.create();
		o.compose(Transformers.<Integer>sampleFirst(1000, TimeUnit.MILLISECONDS, s)).subscribe(observer);

		// send events with simulated time increments
		s.advanceTimeTo(0, TimeUnit.MILLISECONDS);
		o.onNext(1);
		s.advanceTimeTo(1200, TimeUnit.MILLISECONDS);
		o.onNext(2);
		s.advanceTimeTo(2100, TimeUnit.MILLISECONDS);
		o.onNext(3);
		o.onCompleted();

		InOrder inOrder = inOrder(observer);
		inOrder.verify(observer).onNext(1);
		inOrder.verify(observer).onNext(2);
		inOrder.verify(observer).onNext(3);
		inOrder.verify(observer).onCompleted();
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSkipDurationOfZeroThrowsIllegalArgumentException() {
		Observable.just(1).compose(Transformers.<Integer>sampleFirst(0, TimeUnit.SECONDS));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSkipDurationOfLessThanZeroThrowsIllegalArgumentException() {
		Observable.just(1).compose(Transformers.sampleFirst(-1, TimeUnit.SECONDS));
	}
}
