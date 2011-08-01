/**
 * Copyright (C) 2011 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Quackbot.
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.dao.hibernate;

import java.util.Iterator;
import org.hibernate.criterion.Restrictions;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.hibernate.model.ChannelEntryHibernate;
import org.quackbot.dao.model.AdminEntry;
import org.quackbot.dao.model.ServerEntry;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Repository
public class ChannelDAOHibernate extends GenericHbDAO<ChannelEntryHibernate> implements ChannelDAO<ChannelEntryHibernate> {
	@Override
	public ChannelEntryHibernate delete(ChannelEntryHibernate entity) {
		//Remove channel from server
		entity.getServer().getChannels().remove(entity);
		
		//Remove channel from admins
		for (Iterator<AdminEntry> itr = entity.getAdmins().iterator(); itr.hasNext();) {
			AdminEntry curAdmin = itr.next();
			curAdmin.getChannels().remove(entity);
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
