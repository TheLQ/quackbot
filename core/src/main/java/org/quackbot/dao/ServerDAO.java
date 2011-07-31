package org.quackbot.dao;

import java.io.Serializable;
import org.quackbot.dao.model.ServerEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface ServerDAO<T extends ServerEntry<I>, I extends Serializable> extends GenericDAO<T, I> {
	public ServerEntry getByAddress(String serverAddress);

	public ServerEntry create(String address);
}
