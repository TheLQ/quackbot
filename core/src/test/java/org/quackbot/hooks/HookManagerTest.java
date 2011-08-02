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
package org.quackbot.hooks;

import lombok.extern.slf4j.Slf4j;
import org.quackbot.hooks.core.CoreQuackbotHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@ContextConfiguration({"classpath:spring-core.xml"})
@Slf4j
public class HookManagerTest extends AbstractTestNGSpringContextTests {
	@Autowired
	protected HookManager hookManager;
	@Autowired
	protected CoreQuackbotHook hook;

	@Test
	public void autoWiredHooksTest() {
		assertTrue(hookManager.getHooks().contains(hook));
	}
}
