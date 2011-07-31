package org.quackbot.dao;

import java.io.Serializable;
import org.quackbot.dao.model.AdminEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface AdminDAO<T extends AdminEntry<I>, I extends Serializable> extends GenericDAO<T, I> {
	public AdminEntry getByName(String adminName);

	public AdminEntry create(String adminName);
}
