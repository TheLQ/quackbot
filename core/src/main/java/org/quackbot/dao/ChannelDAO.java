package org.quackbot.dao;

import org.quackbot.dao.model.ChannelEntry;
import org.quackbot.dao.model.ServerEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface ChannelDAO<T extends ChannelEntry> extends GenericDAO<T> {
	public T findByName(ServerEntry server, String channelName);
	
	public T create(String channelName);
}
