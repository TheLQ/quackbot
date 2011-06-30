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

import lombok.extern.slf4j.Slf4j;
import org.pircbotx.hooks.events.MessageEvent;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class JSHookLoaderTest {
	JSHookLoader loader = new JSHookLoader();

	@Test
	public void messageEventTest() throws Exception {
		JSHookLoader.JSHookWrapper hook = (JSHookLoader.JSHookWrapper) loader.load("JSPluginTest/SimpleHook.js");
		MessageEvent event = new MessageEvent(null, null, null, "Some message");
		hook.onEvent(event);
		assertEquals(hook.jsEngine.get("event"), event, "Event doesn't match given");
	}
}
