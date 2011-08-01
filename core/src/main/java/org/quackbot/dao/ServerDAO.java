package org.quackbot.dao;

import java.io.Serializable;
import org.quackbot.dao.model.ServerEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface ServerDAO<T extends ServerEntry<I>, I extends Serializable> extends GenericDAO<T, I> {
	public T findByAddress(String serverAddress);

	public T create(String address);
}
