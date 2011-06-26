package org.quackbot.hooks.core;

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
}
