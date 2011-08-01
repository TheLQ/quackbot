package org.quackbot.dao;

import java.io.Serializable;
import org.quackbot.dao.model.ServerEntry;
import org.quackbot.dao.model.UserEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface UserDAO<T extends UserEntry<I>, I extends Serializable> extends GenericDAO<T, I> {
	public UserEntry findByNick(ServerEntry server, String userNick);

	public UserEntry create(String nick);
}
