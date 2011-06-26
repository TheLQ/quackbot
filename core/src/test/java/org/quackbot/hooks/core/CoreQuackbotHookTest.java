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
import org.apache.commons.lang.ArrayUtils;
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
public class CoreQuackbotHookTest {
	protected CoreQuackbotHook hook = new CoreQuackbotHook();
	protected String message = "?command hello0 hello1 hello2 hello3 hello4";

	@Test
	public void getArgsTest() {
		String[] args = hook.getArgs(message);
		assertEquals(args[0], "hello0", "Args 0 doesn't match given");
		assertEquals(args[1], "hello1", "Args 1 doesn't match given");
		assertEquals(args[2], "hello2", "Args 2 doesn't match given");
		assertEquals(args[3], "hello3", "Args 3 doesn't match given");
		assertEquals(args[4], "hello4", "Args 4 doesn't match given");
	}

	@Test(dataProvider = "onCommandLongTests")
	public void executeOnCommandLongTest(OnCommandLong command, String[][] expectedArgs) throws Exception {
		//Build the CommandEvent
		CommandEvent event = new CommandEvent(command, new MessageEvent(null, null, null, message), null, null, message, "?testCommand", hook.getArgs(message));

		//Execute and verify values
		String returned = hook.executeOnCommandLong(event);
		assertEquals(returned, "Success", "onCommandLong doesn't return expected value");
		assertEquals(command.getArgs(), expectedArgs);
	}

	@DataProvider(name = "onCommandLongTests")
	public Object[][] getOnCommandLongTests() {
		Object[][] test = {
			{
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
