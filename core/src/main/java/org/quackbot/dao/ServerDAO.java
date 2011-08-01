package org.quackbot.dao;

import org.quackbot.dao.model.ServerEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface ServerDAO<T extends ServerEntry> extends GenericDAO<T> {
	public T findByAddress(String serverAddress);

	public T create(String address);
}
