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

import org.quackbot.hooks.java.HelpDoc;
import org.quackbot.hooks.java.AdminOnly;
import org.quackbot.hooks.java.Disabled;
import org.quackbot.hooks.java.Optional;
import org.quackbot.hooks.java.Parameters;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.apache.commons.lang.StringUtils;
import org.pircbotx.User;
import org.quackbot.hooks.Command;
import org.quackbot.hooks.HookLoader;
import org.quackbot.err.QuackbotException;
import org.quackbot.events.CommandEvent;
import org.quackbot.hooks.Hook;
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
	public Hook load(String fileLocation) throws Exception {
		throw new UnsupportedOperationException("Java plugins cannot be loaded. Attempted to load " + fileLocation);
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
		String help = (clazz.getAnnotation(HelpDoc.class) != null) ? clazz.getAnnotation(HelpDoc.class).value() : null;

		//Get required and optional parameters
		int requiredCount = 0;
		int optionalCount = 0;
		Method previousMethod = null;
		for (Method curMethod : clazz.getDeclaredMethods()) {
			int totalParams = 0;
			Class<?>[] parameters = curMethod.getParameterTypes();
			
			//Ignore if this isn't an onCommand method
			if (!curMethod.getName().equalsIgnoreCase("onCommand"))
				continue;
			
			//Make sure there aren't multiple onCommand methods
			if(previousMethod != null)
				throw new QuackbotException("Can't have multiple onCommand methods in class " + clazz);
			else
				previousMethod = curMethod;
			
			//Ignore if there are 0 parameters
			if (parameters.length == 0) {
				log.debug("Ignoring " + curMethod.toGenericString() + "  - No parameters");
				continue;
			}

			//Ignore if the first parameter isn't a CommandEvent
			if (parameters[0] != CommandEvent.class) {
				log.debug("Ignoring " + curMethod.toGenericString() + "  - First parameter isn't a command event");
				continue;
			}

			//Ignore if CommandEvent is the only parameter, this is handled by CoreQuackbotHook
			if (parameters.length == 1) {
				log.debug("Ignoring " + curMethod.toGenericString() + "  - Only parameter is CommandEvent");
				continue;
			}
			
			//Throw exception if there is more than one array
			int numArrays = 0;
			for(Class curClass : parameters)
				if(curClass.isArray())
					numArrays++;
			if(numArrays > 1)
				throw new QuackbotException("Method " + curMethod.toGenericString() + " has more than one array as a parameter.");

			//Account for CommandEvent when calculating parameters
			totalParams = parameters.length - 1;

			Parameters paramAnnotation = clazz.getAnnotation(Parameters.class);

			//Build parameter counts based off of @Optional first
			requiredCount = totalParams;
			for (Annotation[] annotations : curMethod.getParameterAnnotations())
				for (Annotation annotation : annotations)
					if (annotation instanceof Optional) {
						if (paramAnnotation != null)
							throw new QuackbotException("Java Hook " + name + " cannot have both optional annotation(s) and Parameter Annotation");
						optionalCount++;
						requiredCount--;
					}

			//End here if @Optional exists and therefor changed the required parameter count
			if (requiredCount != totalParams)
				continue;
			
			//No @Optional, see if the last element is an array. Array = unlimited
			if(parameters[parameters.length - 1].isArray()) {
				optionalCount = -1;
				continue;
			}

			//No @Optional, use totalParams as required count if there's no @Parameter annotation
			if (paramAnnotation == null)
				continue;

			//There is @Parameter, make sure there's enough required parameters
			if (totalParams < paramAnnotation.value())
				throw new QuackbotException("Method " + curMethod.toGenericString() + " has less parameters "
						+ "(" + totalParams + ") then the @Parameter annotation says it should (" + paramAnnotation.value() + ")");

			//Make sure there isn't too many or not enough parameters with optional params
			if (paramAnnotation.optional() != -1 && (paramAnnotation.value() + paramAnnotation.optional() != totalParams))
				throw new QuackbotException("Method " + curMethod.toGenericString() + " has too many or two few parameters "
						+ "(" + totalParams + ") then the @Parameter annotation says it should (" + (paramAnnotation.value() + paramAnnotation.optional()) + " total)");

			//Values have been verified, now they can be used
			requiredCount = paramAnnotation.value();
			optionalCount = paramAnnotation.optional();
		}
		//Setup and send to the HookManager
		cmd.setup(help, admin, enabled, requiredCount, optionalCount);

		return cmd;
	}
}
