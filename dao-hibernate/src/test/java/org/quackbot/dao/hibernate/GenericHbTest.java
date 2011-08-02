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
import org.quackbot.dao.hibernate.model.ServerEntryHibernate;
import org.quackbot.dao.hibernate.model.UserEntryHibernate;
import org.quackbot.dao.hibernate.model.ChannelEntryHibernate;
import org.quackbot.dao.AdminDAO;
import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.LogDAO;
import org.quackbot.dao.ServerDAO;
import org.quackbot.dao.UserDAO;
import org.quackbot.dao.hibernate.model.AdminEntryHibernate;
import org.quackbot.dao.hibernate.model.LogEntryHibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@ContextConfiguration({"classpath:spring-dao-hibernate-test.xml"})
public class GenericHbTest extends AbstractTransactionalTestNGSpringContextTests {
	@Autowired
	protected AbstractApplicationContext context;
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

	@BeforeMethod
	public void setupSchema() {
		LocalSessionFactoryBean session = (LocalSessionFactoryBean) context.getBean("&sessionFactory");
		session.dropDatabaseSchema();
		session.createDatabaseSchema();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected ServerEntryHibernate generateEnviornment(long num, AdminEntryHibernate globalAdmin) {
		ServerEntryHibernate server = serverDao.create("irc.host" + num);
		server.setId(num);
		if (globalAdmin != null)
			server.getAdmins().add(globalAdmin);
		server.getAdmins().add(adminDao.create("serverAdmin" + num));

		AdminEntryHibernate channelAdmin = adminDao.create("channelAdmin" + num);
		UserEntryHibernate channelUser = userDao.create("channelUser" + num);

		ChannelEntryHibernate channel = channelDao.create("#aChannel" + num);
		server.getChannels().add(channel);
		channel.getAdmins().add(channelAdmin);
		channel.getAdmins().add(adminDao.create("aChannelAdmin" + num));
		channel.getUsers().add(userDao.create("aNormalUser" + num));
		channel.getUsers().add(channelUser);
		channel.getOps().add(userDao.create("aOpUser" + num));
		channel.getSuperOps().add(userDao.create("aSuperOpUser" + num));

		channel = channelDao.create("#someChannel" + num);
		server.getChannels().add(channel);
		channel.getAdmins().add(channelAdmin);
		channel.getAdmins().add(adminDao.create("someChannelAdmin" + num));
		channel.getUsers().add(userDao.create("someNormalUser" + num));
		channel.getOps().add(userDao.create("someOpUser" + num));
		channel.getOps().add(channelUser);
		channel.getSuperOps().add(userDao.create("someSuperOpUser" + num));

		return server;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected void setupEnviornment() {
		AdminEntryHibernate globalAdmin = adminDao.create("globalAdmin");
		serverDao.save(generateEnviornment(1, globalAdmin));
		serverDao.save(generateEnviornment(2, globalAdmin));
	}
}
