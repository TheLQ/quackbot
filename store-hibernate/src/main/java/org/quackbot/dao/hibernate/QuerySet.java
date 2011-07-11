/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Wraps a query List with a Set to satisfy DAO interfaces. Provides listener
 * methods to preform an operation 
 * @author lordquackstar
 */
public class QuerySet<E> implements Set<E> {
	protected List queryResults;

	public QuerySet(List queryResults) {
		this.queryResults = queryResults;

	}

	public boolean add(E e) {
		onAdd(e);
		return queryResults.add(e);
	}

	public boolean addAll(Collection<? extends E> c) {
		for (E curObj : c)
			onAdd(curObj);
		return queryResults.addAll(c);
	}

	public boolean remove(Object o) {
		onRemove(o);
		return queryResults.remove(o);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object curObj : c)
			onRemove(curObj);
		return queryResults.removeAll(c);
	}

	public Iterator<E> iterator() {
		//Wrap the iterator to capture remove operations
		final Iterator<E> queryItr = queryResults.iterator();
		return new Iterator<E>() {
			E lastElement;
			public boolean hasNext() {
				return queryItr.hasNext();
			}

			public E next() {
				lastElement = queryItr.next();
				return lastElement;
			}

			public void remove() {
				onRemove(lastElement);
				queryItr.remove();
			}
		};
	}
	
	/* Hook methods */

	public void onAdd(E entry) {
	}

	public void onRemove(Object entry) {
	}

	/* Basic delegate methods */
	
	public Object[] toArray(Object[] a) {
		return queryResults.toArray(a);
	}

	public Object[] toArray() {
		return queryResults.toArray();
	}

	public int size() {
		return queryResults.size();
	}

	public boolean isEmpty() {
		return queryResults.isEmpty();
	}

	public boolean containsAll(Collection c) {
		return queryResults.containsAll(c);
	}

	public boolean contains(Object o) {
		return queryResults.contains(o);
	}

	public void clear() {
		queryResults.clear();
	}
	
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
