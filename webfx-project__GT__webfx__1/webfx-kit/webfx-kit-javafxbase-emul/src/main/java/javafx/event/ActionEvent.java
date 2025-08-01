/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package javafx.event;

/**
 * An {@link Event} representing some type of action. This event type is widely
 * used to represent a variety of things, such as when a {@link javafx.scene.control.Button}
 * has been fired, when a {@link javafx.animation.KeyFrame} has finished, and other
 * such usages.
 * @since JavaFX 2.0
 */
public class ActionEvent extends Event {

	private static final long serialVersionUID = 20121107L;
	/**
	 * The only valid EventType for the ActionEvent.
	 */
	public static final EventType<ActionEvent> ACTION = new EventType<ActionEvent>(Event.ANY, "ACTION");

	/**
	 * Common supertype for all action event types.
	 * @since JavaFX 8.0
	 */
	public static final EventType<ActionEvent> ANY = ACTION;

	/**
	 * Creates a new {@code ActionEvent} with an event type of {@code ACTION}.
	 * The source and target of the event is set to {@code NULL_SOURCE_TARGET}.
	 */
	public ActionEvent() {
		super(ACTION);
	}

	/**
	 * Construct a new {@code ActionEvent} with the specified event source and target.
	 * If the source or target is set to {@code null}, it is replaced by the
	 * {@code NULL_SOURCE_TARGET} value. All ActionEvents have their type set to
	 * {@code ACTION}.
	 *
	 * @param source    the event source which sent the event
	 * @param target    the event target to associate with the event
	 */
	public ActionEvent(Object source, EventTarget target) {
		super(source, target, ACTION);
	}

	@Override
	public ActionEvent copyFor(Object newSource, EventTarget newTarget) {
		return (ActionEvent) super.copyFor(newSource, newTarget);
	}

	@Override
	public ActionEvent duplicate() {
		return new ActionEvent(source, target);
	}

	@Override
	public EventType<? extends ActionEvent> getEventType() {
		return (EventType<? extends ActionEvent>) super.getEventType();
	}

}
