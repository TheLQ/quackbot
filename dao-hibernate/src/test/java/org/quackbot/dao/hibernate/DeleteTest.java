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

import org.quackbot.dao.hibernate.model.ServerEntryHibernate;
import org.quackbot.dao.hibernate.model.ChannelEntryHibernate;
import org.hibernate.ObjectDeletedException;
import org.hibernate.exception.ConstraintViolationException;
import org.quackbot.dao.GenericDAO;
import org.quackbot.dao.model.GenericEntry;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class DeleteTest extends GenericHbTest {
	@Test
	public void deleteAdminGlobalTest() {
		setupEnviornment();

		//Grab the global admin and delete it in a seperate transaction
		deleteEntity(adminDao, adminDao.findByName("globalAdmin"));

		//Finally in our own transaction make sure its gone
		ServerEntryHibernate server1 = serverDao.findById(1L);
		assertEquals(server1.getAdmins().size(), 1, "Too many server1 admins: " + server1.getAdmins());
		assertEquals(server1.getAdmins().iterator().next().getName(), "serverAdmin1", "Remaining server1 admin name is wrong");

		//Make sure its gone from server2
		ServerEntryHibernate server2 = serverDao.findById(2L);
		assertEquals(server2.getAdmins().size(), 1, "Too many server2 admins: " + server2.getAdmins());
		assertEquals(server2.getAdmins().iterator().next().getName(), "serverAdmin2", "Remaining server2 admin name is wrong");
	}

	@Test
	public void deleteAdminServerTest() {
		setupEnviornment();

		//Grab the someChannelAdmin2 admin and delete it in a seperate transaction
		deleteEntity(adminDao, adminDao.findByName("serverAdmin1"));

		//Make sure its gone from server1
		ServerEntryHibernate server1 = serverDao.findById(1L);
		assertEquals(server1.getAdmins().size(), 1, "Too many server1 admins: " + server1.getAdmins());
		assertEquals(server1.getAdmins().iterator().next().getName(), "globalAdmin", "Remaining server1 admin name is wrong");
	}

	@Test
	public void deleteAdminChannelGlobalTest() {
		setupEnviornment();

		//Grab the channel admin and delete it in a seperate transaction
		deleteEntity(adminDao, adminDao.findByName("channelAdmin1"));

		//Make sure its gone from #aChannel1
		ChannelEntryHibernate aChannel = channelDao.findByName(serverDao.findByAddress("irc.host1"), "#aChannel1");
		assertEquals(aChannel.getAdmins().size(), 1, "Too many #aChannel1 admins: " + aChannel.getAdmins());
		assertEquals(aChannel.getAdmins().iterator().next().getName(), "aChannelAdmin1", "Remaining #aChannel1 admin name is wrong");

		//Make sure its gone from #someChannel1
		ChannelEntryHibernate someChannel = channelDao.findByName(serverDao.findByAddress("irc.host1"), "#someChannel1");
		assertEquals(someChannel.getAdmins().size(), 1, "Too many #someChannel1 admins: " + someChannel.getAdmins());
		assertEquals(someChannel.getAdmins().iterator().next().getName(), "someChannelAdmin1", "Remaining #someChannel1 admin name is wrong");
	}

	@Test
	public void deleteAdminChannelTest() {
		setupEnviornment();

		//Grab the someChannelAdmin2 admin and delete it in a seperate transaction
		deleteEntity(adminDao, adminDao.findByName("someChannelAdmin2"));

		//Make sure its gone from #someChannel1
		ChannelEntryHibernate someChannel = channelDao.findByName(serverDao.findByAddress("irc.host2"), "#someChannel2");
		assertEquals(someChannel.getAdmins().size(), 1, "Too many #someChannel2 admins: " + someChannel.getAdmins());
		assertEquals(someChannel.getAdmins().iterator().next().getName(), "channelAdmin2", "Remaining #someChannel1 admin name is wrong");
	}

	/**
	 * WARNING: THIS TEST IS BROKEN
	 */
	@Test(successPercentage = 0, expectedExceptions = ObjectDeletedException.class)
	public void deleteChannelTest() {
		setupEnviornment();

		//Grab #someChannel2  and delete it in a seperate transaction
		deleteEntity(channelDao, channelDao.findByName(serverDao.findByAddress("irc.host2"), "#someChannel2"));

		//Make sure other channels still exist
		//TODO: Check for other channels
		ServerEntryHibernate server1 = serverDao.findByAddress("irc.host1");
		ServerEntryHibernate server2 = serverDao.findByAddress("irc.host2");
		assertNotNull(channelDao.findByName(server1, "#aChannel1"), "#aChannel1 doesn't exist");
		assertNotNull(channelDao.findByName(server1, "#someChannel1"), "#someChannel1 doesn't exist");
		assertNotNull(channelDao.findByName(server2, "#aChannel2"), "#aChannel2 doesn't exist");
		//List remainingChannels =session.createQuery("from ChannelDAOHb WHERE name != '#aChannel1' AND name != '#someChannel1' AND name != '#aChannel2'").list();
		//assertEquals(remainingChannels.size(), 0, "Extra channels after deletion: " + StringUtils.join(remainingChannels.toArray(), ", "));

		//Make sure other servers still exist
		assertNotNull(serverDao.findByAddress("irc.host1"), "Server 1 doesn't exist");
		assertNotNull(serverDao.findByAddress("irc.host2"), "Server 2 doesn't exist");
		//List remainingServers = session.createQuery("from ServerDAOHb WHERE address != 'irc.host1' AND address != 'irc.host2'").list();
		//assertEquals(remainingServers.size(), 0, "Strange servers exists: " + StringUtils.join(remainingServers.toArray(), ", "));

		//Make sure other users still exist
		assertNotNull(userDao.findByNick(server1, "someOpUser1"), "User someOpUser1 doesn't exist");
		assertNotNull(userDao.findByNick(server1, "someNormalUser1"), "User someNormalUser1 doesn't exist");
		assertNotNull(userDao.findByNick(server1, "someSuperOpUser1"), "User someSuperOpUser1 doesn't exist");
		assertNotNull(userDao.findByNick(server1, "aOpUser1"), "User aOpUser1 doesn't exist");
		assertNotNull(userDao.findByNick(server1, "aSuperOpUser1"), "User aSuperOpUser1 doesn't exist");
		assertNotNull(userDao.findByNick(server1, "aNormalUser1"), "User aNormalUser1 doesn't exist");
		assertNotNull(userDao.findByNick(server2, "aOpUser2"), "User aOpUser2 doesn't exist");
		assertNotNull(userDao.findByNick(server2, "aSuperOpUser2"), "User aSuperOpUser2 doesn't exist");
		assertNotNull(userDao.findByNick(server2, "aNormalUser2"), "User aNormalUser2 doesn't exist");
		//List remainingUsers = session.createQuery("from ServerDAOHb WHERE 'someOpUser1' AND 'someNormalUser1' AND 'someSuperOpUser1' AND 'aOpUser1' "
		//		+ "AND 'aSuperOpUser1' AND 'aNormalUser1' AND 'aOpUser2' AND 'aSuperOpUser2' AND 'aNormalUser2'").list();
		//assertEquals(remainingUsers.size(), 0, "Extra users after deletion: " + StringUtils.join(remainingUsers.toArray(), ", "));
	}

	/**
	 * WARNING: THIS TEST IS BROKEN
	 */
	@Test(successPercentage = 0, expectedExceptions = ObjectDeletedException.class)
	public void deleteServerTest() {
		setupEnviornment();

		//Grab the some.host2 server and delete it in a seperate transaction
		deleteEntity(serverDao, serverDao.findByAddress("irc.host2"));

		//Make sure other channels still exist
		ServerEntryHibernate server1 = serverDao.findByAddress("irc.host1");
		assertNotNull(channelDao.findByName(server1, "#aChannel1"), "#aChannel1 doesn't exist");
		assertNotNull(channelDao.findByName(server1, "#someChannel1"), "#someChannel1 doesn't exist");
		//List remainingChannels = session.createQuery("from ChannelDAOHb WHERE name != '#aChannel1' AND name != '#someChannel1'").list();
		//assertEquals(remainingChannels.size(), 0, "Extra channels after deletion: " + StringUtils.join(remainingChannels.toArray(), ", "));

		//Make sure other servers still exist
		assertNotNull(serverDao.findByAddress("irc.host1"), "Server 1 doesn't exist");
		//List remainingServers = session.createQuery("from ServerDAOHb WHERE address != 'irc.host1'").list();
		//assertEquals(remainingServers.size(), 0, "Strange servers exists: " + StringUtils.join(remainingServers.toArray(), ", "));

		//Make sure other users still exist
		assertNotNull(userDao.findByNick(server1, "someOpUser1"), "User someOpUser1 doesn't exist");
		assertNotNull(userDao.findByNick(server1, "someNormalUser1"), "User someNormalUser1 doesn't exist");
		assertNotNull(userDao.findByNick(server1, "someSuperOpUser1"), "User someSuperOpUser1 doesn't exist");
		assertNotNull(userDao.findByNick(server1, "aOpUser1"), "User aOpUser1 doesn't exist");
		assertNotNull(userDao.findByNick(server1, "aSuperOpUser1"), "User aSuperOpUser1 doesn't exist");
		assertNotNull(userDao.findByNick(server1, "aNormalUser1"), "User aNormalUser1 doesn't exist");
		//List remainingUsers = session.createQuery("from ServerDAOHb WHERE nick != 'someOpUser1' AND nick != 'someNormalUser1' AND nick != 'someSuperOpUser1' AND nick != 'aOpUser1' "
		//		+ "AND nick != 'aSuperOpUser1' AND nick != 'aNormalUser1'").list();
		//assertEquals(remainingUsers.size(), 0, "Extra users after deletion: " + StringUtils.join(remainingUsers.toArray(), ", "));
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected <T extends GenericEntry> void deleteEntity(GenericDAO<T> dao, T entry) {
		dao.delete(entry);
	}
}
