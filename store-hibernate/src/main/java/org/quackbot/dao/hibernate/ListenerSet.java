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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author lordquackstar
 */
abstract class ListenerSet<T> extends HashSet<T> {
	/* Hook methods */
	public abstract void onAdd(T entry);

	public abstract void onRemove(Object entry);

	@Override
	public boolean add(T e) {
		onAdd(e);
		return super.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T curObj : c)
			onAdd(curObj);
		return super.addAll(c);
	}

	@Override
	public boolean remove(Object o) {
		onRemove(o);
		return super.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object curObj : c)
			onRemove(curObj);
		return super.removeAll(c);
	}

	@Override
	public Iterator<T> iterator() {
		//Wrap the iterator to capture remove operations
		final Iterator<T> queryItr = super.iterator();
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
}
