/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quackbot.hooks;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import java.util.Comparator;
import org.pircbotx.User;
import org.quackbot.QConfiguration;

/**
 *
 * @author Leon
 */
public class CommandManager {
	public static final CommandComparator COMMAND_COMPARATOR = new CommandComparator();
	protected final BiMap<String, Command> commandsByName = HashBiMap.create();
	protected final Multimap<String, Command> commandsByAdminLevel = HashMultimap.create();
	protected final ImmutableList<String> adminLevels;
	protected final Multimap<String, User> activeAdmins = HashMultimap.create();

	public CommandManager(QConfiguration qconfiguration) {
		this.adminLevels = qconfiguration.getAdminLevels();
	}

	/**
	 * All registered commands
	 * @return An <b>immutable copy</b> of the current registered commands
	 */
	public ImmutableSortedSet<Command> getCommands() {
		return ImmutableSortedSet.copyOf(COMMAND_COMPARATOR, commandsByName.values());
	}
	
	public ImmutableSortedSet<Command> getCommandsForAdminLevel(String adminLevel) {
		checkArgument(isValidAdminLevel(adminLevel), "Invalid admin level: %s", adminLevel);
		return ImmutableSortedSet.copyOf(COMMAND_COMPARATOR, commandsByAdminLevel.get(adminLevel));
	}

	/**
	 * Gets a command by name
	 * @param command A command name
	 * @return The command object, or null if it doesn't exist
	 */
	public Command getCommand(String command) {
		return commandsByName.get(command);
	}

	public void addCommand(Command command) {
		commandsByName.put(command.getName(), command);
		commandsByAdminLevel.put(command.getMinimumAdminLevel(), command);
	}

	public void removeCommand(Command command) {
		commandsByName.inverse().remove(command);
		commandsByAdminLevel.remove(command.getMinimumAdminLevel(), command);
	}
	
	public boolean isValidAdminLevel(String adminLevel) {
		return adminLevels.contains(adminLevel);
	}
	
	public ImmutableMultimap<String, User> getActiveAdmins() {
		return ImmutableMultimap.copyOf(activeAdmins);
	}
	
	public boolean addActiveAdmin(String level, User user) {
		checkArgument(isValidAdminLevel(level), "Invalid admin level: %s", level);
		return activeAdmins.put(level, user);
	}
	
	public boolean removeActiveAdmin(String level, User user) {
		return activeAdmins.remove(level, user);
	}
	

	public static class CommandComparator implements Comparator<Command> {
		public int compare(Command o1, Command o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}
}
