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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.quackbot.dao.model.AdminEntry;
import org.quackbot.dao.model.ChannelEntry;
import org.quackbot.dao.model.UserEntry;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class RelationTest extends GenericHbTest {
	@Transactional
	@Test(description = "Make sure when saving a server the assoicated channels get created")
	public void ServerChannelTest() {
		//Setup
		ServerChannelTest0();

		//Make sure they exist
		List<ServerEntryHibernate> servers = serverDao.findAll();
		assertEquals(servers.size(), 1, "Too many/not enough servers");
		ServerEntryHibernate fetchedServer = servers.get(0);
		assertEquals((long) fetchedServer.getId(), 1, "Server ID is wrong");
		assertEquals(fetchedServer.getAddress(), "some.host", "Server host is wrong");

		Set<ChannelEntry> channels = fetchedServer.getChannels();
		assertEquals(channels.size(), 1, "Channels size is wrong (no channel)");
		ChannelEntry fetchedChannel = channels.iterator().next();
		assertNotNull(fetchedChannel, "Channel is null but is in the channel list");
		assertEquals((long) fetchedChannel.getId(), 1, "Channel ID is wrong");
		assertEquals(fetchedChannel.getName(), "#channelName", "Channel name doesn't match");
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected void ServerChannelTest0() {
		ServerEntryHibernate server = serverDao.create("some.host");
		server.getChannels().add(channelDao.create("#channelName"));
		serverDao.save(server);
	}

	@Transactional
	@Test(dependsOnMethods = "ServerChannelTest")
	public void ChannelUserStatusTest() {
		ChannelUserStatusTest0();

		//Make sure the statuses still work (use known working methods
		List<ChannelEntryHibernate> channels = channelDao.findAll();
		assertEquals(channels.size(), 1, "Too many/not enough channels");
		ChannelEntryHibernate chan = channels.get(0);

		//Verify there are 3 users
		List<String> usersShouldExist = new ArrayList();
		usersShouldExist.add("someNickNormal");
		usersShouldExist.add("someNickOp");
		usersShouldExist.add("someNickVoice");
		for (UserEntry curUser : chan.getUsers()) {
			String nick = curUser.getNick();
			assertTrue(usersShouldExist.contains(nick), "Unknown user: " + nick);
			usersShouldExist.remove(nick);
		}
		assertEquals(usersShouldExist.size(), 0, "Users missing from channel's getUsers(" + chan.getUsers().size() + "): " + StringUtils.join(usersShouldExist, ", "));

		//Verify normal
		assertEquals(chan.getNormalUsers().size(), 1, "Normal user list size is wrong");
		UserEntry user = chan.getNormalUsers().iterator().next();
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
	
	@Transactional(propagation= Propagation.REQUIRES_NEW)
	protected void ChannelUserStatusTest0() {
		ServerEntryHibernate server = serverDao.create("some.host");
		ChannelEntryHibernate channel = channelDao.create("#someChannel");
		server.getChannels().add(channel);
		channel.getUsers().add(userDao.create("someNickNormal"));
		channel.getOps().add(userDao.create("someNickOp"));
		channel.getVoices().add(userDao.create("someNickVoice"));
	}

	@Transactional
	@Test(dependsOnMethods = "ChannelUserStatusTest")
	public void UserSameTest() {
		generateEnviornment(1, null);

		//Load channelUser1 from #aChannel1
		ChannelEntry channel = channelDao.findByName(serverDao.findByAddress("irc.host1"), "#aChannel1");
		UserEntry aChannelUser = null;
		for (UserEntry curUser : channel.getUsers())
			if (curUser.getNick().equals("channelUser1"))
				aChannelUser = curUser;
		assertNotNull(aChannelUser, "channelUser1 doesn't exist in channel #aChannel1");

		//Load channelUser1 from #aChannel1
		channel = channelDao.findByName(serverDao.findByAddress("irc.host1"), "#someChannel1");
		UserEntry someChannelUser = null;
		for (UserEntry curUser : channel.getUsers())
			if (curUser.getNick().equals("channelUser1"))
				someChannelUser = curUser;
		assertNotNull(someChannelUser, "channelUser1 doesn't exist in channel #someChannel1");

		//Make sure they are equal
		assertEquals(aChannelUser, someChannelUser, "channelUser1's from #aChannel1 and #someChannel1 are not equal");
		assertEquals(aChannelUser.getId(), someChannelUser.getId(), "channelUser1's from #aChannel1 and #someChannel1 ids don't match");
	}

	@Transactional
	@Test(dependsOnMethods = "ServerChannelTest")
	public void AdminServerTest() {
		setupEnviornment();

		//Verify server1 admins
		ServerEntryHibernate fetchedServer1 = serverDao.findById(1L);
		List<String> adminsShouldExist = new ArrayList();
		adminsShouldExist.add("globalAdmin");
		adminsShouldExist.add("serverAdmin1");
		AdminEntry fetchedGlobalAdmin1 = null;
		for (AdminEntry curAdmin : fetchedServer1.getAdmins()) {
			String name = curAdmin.getName();
			if (name.equals("globalAdmin"))
				fetchedGlobalAdmin1 = curAdmin;
			assertTrue(adminsShouldExist.contains(name), "Unknown server1 admin: " + curAdmin);
			adminsShouldExist.remove(name);
		}
		assertEquals(adminsShouldExist.size(), 0, "Admin(s) missing from server1's getAdmins: " + StringUtils.join(adminsShouldExist, ", "));
		assertNotNull(fetchedGlobalAdmin1, "Global admin not found in server1");

		//Verify server2 admins
		ServerEntryHibernate fetchedServer2 = serverDao.findById(2L);
		adminsShouldExist = new ArrayList();
		adminsShouldExist.add("globalAdmin");
		adminsShouldExist.add("serverAdmin2");
		AdminEntry fetchedGlobalAdmin2 = null;
		for (AdminEntry curAdmin : fetchedServer2.getAdmins()) {
			String name = curAdmin.getName();
			if (name.equals("globalAdmin"))
				fetchedGlobalAdmin2 = curAdmin;
			assertTrue(adminsShouldExist.contains(name), "Unknown server2 admin: " + curAdmin);
			adminsShouldExist.remove(name);
		}
		assertEquals(adminsShouldExist.size(), 0, "Admin(s) missing from server2's getAdmins: " + StringUtils.join(adminsShouldExist, ", "));
		assertNotNull(fetchedGlobalAdmin2, "Global admin not found in server2");

		//Make sure global admins match
		assertEquals(fetchedGlobalAdmin1.getId(), fetchedGlobalAdmin2.getId(), "Global admins IDs do not match");
		assertEquals(fetchedGlobalAdmin1, fetchedGlobalAdmin2, "Global admins do not match in .equals()");
	}

	@Transactional
	@Test(dependsOnMethods = "AdminServerTest")
	public void AdminChannelTest() {
		setupEnviornment();

		//Verify #aChannel1 admins
		ChannelEntry aChannel1 = channelDao.findByName(serverDao.findById(1L), "#aChannel1");
		List<String> adminsShouldExist = new ArrayList();
		adminsShouldExist.add("channelAdmin1");
		adminsShouldExist.add("aChannelAdmin1");
		AdminEntry fetchedChannelAdmin = null;
		for (AdminEntry curAdmin : aChannel1.getAdmins()) {
			String name = curAdmin.getName();
			if (name.equals("channelAdmin1"))
				fetchedChannelAdmin = curAdmin;
			assertTrue(adminsShouldExist.contains(name), "Unknown #aChannel1 admin: " + curAdmin);
			adminsShouldExist.remove(name);
		}
		assertEquals(adminsShouldExist.size(), 0, "Admin(s) missing from #aChannel1's getAdmins: " + StringUtils.join(adminsShouldExist, ", "));
		assertNotNull(fetchedChannelAdmin, "Channel admin not found in #aChannel1");

		//Verify #someChannel1 admins
		ChannelEntryHibernate someChannel1 = channelDao.findByName(serverDao.findById(1L), "#someChannel1");
		adminsShouldExist = new ArrayList();
		adminsShouldExist.add("channelAdmin1");
		adminsShouldExist.add("someChannelAdmin1");
		AdminEntry fetchedChannelAdmin2 = null;
		for (AdminEntry curAdmin : someChannel1.getAdmins()) {
			String name = curAdmin.getName();
			if (name.equals("channelAdmin1"))
				fetchedChannelAdmin2 = curAdmin;
			assertTrue(adminsShouldExist.contains(name), "Unknown #someChannel1 admin: " + curAdmin);
			adminsShouldExist.remove(name);
		}
		assertEquals(adminsShouldExist.size(), 0, "Admin(s) missing from #someChannel1's getAdmins: " + StringUtils.join(adminsShouldExist, ", "));
		assertNotNull(fetchedChannelAdmin2, "Global admin not found in #someChannel1");

		//Make sure global admins match
		assertEquals(fetchedChannelAdmin.getId(), fetchedChannelAdmin2.getId(), "Global admins IDs do not match");
		assertEquals(fetchedChannelAdmin, fetchedChannelAdmin2, "Global admins do not match in .equals()");
	}
}
