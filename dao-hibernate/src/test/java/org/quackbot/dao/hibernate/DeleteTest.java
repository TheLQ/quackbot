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
import org.apache.commons.lang.StringUtils;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.ObjectDeletedException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
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

		controller.beginTransaction();
		//Grab the global admin and delete it
		Criteria query = controller.getSession().createCriteria(AdminDAOHb.class);
		query.add(Restrictions.eq("name", "globalAdmin"));
		((AdminDAOHb) query.uniqueResult()).delete();
		controller.endTransaction(true);

		controller.beginTransaction();
		//Make sure its gone from server1
		ServerDAOHb server1 = (ServerDAOHb) controller.getSession().createQuery("from ServerDAOHb WHERE SERVER_ID = 1").uniqueResult();
		assertEquals(server1.getAdmins().size(), 1, "Too many server1 admins: " + server1.getAdmins());
		assertEquals(server1.getAdmins().iterator().next().getName(), "serverAdmin1", "Remaining server1 admin name is wrong");

		//Make sure its gone from server2
		ServerDAOHb server2 = (ServerDAOHb) controller.getSession().createQuery("from ServerDAOHb WHERE SERVER_ID = 2").uniqueResult();
		assertEquals(server2.getAdmins().size(), 1, "Too many server2 admins: " + server2.getAdmins());
		assertEquals(server2.getAdmins().iterator().next().getName(), "serverAdmin2", "Remaining server2 admin name is wrong");
		controller.endTransaction(true);
	}

	@Test
	public void deleteAdminServerTest() {
		setupEnviornment();

		controller.beginTransaction();
		//Grab the someChannelAdmin2 admin and delete it
		Criteria query = controller.getSession().createCriteria(AdminDAOHb.class);
		query.add(Restrictions.eq("name", "serverAdmin1"));
		((AdminDAOHb) query.uniqueResult()).delete();
		controller.endTransaction(true);

		controller.beginTransaction();
		//Make sure its gone from server1
		ServerDAOHb server1 = (ServerDAOHb) controller.getSession().createQuery("from ServerDAOHb WHERE SERVER_ID = 1").uniqueResult();
		assertEquals(server1.getAdmins().size(), 1, "Too many server1 admins: " + server1.getAdmins());
		assertEquals(server1.getAdmins().iterator().next().getName(), "globalAdmin", "Remaining server1 admin name is wrong");
	}

	@Test
	public void deleteAdminChannelGlobalTest() {
		setupEnviornment();

		controller.beginTransaction();
		//Grab the channel admin and delete it
		Criteria query = controller.getSession().createCriteria(AdminDAOHb.class);
		query.add(Restrictions.eq("name", "channelAdmin1"));
		((AdminDAOHb) query.list().get(0)).delete();
		controller.endTransaction(true);

		controller.beginTransaction();
		//Make sure its gone from #aChannel1
		ChannelDAOHb aChannel = (ChannelDAOHb) controller.getSession().createQuery("from ChannelDAOHb WHERE name = '#aChannel1'").uniqueResult();
		assertEquals(aChannel.getAdmins().size(), 1, "Too many #aChannel1 admins: " + aChannel.getAdmins());
		assertEquals(aChannel.getAdmins().iterator().next().getName(), "aChannelAdmin1", "Remaining #aChannel1 admin name is wrong");

		//Make sure its gone from #someChannel1
		ChannelDAOHb someChannel = (ChannelDAOHb) controller.getSession().createQuery("from ChannelDAOHb WHERE name = '#someChannel1'").uniqueResult();
		assertEquals(someChannel.getAdmins().size(), 1, "Too many #someChannel1 admins: " + someChannel.getAdmins());
		assertEquals(someChannel.getAdmins().iterator().next().getName(), "someChannelAdmin1", "Remaining #someChannel1 admin name is wrong");
	}

	@Test
	public void deleteAdminChannelTest() {
		setupEnviornment();

		controller.beginTransaction();
		//Grab the someChannelAdmin2 admin and delete it
		Criteria query = controller.getSession().createCriteria(AdminDAOHb.class);
		query.add(Restrictions.eq("name", "someChannelAdmin2"));
		((AdminDAOHb) query.uniqueResult()).delete();
		controller.endTransaction(true);

		controller.beginTransaction();
		//Make sure its gone from #someChannel1
		ChannelDAOHb someChannel = (ChannelDAOHb) controller.getSession().createQuery("from ChannelDAOHb WHERE name = '#someChannel2'").uniqueResult();
		assertEquals(someChannel.getAdmins().size(), 1, "Too many #someChannel2 admins: " + someChannel.getAdmins());
		assertEquals(someChannel.getAdmins().iterator().next().getName(), "channelAdmin2", "Remaining #someChannel1 admin name is wrong");
	}

	/**
	 * WARNING: THIS TEST IS BROKEN
	 */
	@Test(successPercentage = 0, expectedExceptions = ObjectDeletedException.class)
	public void deleteChannelTest() {
		setupEnviornment();

		controller.beginTransaction();
		//Grab the someChannelAdmin2 admin and delete it
		Criteria query = controller.getSession().createCriteria(ChannelDAOHb.class);
		query.add(Restrictions.eq("name", "#someChannel2"));
		((ChannelDAOHb) query.uniqueResult()).delete();
		controller.endTransaction(true);

		controller.beginTransaction();
		Session session = controller.getSession();
		//Make sure other channels still exist
		assertNotNull(session.createQuery("from ChannelDAOHb WHERE name = '#aChannel1'"), "#aChannel1 doesn't exist");
		assertNotNull(session.createQuery("from ChannelDAOHb WHERE name = '#someChannel1'"), "#someChannel1 doesn't exist");
		assertNotNull(session.createQuery("from ChannelDAOHb WHERE name = '#aChannel2'"), "#aChannel2 doesn't exist");
		List remainingChannels = session.createQuery("from ChannelDAOHb WHERE name != '#aChannel1' AND name != '#someChannel1' AND name != '#aChannel2'").list();
		assertEquals(remainingChannels.size(), 0, "Extra channels after deletion: " + StringUtils.join(remainingChannels.toArray(), ", "));

		//Make sure other servers still exist
		assertNotNull(session.createQuery("from ServerDAOHb WHERE address = 'irc.host1'"), "Server 1 doesn't exist");
		assertNotNull(session.createQuery("from ServerDAOHb WHERE address = 'irc.host2'"), "Server 2 doesn't exist");
		List remainingServers = session.createQuery("from ServerDAOHb WHERE address != 'irc.host1' AND address != 'irc.host2'").list();
		assertEquals(remainingServers.size(), 0, "Strange servers exists: " + StringUtils.join(remainingServers.toArray(), ", "));

		//Make sure other users still exist
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'someOpUser1'"), "User someOpUser1 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'someNormalUser1'"), "User someNormalUser1 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'someSuperOpUser1'"), "User someSuperOpUser1 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'aOpUser1'"), "User aOpUser1 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'aSuperOpUser1'"), "User aSuperOpUser1 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'aNormalUser1'"), "User aNormalUser1 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'aOpUser2'"), "User aOpUser2 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'aSuperOpUser2'"), "User aSuperOpUser2 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'aNormalUser2'"), "User aNormalUser2 doesn't exist");
		List remainingUsers = session.createQuery("from ServerDAOHb WHERE 'someOpUser1' AND 'someNormalUser1' AND 'someSuperOpUser1' AND 'aOpUser1' "
				+ "AND 'aSuperOpUser1' AND 'aNormalUser1' AND 'aOpUser2' AND 'aSuperOpUser2' AND 'aNormalUser2'").list();
		assertEquals(remainingUsers.size(), 0, "Extra users after deletion: " + StringUtils.join(remainingUsers.toArray(), ", "));
	}

	/**
	 * WARNING: THIS TEST IS BROKEN
	 */
	@Test(successPercentage = 0, expectedExceptions = ConstraintViolationException.class)
	public void deleteServerTest() {
		setupEnviornment();

		controller.beginTransaction();
		//Grab the some.host2 server and delete it
		Criteria query = controller.getSession().createCriteria(ServerDAOHb.class);
		query.add(Restrictions.eq("address", "irc.host2"));
		((ServerDAOHb) query.uniqueResult()).delete();
		controller.endTransaction(true);

		controller.beginTransaction();
		Session session = controller.getSession();
		//Make sure other channels still exist
		assertNotNull(session.createQuery("from ChannelDAOHb WHERE name = '#aChannel1'"), "#aChannel1 doesn't exist");
		assertNotNull(session.createQuery("from ChannelDAOHb WHERE name = '#someChannel1'"), "#someChannel1 doesn't exist");
		List remainingChannels = session.createQuery("from ChannelDAOHb WHERE name != '#aChannel1' AND name != '#someChannel1'").list();
		assertEquals(remainingChannels.size(), 0, "Extra channels after deletion: " + StringUtils.join(remainingChannels.toArray(), ", "));

		//Make sure other servers still exist
		assertNotNull(session.createQuery("from ServerDAOHb WHERE address = 'irc.host1'"), "Server 1 doesn't exist");
		List remainingServers = session.createQuery("from ServerDAOHb WHERE address != 'irc.host1'").list();
		assertEquals(remainingServers.size(), 0, "Strange servers exists: " + StringUtils.join(remainingServers.toArray(), ", "));

		//Make sure other users still exist
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'someOpUser1'"), "User someOpUser1 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'someNormalUser1'"), "User someNormalUser1 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'someSuperOpUser1'"), "User someSuperOpUser1 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'aOpUser1'"), "User aOpUser1 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'aSuperOpUser1'"), "User aSuperOpUser1 doesn't exist");
		assertNotNull(session.createQuery("from UserDAOHb WHERE nick = 'aNormalUser1'"), "User aNormalUser1 doesn't exist");
		List remainingUsers = session.createQuery("from ServerDAOHb WHERE nick != 'someOpUser1' AND nick != 'someNormalUser1' AND nick != 'someSuperOpUser1' AND nick != 'aOpUser1' "
				+ "AND nick != 'aSuperOpUser1' AND nick != 'aNormalUser1'").list();
		assertEquals(remainingUsers.size(), 0, "Extra users after deletion: " + StringUtils.join(remainingUsers.toArray(), ", "));
	}
}
