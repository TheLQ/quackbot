/**
 * Copyright (C) 2010 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of PircBotX.
 *
 * PircBotX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PircBotX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PircBotX.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quackbot.hooks;

import org.quackbot.hooks.java.HelpDoc;
import org.quackbot.hooks.java.AdminOnly;
import org.quackbot.hooks.java.Disabled;
import org.quackbot.hooks.java.Optional;
import org.quackbot.hooks.java.Parameters;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.quackbot.Command;
import org.quackbot.HookLoader;
import org.quackbot.err.QuackbotException;
import org.quackbot.hook.Hook;
import org.quackbot.hook.HookManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the global JavaBean/Utility for all Java written commands
 *
 * @author Lord.Quackstar
 */
public class JavaHookLoader implements HookLoader {
	/**
	 * Log4j logger
	 */
	private static Logger log = LoggerFactory.getLogger(JavaHookLoader.class);

	@Override
	public Hook load(File file) throws Exception {
		throw new UnsupportedOperationException("Java plugins cannot be loaded. Attempted to load " + file.getAbsolutePath());
	}

	public static Command load(Command cmd) throws Exception {
		if (cmd == null)
			return null;
		Class<? extends Command> clazz = cmd.getClass();
		String name = clazz.getSimpleName();
		log.info("New Java Plugin " + name);

		//Command info creation
		boolean admin = clazz.isAnnotationPresent(AdminOnly.class);
		boolean enabled = !clazz.isAnnotationPresent(Disabled.class);
		String help = StringUtils.defaultString(clazz.getAnnotation(HelpDoc.class).value());

		//Get required and optional parameters
		int requiredCount = 0;
		int optionalCount = 0;
		for (Method curMethod : clazz.getMethods()) {
			int totalParams = 0;
			Class<?>[] parameters = curMethod.getParameterTypes();
			if (curMethod.getName().equalsIgnoreCase("onCommand") || curMethod.getName().equalsIgnoreCase("onCommandChannel")) {
				//Ignore if there are less than 2 parameters
				if (parameters.length < 2) {
					log.debug("Ignoring " + curMethod.toGenericString() + "  - Not enough parameters");
					continue;
				}

				//Ignore if the first 2 parameters aren't Channel and User
				if (parameters[0] != Channel.class || parameters[1] != User.class) {
					log.debug("Ignoring " + curMethod.toGenericString() + "  - First 2 parameters aren't Channel then User");
					continue;
				}

				//Ignore if there is a 3rd parameter and its an Array
				if (parameters.length > 2 && parameters[3].isArray()) {
					log.debug("Ignoring " + curMethod.toGenericString() + "  - 3rd parameter is an Array");
					continue;
				}
				totalParams = parameters.length - 2;
			} else if (curMethod.getName().equalsIgnoreCase("onCommandPM")) {
				//Ignore if there are 0 parameters
				if (parameters.length == 0) {
					log.debug("Ignoring " + curMethod.toGenericString() + "  - Not enough parameters");
					continue;
				}

				//Ignore if the first parameter isn't a User
				if (parameters[1] != User.class) {
					log.debug("Ignoring " + curMethod.toGenericString() + "  - First parameter isn't user");
					continue;
				}

				//Ignore if there is a 2nd parameter and its an Array
				if (parameters.length > 1 && parameters[3].isArray()) {
					log.debug("Ignoring " + curMethod.toGenericString() + "  - 2nd parameter is an Array");
					continue;
				}
				totalParams = parameters.length - 1;
			} else
				continue;

			Parameters paramAnnotation = clazz.getAnnotation(Parameters.class);

			//If there are 0 params, make sure @Parameter doesn't say it needs more than 0
			if (totalParams == 0 && paramAnnotation != null && (paramAnnotation.value() + paramAnnotation.optional() != 0))
				throw new QuackbotException("Method " + curMethod.toGenericString() + " has no parameters even though @Parameter says it should");

			//Build parameter list based off of @Optional first
			requiredCount = totalParams;
			for (Annotation[] annotations : curMethod.getParameterAnnotations())
				for (Annotation annotation : annotations)
					if (annotation instanceof Optional) {
						if (paramAnnotation != null)
							throw new QuackbotException("Java Hook " + name + " cannot have both optional annotation(s) and Parameter Annotation");
						optionalCount++;
						requiredCount--;
					}

			//Must not of had any @Optional annotations, use @Parameter if it exists instead
			if (paramAnnotation != null) {
				//Make sure the method actually has enough params
				if (paramAnnotation.value() + paramAnnotation.optional() != totalParams)
					throw new QuackbotException("Method " + curMethod.toGenericString() + " has " + totalParams + " parameters while @Parameter annotation specifies " + paramAnnotation.value() + paramAnnotation.optional());
				requiredCount = paramAnnotation.value();
				optionalCount = paramAnnotation.optional();
			}
		}

		//Setup and send to the HookManager
		cmd.setup(help, admin, enabled, requiredCount, optionalCount);
		return cmd;
	}
}
