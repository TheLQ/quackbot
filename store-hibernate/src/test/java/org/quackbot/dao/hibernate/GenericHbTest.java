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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
	
	protected ChannelDAOHb generateChannel() {
		ChannelDAOHb channel = new ChannelDAOHb();
		channel.setName("#channelName");
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
