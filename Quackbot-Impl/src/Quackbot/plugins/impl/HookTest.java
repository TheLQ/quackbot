/**
 * @(#)HookTest.java
 *
 * Copyright Leon Blakey/Lord.Quackstar, 2009-2010
 *
 * This file is part of Quackbot
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
package Quackbot.plugins.impl;


import Quackbot.hook.Hook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Java cmd test
 * @author Lord.Quackstar
 */
public class HookTest extends Hook {
	private static Logger log = LoggerFactory.getLogger(HookTest.class);
	public HookTest() {
		super("HookTest");
	}
}
