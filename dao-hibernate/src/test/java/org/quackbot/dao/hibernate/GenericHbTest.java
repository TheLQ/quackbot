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

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.cfg.Configuration;
import org.quackbot.dao.hibernate.model.ServerEntryHibernate;
import org.quackbot.dao.hibernate.model.UserEntryHibernate;
import org.quackbot.dao.hibernate.model.ChannelEntryHibernate;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.quackbot.dao.AdminDAO;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.LogDAO;
import org.quackbot.dao.ServerDAO;
import org.quackbot.dao.UserDAO;
import org.quackbot.dao.hibernate.model.AdminEntryHibernate;
import org.quackbot.dao.hibernate.model.LogEntryHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class GenericHbTest {
	protected Configuration config;
	@Autowired
	@Setter(AccessLevel.PROTECTED)
	protected AdminDAO<AdminEntryHibernate> adminDao;
	@Autowired
	@Setter(AccessLevel.PROTECTED)
	protected ChannelDAO<ChannelEntryHibernate> channelDao;
	@Autowired
	@Setter(AccessLevel.PROTECTED)
	protected LogDAO<LogEntryHibernate> logDao;
	@Autowired
	@Setter(AccessLevel.PROTECTED)
	protected ServerDAO<ServerEntryHibernate> serverDao;
	@Autowired
	@Setter(AccessLevel.PROTECTED)
	protected UserDAO<UserEntryHibernate> userDao;

	@BeforeClass
	public void setupSpring() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
		context.registerShutdownHook();
		//Extract the config from the session factory
		config = ((LocalSessionFactoryBean) context.getBean("&sessionFactory")).getConfiguration();
	}

	@BeforeMethod
	public void setupSchema() {
		SchemaExport se = new SchemaExport(config);
		se.create(true, true);
	}

	protected ServerEntryHibernate generateServer(String address) {
		return serverDao.create(address);
	}

	protected ChannelEntryHibernate generateChannel(String name) {
		return channelDao.create(name);
	}

	protected UserEntryHibernate generateUser(String name) {
		return userDao.create(name);
	}

	protected AdminEntryHibernate generateAdmin(String name) {
		return adminDao.create(name);
	}

	protected ServerEntryHibernate generateEnviornment(long num, AdminEntryHibernate globalAdmin) {
		ServerEntryHibernate server = generateServer("irc.host" + num);
		server.setId(num);
		if (globalAdmin != null)
			server.getAdmins().add(globalAdmin);
		server.getAdmins().add(generateAdmin("serverAdmin" + num));

		AdminEntryHibernate channelAdmin = generateAdmin("channelAdmin" + num);
		UserEntryHibernate channelUser = generateUser("channelUser" + num);

		ChannelEntryHibernate channel = generateChannel("#aChannel" + num);
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

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected void setupEnviornment() {
		AdminEntryHibernate globalAdmin = generateAdmin("globalAdmin");
		serverDao.save(generateEnviornment(1, globalAdmin));
		serverDao.save(generateEnviornment(2, globalAdmin));
	}
}
