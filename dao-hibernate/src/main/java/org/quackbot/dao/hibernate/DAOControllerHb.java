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

import java.io.InvalidObjectException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.quackbot.dao.AdminDAO;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.DAOController;
import org.quackbot.dao.LogEntryDAO;
import org.quackbot.dao.ServerDAO;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class DAOControllerHb implements DAOController {
	protected Configuration configuration;
	protected SessionFactory sessionFactory;
	protected ThreadLocal<Session> sessions;
	protected static DAOControllerHb instance;

	public DAOControllerHb() {
		if(instance != null)
			throw new RuntimeException("Can't create more than one DAOControllerHb");
		
		// configures settings from hibernate.cfg.xml
		configuration = new Configuration().configure();
		configuration.setNamingStrategy(new PrefixNamingStrategy("quackbot_"));
		
		sessionFactory = configuration.buildSessionFactory();
		
		instance = this;
	}

	public AdminDAO newAdminStore(String name) {
		AdminDAOHb admin = new AdminDAOHb();
		getSession().save(admin);
		return admin;
	}

	public ChannelDAO newChannelStore(String name) {
		ChannelDAOHb channel = new ChannelDAOHb();
		getSession().save(channel);
		return channel;
	}

	public ServerDAO newServerStore(String address) {
		ServerDAOHb server = new ServerDAOHb();
		getSession().save(server);
		return server;
	}
	
	public LogEntryDAO newLogEntry() {
		LogEntryDAOHb logEntry = new LogEntryDAOHb();
		return logEntry;
	}

	@Override
	public Set<ServerDAO> getServers() {
		return Collections.unmodifiableSet(new HashSet(getSession().createQuery("from ServerStoreHb").list()));
	}

	@Override
	public Set<AdminDAO> getAllAdmins() {
		return Collections.unmodifiableSet(new HashSet(getSession().createQuery("from AdminStoreHb").list()));
	}

	@Override
	public void close() throws Exception {
		sessionFactory.close();
	}

	@Override
	public void beginTransaction() {
		//Create a new session
		Session session = sessionFactory.openSession();
		sessions.set(session);
		//Begin the transaction
		session.beginTransaction();
	}

	@Override
	public void endTransaction(boolean isGood) {
		//End the transaction
		try {
			if (isGood)
				getSession().getTransaction().commit();
			else
				getSession().getTransaction().rollback();
		} finally {
			getSession().close();
		}
	}

	protected Session getSession() {
		Session session = sessions.get();
		if (session == null)
			throw new RuntimeException("No session exists for this thread.");
		return session;
	}
	
	public static DAOControllerHb getInstance() {
		return instance;
	}
}
