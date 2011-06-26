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
import org.pircbotx.hooks.events.MessageEvent;
import org.quackbot.events.CommandEvent;
import org.quackbot.hooks.Command;
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

	@Test
	public void executeOnCommandLongTest() throws Exception {
		//Build the CommandEvent
		final String[][] args = new String[3][];
		Command command = new Command("testcommand") {
			public String onCommand(CommandEvent event, String hello0, String hello1, String[] hello23, String hello4) throws Exception {
				args[0] = new String[]{hello0};
				args[1] = new String[]{hello1};
				args[2] = hello23;
				args[3] = new String[]{hello4};
				return "Success";
			}
		};
		CommandEvent event = new CommandEvent(command, new MessageEvent(null, null, null, message), null, null, message, "?testCommand", hook.getArgs(message));

		//Execute and verify values
		String returned = hook.executeOnCommandLong(event);
		assertEquals(returned, "Success", "onCommandLong doesn't return expected value");
		assertTrue(Arrays.equals(args[0], new String[]{"hello0"}));
		assertTrue(Arrays.equals(args[1], new String[]{"hello1"}));
		assertTrue(Arrays.equals(args[2], new String[]{"hello2", "hello3"}));
		assertTrue(Arrays.equals(args[3], new String[]{"hello4"}));
	}
}
