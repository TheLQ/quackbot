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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.util.StringHelper;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Mockito.*;

/**
 *
 * @author lordquackstar
 */
public class GenericHbTest {
	protected Configuration config;
	protected SessionFactory sessionFactory;
	protected Session session;

	public GenericHbTest() {
		//Configure these things once
		config = new Configuration().configure();
		config.setNamingStrategy(new TestNamingStrategy());
		sessionFactory = config.buildSessionFactory();		
	}

	@BeforeMethod
	public void setUp() {
		SchemaExport se = new SchemaExport(config);
		se.drop(true, true);
		se.create(true, true);
		session = sessionFactory.openSession();
		
		DAOControllerHb controller = mock(DAOControllerHb.class);
		when(controller.getSession()).thenReturn(session);
		DAOControllerHb.instance = controller;
	}

	protected ServerDAOHb generateServer(String address) {
		ServerDAOHb server = new ServerDAOHb();
		server.setAddress(address);
		return server;
	}

	protected ChannelDAOHb generateChannel(String name) {
		ChannelDAOHb channel = new ChannelDAOHb();
		channel.setName(name);
		return channel;
	}

	protected UserDAOHb generateUser(String name) {
		UserDAOHb user = new UserDAOHb();
		user.setNick(name);
		return user;
	}

	protected AdminDAOHb generateAdmin(String name) {
		AdminDAOHb globalAdmin = new AdminDAOHb();
		globalAdmin.setName(name);
		return globalAdmin;
	}

	protected ServerDAOHb generateEnviornment(int num, AdminDAOHb globalAdmin) {
		ServerDAOHb server = generateServer("irc.host" + num);
		server.setServerId(num);
		if (globalAdmin != null)
			server.getAdmins().add(globalAdmin);
		server.getAdmins().add(generateAdmin("serverAdmin" + num));

		AdminDAOHb channelAdmin = generateAdmin("channelAdmin" + num);

		ChannelDAOHb channel = generateChannel("#aChannel" + num);
		channel.getAdmins().add(channelAdmin);
		channel.getAdmins().add(generateAdmin("aChannelAdmin" + num));
		channel.getUsers().add(generateUser("aNormalUser" + num));
		channel.getOps().add(generateUser("aOpUser" + num));
		channel.getSuperOps().add(generateUser("aSuperOpUser" + num));
		server.getChannels().add(channel);

		channel = generateChannel("#someChannel" + num);
		channel.getAdmins().add(channelAdmin);
		channel.getAdmins().add(generateAdmin("someChannelAdmin" + num));
		channel.getUsers().add(generateUser("someNormalUser" + num));
		channel.getOps().add(generateUser("someOpUser" + num));
		channel.getSuperOps().add(generateUser("someSuperOpUser" + num));
		server.getChannels().add(channel);

		return server;
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
