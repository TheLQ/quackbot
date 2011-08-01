package org.quackbot.dao;

import java.util.List;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface GenericDAO<T> {
	T findById(Long id);

	List<T> findAll();

	T save(T entity);
	
	T delete(T entity);
	
	T create();
}
