/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
		System.out.println("Adding command " + cmd.getName());
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
		System.out.println("Adding command " + cmd.getName());
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
