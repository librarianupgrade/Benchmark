package com.github.davidmoten.rx.internal.operators;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public class QueueWithResourcesForwarder<T> implements QueueWithResources<T> {

	private final Queue<T> q;
	private volatile boolean unsubscribed;

	public QueueWithResourcesForwarder(Queue<T> queue) {
		this.q = queue;
	}

	@Override
	public boolean add(T e) {
		return q.add(e);
	}

	@Override
	public boolean offer(T e) {
		return q.offer(e);
	}

	@Override
	public int size() {
		return q.size();
	}

	@Override
	public boolean isEmpty() {
		return q.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return q.contains(o);
	}

	@Override
	public T remove() {
		return q.remove();
	}

	@Override
	public T poll() {
		return q.poll();
	}

	@Override
	public T element() {
		return q.element();
	}

	@Override
	public Iterator<T> iterator() {
		return q.iterator();
	}

	@Override
	public T peek() {
		return q.peek();
	}

	@Override
	public Object[] toArray() {
		return q.toArray();
	}

	@Override
	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] a) {
		return q.toArray(a);
	}

	@Override
	public boolean remove(Object o) {
		return q.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return q.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return q.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return q.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return q.retainAll(c);
	}

	@Override
	public void clear() {
		q.clear();
	}

	@Override
	public boolean equals(Object o) {
		return q.equals(o);
	}

	@Override
	public int hashCode() {
		return q.hashCode();
	}

	@Override
	public void unsubscribe() {
		unsubscribed = true;
	}

	@Override
	public void freeResources() {
		// do nothing
	}

	@Override
	public boolean isUnsubscribed() {
		return unsubscribed;
	}

	@Override
	public long resourcesSize() {
		return 0;
	}

}
