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

package org.quackbot.data.hibernate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.quackbot.dao.AdminDAO;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.DAOFactory;
import org.quackbot.dao.ServerDAO;

/**
 *
 * @author lordquackstar
 */
public class HbStore implements DAOFactory {
	Session session;
	
	public HbStore() {
		session = new Configuration()
                .configure() // configures settings from hibernate.cfg.xml
                .buildSessionFactory().openSession();
	}
	
	public AdminDAO newAdminStore(String name) {
		AdminStoreHb admin = new AdminStoreHb();
		session.save(admin);
		return admin;
	}

	public ChannelDAO newChannelStore(String name) {
		ChannelStoreHb channel = new ChannelStoreHb();
		session.save(channel);
		return channel;
	}

	public ServerDAO newServerStore(String address) {
		ServerStoreHb server = new ServerStoreHb();
		session.save(server);
		return server;
	}

	public Set<ServerDAO> getServers() {
		return Collections.unmodifiableSet(new HashSet(session.createQuery( "from ServerStoreHb" ).list()));
	}

	public Set<AdminDAO> getAllAdmins() {
		return Collections.unmodifiableSet(new HashSet(session.createQuery( "from AdminStoreHb" ).list()));
	}

	public void close() throws Exception {
		session.close();
		session.disconnect();
	}
}
