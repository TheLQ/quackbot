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
package Quackbot;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
	private static final ArrayList<Command> cmds = new ArrayList<Command>();
	private static final ArrayList<Command> permanentCmds = new ArrayList<Command>();
	/**
	 * TODO
	 */
	private static Logger log = LoggerFactory.getLogger(CommandManager.class);

	/**
	 * This class is a Singleton and should not be initialized
	 */
	private CommandManager() {
	}

	public static void addCommand(Command cmd) {
		log.info("Adding command " + cmd.getName());
		cmds.add(cmd);
		for (Method curMethod : cmd.getClass().getDeclaredMethods())
			curMethod.setAccessible(true);

	}

	public static void removeCommand(String hookName) {
		for (Command curCmd : cmds)
			if (curCmd.getName().equalsIgnoreCase(hookName))
				cmds.remove(curCmd);

	}

	public static void removeCommand(Command hook) {
		for (Command curCmds : cmds)
			if (curCmds == hook)
				cmds.remove(curCmds);
	}

	public static void addPermanentCommand(Command cmd) {
		log.info("Adding command " + cmd.getName());
		permanentCmds.add(cmd);
		for (Method curMethod : cmd.getClass().getDeclaredMethods())
			curMethod.setAccessible(true);
	}

	public static void removePermanentCommand(String hookName) {
		for (Command curCmd : permanentCmds)
			if (curCmd.getName().equalsIgnoreCase(hookName))
				permanentCmds.remove(curCmd);
	}

	public static void removePermanentCommand(Command hook) {
		for (Command curCmds : permanentCmds)
			if (curCmds == hook)
				permanentCmds.remove(curCmds);
	}

	public static void removeAll() {
		cmds.clear();
	}

	public static Command getCommand(String name) {
		for (Command cmd : getCommands())
			if (cmd.getName().equalsIgnoreCase(name))
				return cmd;
		return null;
	}

	public static ArrayList<Command> getCommands() {
		return new ArrayList<Command>(cmds) {
			{
				addAll(permanentCmds);
			}
		};
	}
}
