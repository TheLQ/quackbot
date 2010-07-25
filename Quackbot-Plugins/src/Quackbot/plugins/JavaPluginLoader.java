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
package Quackbot.plugins;

import Quackbot.Command;
import Quackbot.CommandManager;
import Quackbot.Controller;

import Quackbot.PluginLoader;
import Quackbot.err.QuackbotException;
import Quackbot.plugins.java.HelpDoc;
import Quackbot.hook.HookManager;
import Quackbot.hook.Hook;
import Quackbot.plugins.core.AdminHelp;
import Quackbot.plugins.core.Help;
import Quackbot.plugins.java.AdminOnly;
import Quackbot.plugins.java.Disabled;
import Quackbot.plugins.java.Parameters;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the global JavaBean/Utility for all Java written commands
 *
 * @author Lord.Quackstar
 */
public class JavaPluginLoader implements PluginLoader {
	/**
	 * Log4j logger
	 */
	private static Logger log = LoggerFactory.getLogger(JavaPluginLoader.class);

	static {
		Controller.addPluginLoader(new JSPluginLoader(), "js");
		JavaPluginLoader.load(new Help());
		JavaPluginLoader.load(new AdminHelp());
	}

	@Override
	public void load(File file) throws Exception {
		throw new UnsupportedOperationException("Java plugins cannot be loaded. Attempted to load " + file.getAbsolutePath());
	}

	public static void load(Command cmd) {
		if (cmd == null)
			return;
		try {
			Class<? extends Command> clazz = cmd.getClass();
			String name = clazz.getSimpleName();
			log.info("New Java Plugin " + name);
			//Command info creation
			boolean admin = clazz.isAnnotationPresent(AdminOnly.class);
			boolean enabled = !clazz.isAnnotationPresent(Disabled.class);
			//Param and syntax generation
			int requiredCount = (clazz.isAnnotationPresent(Parameters.class)) ? clazz.getAnnotation(Parameters.class).value() : 0;
			int optionalCount = (clazz.isAnnotationPresent(Parameters.class)) ? clazz.getAnnotation(Parameters.class).optional() : 0;
			String help = (clazz.isAnnotationPresent(HelpDoc.class)) ? clazz.getAnnotation(HelpDoc.class).value() : "";
			cmd.setup(name, help, admin, enabled, null, requiredCount, optionalCount);
			CommandManager.addPermanentCommand(cmd);
		} catch (QuackbotException e) {
			log.error("Unable to load Java plugin " + cmd.getName(), e);
		}

	}
}
