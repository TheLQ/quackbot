package org.quackbot.dao.hibernate;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import lombok.Data;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.quackbot.dao.GenericDAO;
import org.quackbot.dao.model.GenericEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
public abstract class GenericHbDAO<T extends GenericEntry> implements GenericDAO<T, Long> {
	protected SessionFactory sessionFactory;
	protected Class<T> persistentClass;

	public GenericHbDAO() {
		persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@Override
	public T findById(Long id) {
		return (T) getSession().createCriteria(getPersistentClass()).add(Restrictions.idEq(id)).uniqueResult();
	}

	@Override
	public List<T> findAll() {
		return getSession().createCriteria(getPersistentClass()).list();
	}

	@Override
	public T save(T entity) {
		getSession().save(entity);
		return entity;
	}

	@Override
	public T delete(T entity) {
		getSession().delete(entity);
		return entity;
	}
	
	@Override
	public T create() {
		try {
			return persistentClass.newInstance();
		} catch (Exception e) {
			//Absolutely no need to force everything that calls this to handle exceptions
			throw new RuntimeException("Can't create class " + persistentClass, e);
		}
	}

	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}
}
