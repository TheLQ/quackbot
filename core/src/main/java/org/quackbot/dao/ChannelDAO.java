package org.quackbot.dao;

import java.io.Serializable;
import org.quackbot.dao.model.ChannelEntry;
import org.quackbot.dao.model.ServerEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public interface ChannelDAO<T extends ChannelEntry<I>, I extends Serializable> extends GenericDAO<T, I> {
	public ChannelEntry getByName(ServerEntry server, String channelName);
}
