package org.quackbot.dao.hibernate;

import java.util.Iterator;
import org.hibernate.criterion.Restrictions;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.hibernate.model.ChannelEntryHibernate;
import org.quackbot.dao.model.AdminEntry;
import org.quackbot.dao.model.ChannelEntry;
import org.quackbot.dao.model.ServerEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class ChannelDAOHibernate extends GenericHbDAO<ChannelEntryHibernate> implements ChannelDAO<ChannelEntryHibernate> {
	@Override
	public ChannelEntryHibernate delete(ChannelEntryHibernate entity) {
		//Remove channel from server
		entity.getServer().getChannels().remove(entity);
		
		//Remove channel from admins
		for (Iterator<AdminEntry> itr = entity.getAdmins().iterator(); itr.hasNext();) {
			AdminEntry curAdmin = itr.next();
			curAdmin.getChannels().remove(this);
			itr.remove();
		}

		//Delete all usermaps for this channel
		entity.getUserMaps().clear();

		//Finally, delete the channel
		getSession().delete(entity);
		return entity;
	}

	@Override
	public ChannelEntryHibernate findByName(ServerEntry server, String channelName) {
		return (ChannelEntryHibernate)getSession()
				.createCriteria(ChannelEntryHibernate.class)
				.add(Restrictions.eq("server", server))
				.add(Restrictions.eq("name", channelName))
				.uniqueResult();
	}

	@Override
	public ChannelEntryHibernate create(String channelName) {
		ChannelEntryHibernate entry = new ChannelEntryHibernate();
		entry.setName(channelName);
		return entry;
	}
}
