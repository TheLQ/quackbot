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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.quackbot.dao.AdminDAO;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.DAOController;
import org.quackbot.dao.LogEntryDAO;
import org.quackbot.dao.ServerDAO;
import org.quackbot.dao.UserDAO;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class DAOControllerHb implements DAOController {
	protected Configuration configuration;
	protected SessionFactory sessionFactory;
	protected ThreadLocal<Queue<Object>> objectsToSave = new ThreadLocal();
	protected static DAOControllerHb instance;

	public DAOControllerHb() {
		if (instance != null)
			throw new RuntimeException("Can't create more than one DAOControllerHb");

		// configures settings from hibernate.cfg.xml
		configuration = new Configuration().configure();
		configuration.setNamingStrategy(new PrefixNamingStrategy("quackbot_"));

		sessionFactory = configuration.buildSessionFactory();

		instance = this;
	}

	@Override
	public AdminDAO newAdminDAO(String name) {
		AdminDAOHb admin = new AdminDAOHb();
		admin.setName(name);
		addObjectToSave(admin);
		return admin;
	}

	@Override
	public ChannelDAO newChannelDAO(String name) {
		ChannelDAOHb channel = new ChannelDAOHb();
		channel.setName(name);
		addObjectToSave(channel);
		return channel;
	}

	@Override
	public ServerDAO newServerDAO(String address) {
		ServerDAOHb server = new ServerDAOHb();
		server.setAddress(address);
		addObjectToSave(server);
		return server;
	}

	@Override
	public LogEntryDAO newLogEntryDAO() {
		LogEntryDAOHb logEntry = new LogEntryDAOHb();
		addObjectToSave(logEntry);
		return logEntry;
	}

	@Override
	public UserDAO newUserDAO(String nick) {
		UserDAOHb user = new UserDAOHb();
		user.setNick(nick);
		addObjectToSave(user);
		return user;
	}

	@Override
	public Set<ServerDAO> getServers() {
		List servers = getSession().createQuery("from ServerStoreHb").list();
		addObjectToSave(servers.toArray());
		return Collections.unmodifiableSet(new HashSet(servers));
	}

	@Override
	public Set<AdminDAO> getAllAdmins() {
		List admins = getSession().createQuery("from AdminStoreHb").list();
		addObjectToSave(admins.toArray());
		return Collections.unmodifiableSet(new HashSet(admins));
	}

	@Override
	public void close() throws Exception {
		sessionFactory.close();
	}

	@Override
	public void beginTransaction() {
		sessionFactory.getCurrentSession().beginTransaction();
	}

	@Override
	public void endTransaction(boolean isGood) {
		//End the transaction
		try {
			if (isGood) {
				Queue<Object> localObjects = objectsToSave.get();
				if (localObjects != null && !localObjects.isEmpty()) {
					synchronized(localObjects) {
						while(!localObjects.isEmpty()) {
							Object object = localObjects.remove();
							log.trace("Removing object:  " + object);
							getSession().saveOrUpdate(object);
						}
						objectsToSave.remove();
					}
				}
				getSession().getTransaction().commit();
			} else
				rollbackTransaction();
		} catch(RuntimeException e) {
			log.error("Exception encountered: ", e);
			rollbackTransaction();
			throw e;
		} finally {
			objectsToSave.remove();
		}
	}
	
	protected void rollbackTransaction() {
		getSession().getTransaction().rollback();
		getSession().close();
	}

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	public static DAOControllerHb getInstance() {
		return instance;
	}

	protected void addObjectToSave(Object... objects) {
		Queue<Object> localObjects = objectsToSave.get();
		log.trace("Adding object: " + StringUtils.join(objects, "\n\rAdding object: "));
		if (localObjects == null)
			objectsToSave.set(localObjects = new LinkedList());
		for(Object curObject : objects)
			localObjects.add(curObject);
	}
}
