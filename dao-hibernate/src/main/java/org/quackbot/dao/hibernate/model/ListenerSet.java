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
package org.quackbot.dao.hibernate.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
abstract class ListenerSet<T> implements Set<T> {
	protected Collection<T> delegateSet;

	public ListenerSet(Collection<T> delegateSet) {
		this.delegateSet = delegateSet;
	}

	/* Hook methods */
	public abstract void onAdd(T entry);

	public abstract void onRemove(Object entry);

	@Override
	public boolean add(T e) {
		onAdd(e);
		return delegateSet.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T curObj : c)
			onAdd(curObj);
		return delegateSet.addAll(c);
	}

	@Override
	public boolean remove(Object o) {
		onRemove(o);
		return delegateSet.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object curObj : c)
			onRemove(curObj);
		return delegateSet.removeAll(c);
	}
	
	@Override
	public void clear() {
		for(T curEntry : delegateSet)
			onRemove(curEntry);
		delegateSet.clear();
	}

	@Override
	public Iterator<T> iterator() {
		//Wrap the iterator to capture remove operations
		final Iterator<T> queryItr = delegateSet.iterator();
		return new Iterator<T>() {
			T lastElement;

			public boolean hasNext() {
				return queryItr.hasNext();
			}

			public T next() {
				lastElement = queryItr.next();
				return lastElement;
			}

			public void remove() {
				onRemove(lastElement);
				queryItr.remove();
			}
		};
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return delegateSet.toArray(a);
	}

	@Override
	public Object[] toArray() {
		return delegateSet.toArray();
	}

	@Override
	public int size() {
		return delegateSet.size();
	}

	@Override
	public boolean isEmpty() {
		return delegateSet.isEmpty();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return delegateSet.containsAll(c);
	}

	@Override
	public boolean contains(Object o) {
		return delegateSet.contains(o);
	}
}
