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
package org.quackbot.plugins;

import Quackbot.Command;
import Quackbot.CommandManager;
import Quackbot.Controller;

import Quackbot.PluginLoader;
import Quackbot.err.QuackbotException;
import Quackbot.hook.Hook;
import Quackbot.hook.HookManager;
import org.quackbot.plugins.java.HelpDoc;
import org.quackbot.plugins.core.AdminHelp;
import org.quackbot.plugins.core.Help;
import org.quackbot.plugins.java.AdminOnly;
import org.quackbot.plugins.java.Disabled;
import org.quackbot.plugins.java.Optional;
import org.quackbot.plugins.java.Parameters;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
		HookManager.addPluginHook(new Hook("QBInit") {
			@Override
			public void onInit(Controller controller) throws Exception {
				controller.config.addPluginLoader(new JSPluginLoader(), "js");
				JavaPluginLoader.load(new Help());
				JavaPluginLoader.load(new AdminHelp());
			}
		});

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
			int requiredCount = 0;
			int optionalCount = 0;
			for (Method curMethod : clazz.getDeclaredMethods())
				if (curMethod.getName().equalsIgnoreCase("onCommand")) {
					//number of optionals
					requiredCount = curMethod.getParameterTypes().length;
					for (Annotation[] annotations : curMethod.getParameterAnnotations())
						for (Annotation annotation : annotations)
							if (annotation instanceof Optional) {
								if (clazz.isAnnotationPresent(Parameters.class))
									throw new QuackbotException("Java plugin " + name + " cannot have both optional annotation(s) and the Parameter Annotation");
								optionalCount++;
								requiredCount--;
							}
				}
			if (clazz.isAnnotationPresent(Parameters.class)) {

				if (clazz.getAnnotation(Parameters.class).value() != -1)
					requiredCount = clazz.getAnnotation(Parameters.class).value();
				if (clazz.getAnnotation(Parameters.class).optional() != -1)
					requiredCount = clazz.getAnnotation(Parameters.class).optional();
			}

			String help = (clazz.isAnnotationPresent(HelpDoc.class)) ? clazz.getAnnotation(HelpDoc.class).value() : "";
			cmd.setup(name, help, admin, enabled, null, optionalCount, requiredCount);
			CommandManager.addPermanentCommand(cmd);
		} catch (QuackbotException e) {
			log.error("Unable to load Java plugin " + cmd.getName(), e);
		}
	}
}
