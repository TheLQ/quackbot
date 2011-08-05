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
package org.quackbot.impl;

import org.quackbot.impl.plugins.JavaTest;
import lombok.extern.slf4j.Slf4j;
import org.quackbot.Controller;
import org.quackbot.hooks.loaders.JavaHookLoader;

/**
 * Main Class for implementation.
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class ExampleMain extends HibernateMain {
	/**
	 * Main method of Implementation
	 * @param args Passed parameters (ignored)
	 */
	public static void main(String[] args) {		
		Controller controller = new Controller();
		controller.setDefaultName("Quackbot");
		controller.addPrefix("?");
		try {
			controller.getHookManager().addHook(JavaHookLoader.load(new JavaTest()));
		} catch (Exception ex) {
			log.error("Can't load hook Javatest", ex);
		}

		//Start
		controller.start();
	}
}
