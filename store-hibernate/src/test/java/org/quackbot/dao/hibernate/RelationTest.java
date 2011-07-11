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

import org.quackbot.dao.UserDAO;
import java.util.List;
import org.hibernate.SessionFactory;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.util.StringHelper;
import org.quackbot.dao.ChannelDAO;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author lordquackstar
 */
public class RelationTest {
	protected Configuration config;
	protected SessionFactory sessionFactory;
	protected Session session;

	public RelationTest() {
		//Configure these things once
		config = new Configuration().configure();
		config.setNamingStrategy(new TestNamingStrategy());
		sessionFactory = config.buildSessionFactory();
		session = sessionFactory.openSession();
		
		DAOControllerHb controller = mock(DAOControllerHb.class);
		when(controller.getSession()).thenReturn(session);
		DAOControllerHb.instance = controller;
	}

	@BeforeTest
	public void setUp() {
		SchemaExport se = new SchemaExport(config);
		se.create(true, true);
	}

	@Test(description = "Make sure when saving a server the assoicated channels get created")
	public void ServerChannelTest() {
		//Setup
		session.beginTransaction();
		ServerDAOHb server = new ServerDAOHb();
		server.setAddress("some.host");
		ChannelDAOHb channel = new ChannelDAOHb();
		channel.setName("#channelName");
		server.getChannels().add(channel);
		session.save(server);
		session.getTransaction().commit();

		//Make sure they exist
		session.beginTransaction();
		List results = session.createQuery(" from ServerDAOHb").list();
		assertEquals(results.size(), 1, "Too many/No servers");
		ServerDAOHb fetchedServer = (ServerDAOHb) results.get(0);
		assertNotNull(fetchedServer, "Fetched server is null");
		assertEquals((int) fetchedServer.getServerId(), 1, "Server ID is wrong");
		assertEquals(fetchedServer.getAddress(), "some.host", "Server host is wrong");

		Set<ChannelDAO> channels = fetchedServer.getChannels();
		assertEquals(channels.size(), 1, "Channels size is wrong (no channel)");
		ChannelDAO fetchedChannel = channels.iterator().next();
		assertNotNull(fetchedChannel, "Channel is null but is in the channel list");
		assertEquals((int) fetchedChannel.getChannelID(), 1, "Channel ID is wrong");
		assertEquals(fetchedChannel.getName(), "#channelName", "Channel name doesn't match");
		session.getTransaction().commit();
	}

	@Test(dependsOnMethods = "ServerChannelTest", description = "Make sure when saving a server the assoicated channels and users get created")
	public void ServerChannelUserTest() {
		//Setup
		session.beginTransaction();
		ServerDAOHb server = new ServerDAOHb();
		server.setAddress("some.host");
		ChannelDAOHb channel = new ChannelDAOHb();
		channel.setName("#channelName");
		server.getChannels().add(channel);
		UserDAOHb userDAOHb = new UserDAOHb();
		userDAOHb.setNick("someNick");
		channel.getUsers().add(userDAOHb);
		session.save(server);
		session.getTransaction().commit();

		//Make sure the channel created the user
		session.beginTransaction();
		List results = session.createQuery("from ChannelDAOHb").list();
		assertEquals(results.size(), 1, "Too many/No channels");
		ChannelDAOHb fetchedChannel = (ChannelDAOHb) results.get(0);
		assertEquals((int) fetchedChannel.getChannelID(), 1, "Channel ID is wrong");
		assertEquals(fetchedChannel.getName(), "#channelName", "Channel name doesn't match");
		
		Set<UserDAO> users = channel.getUsers();
		assertEquals(users.size(), 1, "Too many/No users");
		UserDAO fetchedUser = users.iterator().next();
		assertEquals((int) fetchedUser.getUserId(), 1, "User ID is wrong");
		assertEquals(fetchedUser.getNick(), "someNick");
	}

	protected class TestNamingStrategy extends ImprovedNamingStrategy {
		@Override
		public String classToTableName(String className) {
			return StringHelper.unqualify(className);
		}

		@Override
		public String propertyToColumnName(String propertyName) {
			return propertyName;
		}

		@Override
		public String tableName(String tableName) {
			return "TEST_" + tableName;
		}

		@Override
		public String columnName(String columnName) {
			return columnName;
		}

		public String propertyToTableName(String className, String propertyName) {
			return "TEST_" + classToTableName(className) + '_'
					+ propertyToColumnName(propertyName);
		}
	}
}
