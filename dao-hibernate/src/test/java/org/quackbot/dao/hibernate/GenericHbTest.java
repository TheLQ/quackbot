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

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class GenericHbTest {
	protected Configuration config;
	protected DAOControllerHb controller;

	public GenericHbTest() {
		//Configure these things once
		controller = new DAOControllerHb() {
			{
				configuration.setNamingStrategy(new PrefixNamingStrategy("TEST_quackbot_"));
				GenericHbTest.this.config = configuration;
				//Rebuild session factory so new naming strategy is used
				sessionFactory = configuration.buildSessionFactory();
			}
		};
	}

	@BeforeMethod
	public void setUp() {
		SchemaExport se = new SchemaExport(config);
		se.create(true, true);
	}

	protected ServerDAOHb generateServer(String address) {
		return (ServerDAOHb) controller.newServerDAO(address);
	}

	protected ChannelDAOHb generateChannel(String name) {
		return (ChannelDAOHb) controller.newChannelDAO(name);
	}

	protected UserDAOHb generateUser(String name) {
		return (UserDAOHb) controller.newUserDAO(name);
	}

	protected AdminDAOHb generateAdmin(String name) {
		return (AdminDAOHb) controller.newAdminDAO(name);
	}

	protected ServerDAOHb generateEnviornment(int num, AdminDAOHb globalAdmin) {
		ServerDAOHb server = generateServer("irc.host" + num);
		server.setServerId(num);
		if (globalAdmin != null)
			server.getAdmins().add(globalAdmin);
		server.getAdmins().add(generateAdmin("serverAdmin" + num));

		AdminDAOHb channelAdmin = generateAdmin("channelAdmin" + num);
		UserDAOHb channelUser = generateUser("channelUser" + num);

		ChannelDAOHb channel = generateChannel("#aChannel" + num);
		server.getChannels().add(channel);
		channel.getAdmins().add(channelAdmin);
		channel.getAdmins().add(generateAdmin("aChannelAdmin" + num));
		channel.getUsers().add(generateUser("aNormalUser" + num));
		channel.getUsers().add(channelUser);
		channel.getOps().add(generateUser("aOpUser" + num));
		channel.getSuperOps().add(generateUser("aSuperOpUser" + num));

		channel = generateChannel("#someChannel" + num);
		server.getChannels().add(channel);
		channel.getAdmins().add(channelAdmin);
		channel.getAdmins().add(generateAdmin("someChannelAdmin" + num));
		channel.getUsers().add(generateUser("someNormalUser" + num));
		channel.getOps().add(generateUser("someOpUser" + num));
		channel.getOps().add(channelUser);
		channel.getSuperOps().add(generateUser("someSuperOpUser" + num));

		return server;
	}
}
