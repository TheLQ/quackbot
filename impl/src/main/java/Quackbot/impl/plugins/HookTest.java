/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Quackbot.impl.plugins;

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
