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
package org.quackbot.hooks.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.quackbot.Bot;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.quackbot.Controller;
import org.quackbot.data.AdminStore;
import org.quackbot.data.ChannelStore;
import org.quackbot.data.DataStore;
import org.quackbot.data.ServerStore;
import org.quackbot.err.QuackbotException;
import org.quackbot.events.CommandEvent;
import org.quackbot.hooks.Command;
import org.quackbot.hooks.java.Parameters;
import org.testng.annotations.DataProvider;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class CoreQuackbotHookTest {
	DataStore store = new DataStore() {
		public AdminStore newAdminStore(String name) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public ChannelStore newChannelStore(String name) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public ServerStore newServerStore(String address) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Set<ServerStore> getServers() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Set<AdminStore> getAllAdmins() {
			return new HashSet();
		}

		public void close() throws Exception {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	};
	Controller controller = new Controller(store);
	Bot bot = new Bot(controller, -1, Executors.newCachedThreadPool());
	Channel channel = new Channel(bot, "#someChannel") {
	};
	User user = new User(bot, "SomeUser") {
	};
	protected CoreQuackbotHook hook = new CoreQuackbotHook() {
		@Override
		public Controller getController() {
			return controller;
		}

		@Override
		public Bot getBot() {
			return bot;
		}
	};
	protected String args4 = "hello0 hello1 hello2 hello3 hello4";
	protected String args3 = "hello0 hello1 hello2 hello3";

	public CoreQuackbotHookTest() {
		controller.getHookManager().addHook(hook);
		controller.addPrefix("?");
	}

	@Test
	public void getArgsTest() {
		String[] args = hook.getArgs("?someCommand " + args4);
		assertEquals(args.length, 5, "Too many or too few args given from getArgs");
		assertEquals(args[0], "hello0", "Args 0 doesn't match given");
		assertEquals(args[1], "hello1", "Args 1 doesn't match given");
		assertEquals(args[2], "hello2", "Args 2 doesn't match given");
		assertEquals(args[3], "hello3", "Args 3 doesn't match given");
		assertEquals(args[4], "hello4", "Args 4 doesn't match given");
	}

	@Test(dataProvider = "onCommandLongTests")
	public void executeOnCommandLongTest(String cmdMessage, OnCommandLong command, String[][] expectedArgs) throws Exception {
		//Build the CommandEvent
		CommandEvent event = new CommandEvent(command, new MessageEvent(null, null, null, cmdMessage), null, null, cmdMessage, "?testCommand", hook.getArgs(cmdMessage));

		//Execute and verify values
		String returned = hook.executeOnCommandLong(event);
		assertEquals(returned, "Success", "onCommandLong doesn't return expected value");

		log.debug("Current command: " + command.getName());
		logMultiArray(expectedArgs, "Expected args");
		logMultiArray(command.getArgs(), "Given args");

		assertTrue(Arrays.deepEquals(command.getArgs(), expectedArgs), "Command test " + command.getName() + " args don't equal");
	}

	/**
	 * Instead of only testing onCommandLong, test feeding into onMessage
	 */
	@Test(dataProvider = "onCommandLongTests")
	public void executeTest(String cmdMessage, OnCommandLong command, String[][] expectedArgs) throws Exception {
		//Remove the prefix
		cmdMessage = cmdMessage.substring(1);

		//Build the messageEvent 
		final StringBuilder response = new StringBuilder();
		MessageEvent messageEvent = new MessageEvent(null, channel, null, cmdMessage) {
			@Override
			public void respond(String commandResponse) {
				if (commandResponse != null)
					response.append(commandResponse);
			}
		};

		//Add our test listener
		controller.getHookManager().addHook(command);

		//Feed into onMessage
		log.trace("Sending message " + cmdMessage);
		hook.execute(messageEvent, channel, null, cmdMessage);

		//Verify the results
		assertEquals(response.toString(), "Success", "onCommandLong in test " + command.getName() + " doesn't return expected value");
		assertTrue(Arrays.deepEquals(command.getArgs(), expectedArgs), "Command test " + command.getName() + " args don't equal");

		//Remove it to cleanup
		controller.getHookManager().removeHook(command);
	}

	@Test
	public void onMessageTest() throws Exception {
		//Generate a simple message event
		final String origMessage = "?hello " + args4;
		final MessageEvent messageEvent = new MessageEvent(null, channel, null, origMessage);

		//This will notify us that execute actually ran. Yes, its ugly, but boolean is final
		final StringBuilder executed = new StringBuilder("false");

		//Test hook that makes sure all the information passed into execute is good
		CoreQuackbotHook tempHook = new CoreQuackbotHook() {
			@Override
			public Bot getBot() {
				return bot;
			}

			@Override
			protected void execute(Event event, Channel chan, User user, String message) throws Exception {
				assertEquals(event, messageEvent, "Event passed to execute doesn't match given");
				assertEquals(chan, channel, "Channel does not match given");
				assertEquals(user, CoreQuackbotHookTest.this.user, "User does not match given");
				assertEquals(message, origMessage.substring(1), "Message does not match given");
				executed.setLength(0);
				executed.append("true");
			}
		};

		tempHook.onMessage(messageEvent);
	}
	
	@Test
	public void onPrivateMessageTest() throws Exception {
		//Generate a simple message event
		final String origMessage = "hello " + args4;
		final PrivateMessageEvent pmEvent = new PrivateMessageEvent(bot, user, origMessage);

		//This will notify us that execute actually ran. Yes, its ugly, but boolean is final
		final StringBuilder executed = new StringBuilder("false");

		//Test hook that makes sure all the information passed into execute is good
		CoreQuackbotHook tempHook = new CoreQuackbotHook() {
			@Override
			public Bot getBot() {
				return bot;
			}

			@Override
			protected void execute(Event event, Channel chan, User user, String message) throws Exception {
				assertEquals(event, pmEvent, "Event passed to execute doesn't match given");
				assertEquals(chan, null, "Channel does not match given");
				assertEquals(user, CoreQuackbotHookTest.this.user, "User does not match given");
				assertEquals(message, origMessage, "Message does not match given");
				executed.setLength(0);
				executed.append("true");
			}
		};

		tempHook.onPrivateMessage(pmEvent);
	}

	@DataProvider(name = "onCommandLongTests")
	public Object[][] getOnCommandLongTests() {
		Object[][] test = {
			{
				"?Message4-Array23 " + args4,
				new OnCommandLong("Message4-Array23", 5, 0) {
					public String onCommand(CommandEvent event, String hello0, String hello1, String[] hello23, String hello4) throws Exception {
						args = (String[][]) ArrayUtils.add(args, makeArray(hello0));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello1));
						args = (String[][]) ArrayUtils.add(args, hello23);
						args = (String[][]) ArrayUtils.add(args, makeArray(hello4));
						return "Success";
					}
				}, new String[][]{
					{"hello0"},
					{"hello1"},
					{"hello2", "hello3"},
					{"hello4"}
				}
			},
			{
				"?Message4-Array34 " + args4,
				new OnCommandLong("Message4-Array34", 5, 0) {
					public String onCommand(CommandEvent event, String hello0, String hello1, String hello2, String[] hello34) throws Exception {
						args = (String[][]) ArrayUtils.add(args, makeArray(hello0));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello1));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello2));
						args = (String[][]) ArrayUtils.add(args, hello34);
						return "Success";
					}
				}, new String[][]{
					{"hello0"},
					{"hello1"},
					{"hello2"},
					{"hello3", "hello4"}
				}
			},
			{
				"?Message4-Array01 " + args4,
				new OnCommandLong("Message4-Array01", 5, 0) {
					public String onCommand(CommandEvent event, String[] hello01, String hello2, String hello3, String hello4) throws Exception {
						args = (String[][]) ArrayUtils.add(args, hello01);
						args = (String[][]) ArrayUtils.add(args, makeArray(hello2));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello3));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello4));
						return "Success";
					}
				}, new String[][]{
					{"hello0", "hello1"},
					{"hello2"},
					{"hello3"},
					{"hello4"}
				}
			},
			{
				"?Message3-Array23 " + args3,
				new OnCommandLong("Message3-Array23", 4, 0) {
					public String onCommand(CommandEvent event, String hello0, String hello1, String[] hello23, String hello4) throws Exception {
						args = (String[][]) ArrayUtils.add(args, makeArray(hello0));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello1));
						args = (String[][]) ArrayUtils.add(args, hello23);
						args = (String[][]) ArrayUtils.add(args, makeArray(hello4));
						return "Success";
					}
				}, new String[][]{
					{"hello0"},
					{"hello1"},
					{"hello2", "hello3"},
					{null}
				}
			},
			{
				"?Message3-Array34 " + args3,
				new OnCommandLong("Message3-Array34", 4, 0) {
					public String onCommand(CommandEvent event, String hello0, String hello1, String hello2, String[] hello34) throws Exception {
						args = (String[][]) ArrayUtils.add(args, makeArray(hello0));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello1));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello2));
						args = (String[][]) ArrayUtils.add(args, hello34);
						return "Success";
					}
				}, new String[][]{
					{"hello0"},
					{"hello1"},
					{"hello2"},
					{"hello3"} //Do not expect null as the last element as its an optional parameter
				}
			},
			{
				"?Message3-Array01 " + args3,
				new OnCommandLong("Message3-Array01", 4, 0) {
					public String onCommand(CommandEvent event, String[] hello01, String hello2, String hello3, String hello4) throws Exception {
						args = (String[][]) ArrayUtils.add(args, hello01);
						args = (String[][]) ArrayUtils.add(args, makeArray(hello2));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello3));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello4));
						return "Success";
					}
				}, new String[][]{
					{"hello0", "hello1"},
					{"hello2"},
					{"hello3"},
					{null}
				}
			}
		};
		return test;
	}

	/************************* UTILS ************************/
	/**
	 * Simpler way to make arrays dynamically
	 * @param <T> The type of array to create
	 * @param args Elements in the array
	 * @return A completed array
	 */
	protected <T> T[] makeArray(T... args) {
		return args;
	}

	protected void logMultiArray(Object[][] array, String message) {
		StringBuilder sb = new StringBuilder(message);
		for (int outer = 0; outer < array.length; outer++)
			sb.append("\n").append(outer).append(" - ").append(StringUtils.join(array[outer], ", "));
		log.trace(sb.toString().trim());
	}

	/**
	 * Simple test class for extracting arguments from the long version of onCommand 
	 */
	@Getter
	protected class OnCommandLong extends Command {
		String[][] args = new String[0][];
		int requiredParams = 0;
		int optionalParams = 0;

		public OnCommandLong(String name, int requiredParams, int optionalParams) {
			super(name);
			this.requiredParams = requiredParams;
			this.optionalParams = optionalParams;
		}
	}
}