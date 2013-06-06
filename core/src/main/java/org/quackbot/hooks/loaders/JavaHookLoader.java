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

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.hooks.Listener;
import org.quackbot.AdminLevel;
import org.quackbot.Controller;
import org.quackbot.hooks.Command;
import org.quackbot.hooks.HookLoader;
import org.quackbot.events.CommandEvent;
import org.quackbot.hooks.QListener;
import org.quackbot.hooks.java.JavaArgument;
import org.quackbot.hooks.java.JavaCommand;

/**
 * This is the global JavaBean/Utility for all Java written commands
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class JavaHookLoader implements HookLoader {
	@Override
	public QListener load(String fileLocation) throws Exception {
		throw new UnsupportedOperationException("Java plugins cannot be loaded. Attempted to load " + fileLocation);
	}

	public static ImmutableList<Command> loadCommands(Controller controller, Object command) throws Exception {
		checkNotNull(controller, "Must specify controller");
		checkNotNull(command, "Must specify command object");

		//Find any command annotations
		ImmutableList.Builder<Command> addedCommands = ImmutableList.builder();
		for (Method curMethod : command.getClass().getMethods()) {
			JavaCommand commandAnnotation = curMethod.getAnnotation(JavaCommand.class);
			if (commandAnnotation == null)
				continue;

			//Parse arguments first
			ImmutableList.Builder<JavaMethodArgument> arguments = ImmutableList.builder();
			for (JavaArgument curArgument : commandAnnotation.arguments())
				arguments.add(new JavaMethodArgument(curArgument.value(), curArgument.getArgumentHelp(), curArgument.isRequired()));

			//Build and add command to hookManager
			AdminLevel minimumLevel =  controller.getAdminLevel(commandAnnotation.minimumLevel());
			if(minimumLevel == null)
				throw new RuntimeException("Unknown level " + commandAnnotation.minimumLevel());
			JavaMethodCommand methodCommand = new JavaMethodCommand(commandAnnotation.name(),
					commandAnnotation.help(),
					minimumLevel,
					arguments.build(), 
					command, 
					curMethod);
			controller.getHookManager().addCommand(methodCommand);
			addedCommands.add(methodCommand);
		}
		return addedCommands.build();
	}
	
	public static ImmutableList<Command> loadListener(Controller controller, Listener listenerWithCommands) throws Exception {
		controller.getHookManager().addListener(listenerWithCommands);
		return loadCommands(controller, listenerWithCommands);
	}

	@Data
	public static class JavaMethodCommand implements Command {
		protected final String name;
		protected final String help;
		protected final AdminLevel minimumAdminLevel;
		protected final ImmutableList<JavaMethodArgument> arguments;
		protected final Object commandObject;
		protected final Method commandMethod;

		public void onCommand(CommandEvent event, ImmutableList<String> argumentsString) throws Exception {
			//Attempt basic conversion of fields
			List<Object> argumentsObject = new ArrayList();
			Class<?>[] methodParameters = commandMethod.getParameterTypes();

			if (argumentsString.size() > methodParameters.length)
				throw new RuntimeException("More arguments given than there are parameters");
			for (int i = 0, size = methodParameters.length; i < size; i++)
				if (methodParameters[i] == String.class)
					argumentsObject.add(argumentsString.get(i));
				else
					throw new RuntimeException("Unknown argument class " + methodParameters[i]);
			commandMethod.invoke(commandObject, argumentsObject.toArray());
		}
	}

	@Data
	public static class JavaMethodArgument implements Command.Argument {
		protected final String name;
		protected final String argumentHelp;
		protected final boolean required;
	}
}
