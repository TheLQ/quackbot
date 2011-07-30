package org.quackbot.dao.hibernate;

import org.quackbot.dao.ChannelDAO;
import org.quackbot.dao.ServerDAO;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class UpdateTest extends GenericHbTest {
	protected final String aString = "I'm some really long multiword string";

	@Test
	public void serverInfoUpdate() {
		setupEnviornment();

		//Change server addresses and other info in the enviornment
		controller.beginTransaction();
		for (ServerDAO curServer : controller.getServers()) {
			curServer.setAddress(curServer.getAddress() + "-test");
			curServer.setPassword("testPassword");
			curServer.setPort(9876);
		}
		controller.endTransaction(true);
		controller.getSession().clear();

		//Make sure the values were updated
		controller.beginTransaction();
		for (ServerDAO curServer : controller.getServers()) {
			assertTrue(curServer.getAddress().endsWith("-test"), "Address doesn't end with -test, wasn't updated? (Real value: " + curServer.getAddress());
			assertEquals(curServer.getPassword(), "testPassword", "Password wasn't udpated");
			assertEquals((int) curServer.getPort(), 9867, "Port wasn't updated");
		}
		controller.endTransaction(true);
	}

	@Test
	public void channelInfoUpdate() {
		setupEnviornment();

		//Change server addresses and other info in the enviornment
		final long createTimestamp = 1513215L;
		final long topicTimestamp = 4588496L;
		controller.beginTransaction();
		for (ServerDAO curServer : controller.getServers())
			for (ChannelDAO curChannel : curServer.getChannels()) {
				curChannel.setName(curChannel.getName() + "-test");
				curChannel.setCreateTimestamp(createTimestamp);
				curChannel.setTopicTimestamp(topicTimestamp);
				curChannel.setMode("testModes");
				curChannel.setTopic(aString);
				curChannel.setPassword("aPassword-test");
				curChannel.setTopicSetter("SomeUser");
			}
		controller.endTransaction(true);
		controller.getSession().clear();

		//Make sure the values were updated
		controller.beginTransaction();
		for (ServerDAO curServer : controller.getServers()) {
			for (ChannelDAO curChannel : curServer.getChannels()) {
				assertTrue(curChannel.getName().endsWith("-test"), "Name wasn't updated? (Real value: " + curChannel.getName());
				assertEquals(curChannel.getCreateTimestamp(), (Long) createTimestamp, "Create timestamp wasn't updated");
				assertEquals(curChannel.getTopicTimestamp(), (Long) topicTimestamp, "Topic timestamp wasn't updated");
				assertEquals(curChannel.getMode(), "testModes", "Mode wasn't updated");
				assertEquals(curChannel.getTopic(), aString, "Topic wasn't updated");
				assertEquals(curChannel.getPassword(), "aPassword-test", "Password wasn't updated");
				assertEquals(curChannel.getTopicSetter(), "SomeUser", "Topic setter wasn't updated");
			}
			controller.endTransaction(true);
		}
	}
}