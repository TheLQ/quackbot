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
package org.quackbot.hooks.loaders;

import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import org.quackbot.Bot;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.pircbotx.hooks.events.MessageEvent;
import org.quackbot.events.CommandEvent;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import org.pircbotx.hooks.managers.GenericListenerManager;
import org.pircbotx.hooks.managers.ListenerManager;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class JSHookLoaderTest {
	JSHookLoader loader = new JSHookLoader();
	Bot bot;
	protected ListenerManager manager = new GenericListenerManager();

	public JSHookLoaderTest() {
		bot = mock(Bot.class);
		when(bot.getListenerManager()).thenReturn(manager);
	}

	@Test
	public void messageEventTest() throws Exception {
		JSHookLoader.JSHookWrapper hook = (JSHookLoader.JSHookWrapper) loader.load("JSPluginTest/SimpleHook.js");
		MessageEvent event = new MessageEvent(bot, null, null, "Some message");
		hook.onEvent(event);
		assertEquals(hook.jsEngine.get("event"), event, "Event doesn't match given");
	}

	@Test
	public void commandSimpleTest() throws Exception {
		JSHookLoader.JSCommandWrapper hook = (JSHookLoader.JSCommandWrapper) loader.load("JSPluginTest/Command_Simple.js");

		//Make sure arguments are setup correctly
		assertEquals(hook.getRequiredParams(), 1, "Required argument count is wrong");
		assertEquals(hook.getOptionalParams(), 0, "Optional argument count is wrong");

		//Test sending a command
		MessageEvent messageEvent = new MessageEvent(bot, null, null, "Some message");
		CommandEvent commandEvent = new CommandEvent(null, messageEvent, null, null, "?command someArg", null, new String[]{"someArg"});
		String returned = hook.onCommand(commandEvent);

		assertEquals(returned, "Success", "Returned value doesn't match given");
		assertEquals(hook.jsEngine.get("event"), commandEvent, "Event doesn't match given");
		assertEquals(hook.jsEngine.get("arg1"), "someArg", "Single argument doesn't match given");
	}

	@Test(dataProvider = "commandOptionalArrayDataProvider")
	public void commandOptionalArrayTest(String[] extraArgs) throws Exception {
		log.trace("--- Begin commandOptionalArrayTest with args " + Arrays.toString(extraArgs) + " ---");
		JSHookLoader.JSCommandWrapper hook = (JSHookLoader.JSCommandWrapper) loader.load("JSPluginTest/Command_OptionalArray.js");

		//Make sure arguments are setup correctly
		assertEquals(hook.getRequiredParams(), 2, "Required argument count is wrong");
		assertEquals(hook.getOptionalParams(), -1, "Optional argument count is wrong");

		//Test sending a command with several args
		MessageEvent messageEvent = new MessageEvent(bot, null, null, "Some message");
		CommandEvent commandEvent = new CommandEvent(null, messageEvent, null, null, "?command someArg1 someArg2 " + StringUtils.join(extraArgs, ", "), null, (String[]) ArrayUtils.addAll(new String[]{"someArg1", "someArg2"}, extraArgs));
		String returned = hook.onCommand(commandEvent);
		ScriptEngine engine = hook.jsEngine;

		assertEquals(returned, "Success", "Returned value doesn't match given");
		assertEquals(engine.get("event"), commandEvent, "Event doesn't match given");
		assertEquals(engine.get("arg1"), "someArg1", "First argument doesn't match given");
		assertEquals(engine.get("arg2"), "someArg2", "Second argument doesn't match given");

		//In order to verify the arg array, need to convert back to a Java array
		Object[] argArray = (Object[]) ((Invocable) engine).invokeMethod(engine.get("QuackUtils"), "toJavaArray", Object.class, engine.get("argArray3"));
		for (int i = 0; i < extraArgs.length; i++) {
			log.trace("Arg array: " + Arrays.toString(argArray));
			Object commandArg = argArray[i];
			log.trace("Got command arg successfully");
			Object extraArg = extraArgs[i];
			assertEquals(commandArg, extraArg, "Extra arg #" + i + " doesn't match given");
		}
	}

	@DataProvider
	public Object[][] commandOptionalArrayDataProvider() {
		Object[][] parameters = {{new String[]{}},
			{new String[]{"someArg3"}},
			{new String[]{"someArg3", "someArg4", "someArg5", "someArg6"}}
		};
		return parameters;
	}
}