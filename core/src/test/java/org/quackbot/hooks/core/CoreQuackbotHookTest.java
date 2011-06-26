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

import java.lang.reflect.Array;
import java.util.Arrays;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.hooks.events.MessageEvent;
import org.quackbot.events.CommandEvent;
import org.quackbot.hooks.Command;
import org.testng.annotations.DataProvider;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class CoreQuackbotHookTest {
	protected CoreQuackbotHook hook = new CoreQuackbotHook();
	protected String message4 = "?command hello0 hello1 hello2 hello3 hello4";
	protected String message3 = "?command hello0 hello1 hello2 hello3";

	@Test
	public void getArgsTest() {
		String[] args = hook.getArgs(message4);
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
		assertTrue(Arrays.deepEquals(command.getArgs(), expectedArgs));
	}
	
	@DataProvider(name = "onCommandLongTests")
	public Object[][] getOnCommandLongTests() {
		Object[][] test = {
			{
				message4,
				new OnCommandLong() {
					public String onCommand(CommandEvent event, String hello0, String hello1, String[] hello23, String hello4) throws Exception {
						args = (String[][]) ArrayUtils.add(args, makeArray(hello0));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello1));
						args = (String[][]) ArrayUtils.add(args, hello23);
						args = (String[][]) ArrayUtils.add(args, makeArray(hello4));
						return "Success";
					}
				}, new String[][] {
					makeArray("hello0"),
					makeArray("hello1"),
					makeArray("hello2", "hello3"),
					makeArray("hello4")
				}
			},
			{
				message4,
				new OnCommandLong() {
					public String onCommand(CommandEvent event, String hello0, String hello1, String hello2, String[] hello34) throws Exception {
						args = (String[][]) ArrayUtils.add(args, makeArray(hello0));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello1));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello2));
						args = (String[][]) ArrayUtils.add(args, hello34);
						return "Success";
					}
				}, new String[][] {
					makeArray("hello0"),
					makeArray("hello1"),
					makeArray("hello2"),
					makeArray("hello3", "hello4")
				}
			},
			{
				message4,
				new OnCommandLong() {
					public String onCommand(CommandEvent event, String[] hello01, String hello2, String hello3, String hello4) throws Exception {
						args = (String[][]) ArrayUtils.add(args, hello01);
						args = (String[][]) ArrayUtils.add(args, makeArray(hello2));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello3));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello4));
						return "Success";
					}
				}, new String[][] {
					makeArray("hello0"),
					makeArray("hello20"),
					makeArray("hello2"),
					makeArray("hello3", "hello4")
				}
			},
			{
				message3,
				new OnCommandLong() {
					public String onCommand(CommandEvent event, String hello0, String hello1, String[] hello23, String hello4) throws Exception {
						args = (String[][]) ArrayUtils.add(args, makeArray(hello0));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello1));
						args = (String[][]) ArrayUtils.add(args, hello23);
						args = (String[][]) ArrayUtils.add(args, makeArray(hello4));
						return "Success";
					}
				}, new String[][] {
					makeArray("hello0"),
					makeArray("hello1"),
					makeArray("hello2", "hello3"),
					new String[]{null}
				}
			},
			{
				message3,
				new OnCommandLong() {
					public String onCommand(CommandEvent event, String hello0, String hello1, String hello2, String[] hello34) throws Exception {
						args = (String[][]) ArrayUtils.add(args, makeArray(hello0));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello1));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello2));
						args = (String[][]) ArrayUtils.add(args, hello34);
						return "Success";
					}
				}, new String[][] {
					makeArray("hello0"),
					makeArray("hello1"),
					makeArray("hello2"),
					makeArray("hello3", null)
				}
			},
			{
				message3,
				new OnCommandLong() {
					public String onCommand(CommandEvent event, String[] hello01, String hello2, String hello3, String hello4) throws Exception {
						args = (String[][]) ArrayUtils.add(args, hello01);
						args = (String[][]) ArrayUtils.add(args, makeArray(hello2));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello3));
						args = (String[][]) ArrayUtils.add(args, makeArray(hello4));
						return "Success";
					}
				}, new String[][] {
					makeArray("hello0"),
					makeArray("hello1"),
					makeArray("hello2"),
					makeArray("hello3", null)
				}
			}
		};
		return test;
	}

	protected <T> T[] makeArray(T... args) {
		return args;
	}

	protected class OnCommandLong extends Command {
		@Getter
		String[][] args = new String[0][];
	}
}
