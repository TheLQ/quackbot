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

import java.util.ArrayList;
import org.quackbot.dao.UserDAO;
import java.util.List;
import org.hibernate.SessionFactory;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.util.StringHelper;
import org.quackbot.dao.ChannelDAO;
import org.testng.annotations.BeforeMethod;
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

		DAOControllerHb controller = mock(DAOControllerHb.class);
		when(controller.getSession()).thenReturn(session);
		DAOControllerHb.instance = controller;
	}

	@BeforeMethod
	public void setUp() {
		SchemaExport se = new SchemaExport(config);
		se.drop(true, true);
		se.create(true, true);
		session = sessionFactory.openSession();
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

	@Test(dependsOnMethods = "ServerChannelTest")
	public void ChannelUserStatusTest() {
		session.beginTransaction();
		ServerDAOHb server = new ServerDAOHb();
		server.setAddress("some.host");
		server.getChannels().add(generateChannel());
		session.save(server);
		session.getTransaction().commit();

		//Make sure the statuses still work (use known working methods
		session.beginTransaction();
		ChannelDAOHb chan = (ChannelDAOHb) session.createQuery("from ChannelDAOHb").list().get(0);
		
		//Verify there are 3 users
		List<String> usersShouldExist = new ArrayList();
		usersShouldExist.add("someNickNormal");
		usersShouldExist.add("someNickOp");
		usersShouldExist.add("someNickVoice");
		for(UserDAO curUser : chan.getUsers()) {
			String nick = curUser.getNick();
			assertTrue(usersShouldExist.contains(nick), "Unknown user: " + nick);
			usersShouldExist.remove(nick);
		}
		assertEquals(usersShouldExist.size(), 0, "Users missing from channel's getUsers: " + StringUtils.join(usersShouldExist, ", "));
		
		//Verify normal
		assertEquals(chan.getNormalUsers().size(), 1, "Normal user list size is wrong");
		UserDAO user = chan.getNormalUsers().iterator().next();
		assertEquals(user.getNick(), "someNickNormal", "Normal nick is wrong (wrong user?)");
		
		//Verify op
		assertEquals(chan.getOps().size(), 1, "Op list size is wrong");
		user = chan.getOps().iterator().next();
		assertEquals(user.getNick(), "someNickOp", "Op nick is wrong (wrong user?)");
		
		//Verify voice
		assertEquals(chan.getVoices().size(), 1, "Op list size is wrong");
		user = chan.getVoices().iterator().next();
		assertEquals(user.getNick(), "someNickVoice", "Op nick is wrong (wrong user?)");
		
		//Make sure the other lists are empty
		assertEquals(chan.getSuperOps().size(), 0, "Extra super ops: " + StringUtils.join(chan.getSuperOps(), ", "));
		assertEquals(chan.getHalfOps().size(), 0, "Extra half ops: " + StringUtils.join(chan.getHalfOps(), ", "));
		assertEquals(chan.getOwners().size(), 0, "Extra owners: " + StringUtils.join(chan.getOwners(), ", "));
	}

	protected ChannelDAOHb generateChannel() {
		ChannelDAOHb channel = new ChannelDAOHb();
		channel.setName("#channelName");
		channel.getUsers().add(generateUser("someNickNormal"));
		channel.getOps().add(generateUser("someNickOp"));
		channel.getVoices().add(generateUser("someNickVoice"));
		return channel;
	}

	protected UserDAOHb generateUser(String name) {
		UserDAOHb user = new UserDAOHb();
		user.setNick(name);
		return user;
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
