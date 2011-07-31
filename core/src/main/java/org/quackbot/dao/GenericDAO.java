package org.quackbot.dao;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface GenericDAO<T, K extends Serializable> {
	T findById(K id);

	List<T> findAll();

	T save(T entity);
	
	T delete(T entity);
}
