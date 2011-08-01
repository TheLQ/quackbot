package org.quackbot.dao;

import org.quackbot.dao.model.AdminEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface AdminDAO<T extends AdminEntry> extends GenericDAO<T> {
	public T findByName(String adminName);

	public T create(String adminName);
}
