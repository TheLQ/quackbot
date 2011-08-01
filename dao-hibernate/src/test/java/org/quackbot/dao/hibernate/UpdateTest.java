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

import org.quackbot.dao.model.ChannelEntry;
import org.quackbot.dao.model.ServerEntry;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class UpdateTest extends GenericHbTest {
	protected final String aString = "I'm some really long multiword string";

	@Test
	public void serverInfoUpdateTest() {
		setupEnviornment();

		//Change server addresses and other info in the enviornment
		serverInfoUpdateTest0();

		//Make sure the values were updated
		for (ServerEntry curServer : serverDao.findAll()) {
			assertTrue(curServer.getAddress().endsWith("-test"), "Address doesn't end with -test, wasn't updated? (Real value: " + curServer.getAddress());
			assertEquals(curServer.getPassword(), "testPassword", "Password wasn't udpated");
			assertEquals((int) curServer.getPort(), 9876, "Port wasn't updated");
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected void serverInfoUpdateTest0() {
		for (ServerEntry curServer : serverDao.findAll()) {
			curServer.setAddress(curServer.getAddress() + "-test");
			curServer.setPassword("testPassword");
			curServer.setPort(9876);
		}
	}

	@Test
	public void channelInfoUpdateTest() {
		setupEnviornment();

		//Change server addresses and other info in the enviornment
		final long createTimestamp = 1513215L;
		final long topicTimestamp = 4588496L;
		channelInfoUpdateTest0(createTimestamp, topicTimestamp);

		//Make sure the values were updated
		//TODO: Use test with server id?
		for (ServerEntry curServer : serverDao.findAll())
			for (ChannelEntry curChannel : curServer.getChannels()) {
				assertTrue(curChannel.getName().endsWith("-test"), "Name wasn't updated? (Real value: " + curChannel.getName());
				assertEquals(curChannel.getCreateTimestamp(), (Long) createTimestamp, "Create timestamp wasn't updated");
				assertEquals(curChannel.getTopicTimestamp(), (Long) topicTimestamp, "Topic timestamp wasn't updated");
				assertEquals(curChannel.getMode(), "testModes", "Mode wasn't updated");
				assertEquals(curChannel.getTopic(), aString, "Topic wasn't updated");
				assertEquals(curChannel.getPassword(), "aPassword-test", "Password wasn't updated");
				assertEquals(curChannel.getTopicSetter(), "SomeUser", "Topic setter wasn't updated");
			}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	protected void channelInfoUpdateTest0(long createTimestamp, long topicTimestamp) {
		for (ServerEntry curServer : serverDao.findAll())
			for (ChannelEntry curChannel : curServer.getChannels()) {
				curChannel.setName(curChannel.getName() + "-test");
				curChannel.setCreateTimestamp(createTimestamp);
				curChannel.setTopicTimestamp(topicTimestamp);
				curChannel.setMode("testModes");
				curChannel.setTopic(aString);
				curChannel.setPassword("aPassword-test");
				curChannel.setTopicSetter("SomeUser");
			}
	}
}