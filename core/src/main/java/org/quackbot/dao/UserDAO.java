package org.quackbot.dao;

import org.quackbot.dao.model.ServerEntry;
import org.quackbot.dao.model.UserEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface UserDAO<T extends UserEntry> extends GenericDAO<T> {
	public UserEntry findByNick(ServerEntry server, String userNick);

	public UserEntry create(String nick);
}
