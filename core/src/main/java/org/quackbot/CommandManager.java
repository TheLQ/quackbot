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
package org.quackbot;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Owner
 */
public class CommandManager {
	/**
	 * String - Name of Hook Method
	 * ArrayList - All Hooks for that method
	 *		BaseHook - Hook
	 */
	private static final Map<String, BaseCommand> cmds = Collections.synchronizedMap(new HashMap<String, BaseCommand>());
	private static final Map<String, BaseCommand> permanentCmds = Collections.synchronizedMap(new HashMap<String, BaseCommand>());
	/**
	 * TODO
	 */
	private static Logger log = LoggerFactory.getLogger(CommandManager.class);

	/**
	 * This class is a Singleton and should not be initialized
	 */
	private CommandManager() {
	}

	public static void addCommand(BaseCommand command) {
		if (StringUtils.isBlank(command.getName()))
			throw new IllegalArgumentException("Command names cannot be null. File: " + command.getFile());
		synchronized (cmds) {
			log.debug("Adding command " + command.getName());
			cmds.put(command.getName(), command);
			for (Method curMethod : command.getClass().getDeclaredMethods())
				curMethod.setAccessible(true);
		}
	}

	public static void removeCommand(String commandName) {
		synchronized (cmds) {
			log.debug("Attempting to remove command " + commandName);
			cmds.remove(commandName);
		}
	}

	public static void removeCommand(BaseCommand command) {
		for (Map.Entry<String, BaseCommand> curEntry : cmds.entrySet())
			if (curEntry.getValue() == command) {
				synchronized (cmds) {
					log.debug("Attempting to remove command " + command.getName());
					cmds.remove(curEntry.getKey());
				}
				break;
			}
	}

	public static void addPermanentCommand(BaseCommand command) {
		if (StringUtils.isBlank(command.getName()))
			throw new IllegalArgumentException("Command names cannot be null. File: " + command.getFile());
		synchronized (permanentCmds) {
			log.debug("Adding command " + command.getName());
			permanentCmds.put(command.getName(), command);
			for (Method curMethod : command.getClass().getDeclaredMethods())
				curMethod.setAccessible(true);
		}
	}

	public static void removePermanentCommand(String commandName) {
		synchronized (permanentCmds) {
			log.debug("Attempting to remove command " + commandName);
			permanentCmds.remove(commandName);
		}
	}

	public static void removePermanentCommand(BaseCommand cmd) {
		for (Map.Entry<String, BaseCommand> curEntry : permanentCmds.entrySet())
			if (curEntry.getValue() == cmd) {
				synchronized (permanentCmds) {
					log.debug("Removing command " + cmd.getName());
					permanentCmds.remove(curEntry.getKey());
				}
				break;
			}
	}

	public static void removeAll() {
		synchronized (cmds) {
			log.debug("Removing all non-permanent commands");
			cmds.clear();
		}
	}

	public static BaseCommand getCommand(String name) {
		BaseCommand cmd = cmds.get(name);
		if (cmd == null)
			cmd = permanentCmds.get(name);
		return cmd;
	}

	public static List<BaseCommand> getCommands() {
		return new ArrayList<BaseCommand>(cmds.values()) {
			{
				addAll(permanentCmds.values());
			}
		};
	}
}
