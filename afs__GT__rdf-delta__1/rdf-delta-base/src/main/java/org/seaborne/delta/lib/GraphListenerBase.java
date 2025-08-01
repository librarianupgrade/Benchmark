/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.delta.lib;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEvents;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Triple;

// An implemention of GraphListenerBase returns in Jena5.

/**
 *  Convert the full, wide GraphListener interface into just add/delete
 *  together with optional startRead/finishRead.
 *  <p>Any {@link GraphListener} method can be overridden.
 */
public abstract class GraphListenerBase implements GraphListener {
	public GraphListenerBase() {
	}

	@Override
	public void notifyAddTriple(Graph g, Triple t) {
		addEvent(t);
	}

	@Override
	public void notifyAddArray(Graph g, Triple[] triples) {
		for (Triple triple : triples) {
			addEvent(triple);
		}
	}

	@Override
	public void notifyAddList(Graph g, List<Triple> triples) {
		notifyAddIterator(g, triples.iterator());
	}

	@Override
	public void notifyAddIterator(Graph g, Iterator<Triple> it) {
		for (; it.hasNext();)
			addEvent(it.next());
	}

	@Override
	public void notifyAddGraph(Graph g, Graph added) {
		added.find().forEach(this::addEvent);
	}

	@Override
	public void notifyDeleteTriple(Graph g, Triple t) {
		deleteEvent(t);
	}

	@Override
	public void notifyDeleteList(Graph g, List<Triple> triples) {
		notifyDeleteIterator(g, triples.iterator());
	}

	@Override
	public void notifyDeleteArray(Graph g, Triple[] triples) {
		for (Triple triple : triples) {
			deleteEvent(triple);
		}
	}

	@Override
	public void notifyDeleteIterator(Graph g, Iterator<Triple> it) {
		for (; it.hasNext();)
			deleteEvent(it.next());
	}

	@Override
	public void notifyDeleteGraph(Graph g, Graph removed) {
		removed.find().forEach(this::deleteEvent);
	}

	protected abstract void addEvent(Triple t);

	protected abstract void deleteEvent(Triple t);

	// --------

	@Override
	public void notifyEvent(Graph source, Object value) {
		if (value.equals(GraphEvents.startRead))
			startRead();
		else if (value.equals(GraphEvents.finishRead))
			finishRead();
	}

	protected void startRead() {
	}

	protected void finishRead() {
	}
}
