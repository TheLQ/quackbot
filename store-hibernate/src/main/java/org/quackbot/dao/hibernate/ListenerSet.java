/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
