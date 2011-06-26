package org.quackbot.hooks.core;

import java.util.Arrays;
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
	protected String message = "?command hello1 hello2 hello3 hello4 hello5";

	@Test
	public void getArgsTest() {
		String[] args = hook.getArgs(message);
		assertEquals(args[0], "hello1", "Args 0 doesn't match given");
		assertEquals(args[1], "hello2", "Args 1 doesn't match given");
		assertEquals(args[2], "hello3", "Args 2 doesn't match given");
		assertEquals(args[3], "hello4", "Args 3 doesn't match given");
		assertEquals(args[4], "hello5", "Args 4 doesn't match given");
	}

	@Test
	public void executeOnCommandLongTest() throws Exception {
		//Build the CommandEvent
		final String[][] args = new String[3][];
		Command command = new Command("testcommand") {
			public String onCommand(CommandEvent event, String hello1, String hello2, String[] hello34, String hello5) throws Exception {
				args[0] = new String[]{hello1};
				args[1] = new String[]{hello2};
				args[2] = hello34;
				args[3] = new String[]{hello5};
				return "Success";
			}
		};
		CommandEvent event = new CommandEvent(command, null, null, null, message, "?testCommand", hook.getArgs(message));
		
		//Execute and verify values
		String returned = hook.executeOnCommandLong(event);
		assertEquals(returned, "Success", "onCommandLong doesn't return expected value");
		assertTrue(Arrays.equals(args[0], new String[]{"hello1"}));
		assertTrue(Arrays.equals(args[1], new String[]{"hello2"}));
		assertTrue(Arrays.equals(args[2], new String[]{"hello3", "hello4"}));
		assertTrue(Arrays.equals(args[3], new String[]{"hello5"}));
	}
}
