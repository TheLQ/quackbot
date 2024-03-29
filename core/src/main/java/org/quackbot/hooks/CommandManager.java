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
import com.google.common.collect.Multimap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.pircbotx.User;
import org.quackbot.AdminLevels;
import org.quackbot.Controller;
import org.quackbot.hooks.events.CommandAddedEvent;
import org.quackbot.hooks.events.CommandEnabledEvent;

/**
 *
 * @author Leon
 */
public class CommandManager {
	public static final CommandComparator COMMAND_COMPARATOR = new CommandComparator();
	protected final Controller controller;
	protected final BiMap<String, Command> commandsByName = HashBiMap.create();
	protected final Multimap<String, Command> commandsByAdminLevel = HashMultimap.create();
	protected final Set<Command> commandsByEnabled = new HashSet<Command>();
	protected final Multimap<String, User> activeAdmins = HashMultimap.create();

	public CommandManager(Controller controller) {
		this.controller = controller;
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
		commandsByEnabled.add(command);
		controller.getHookManager().dispatchEvent(new CommandAddedEvent(controller, command, true));
	}

	public void removeCommand(Command command) {
		commandsByName.inverse().remove(command);
		commandsByAdminLevel.remove(command.getMinimumAdminLevel(), command);
		commandsByEnabled.remove(command);
		controller.getHookManager().dispatchEvent(new CommandAddedEvent(controller, command, false));
	}

	public void enableCommand(Command command) {
		boolean enabled = commandsByEnabled.contains(command);
		if (!enabled)
			commandsByEnabled.add(command);
		controller.getHookManager().dispatchEvent(new CommandEnabledEvent(controller, command, enabled, true));
	}

	public void disableCommand(Command command) {
		boolean enabled = commandsByEnabled.contains(command);
		if (!enabled)
			commandsByEnabled.remove(command);
		controller.getHookManager().dispatchEvent(new CommandEnabledEvent(controller, command, enabled, false));
	}

	public ImmutableSortedSet<Command> getEnabledCommands() {
		return ImmutableSortedSet.copyOf(commandsByEnabled);
	}

	public boolean isValidAdminLevel(String adminLevel) {
		return controller.getQconfiguration().getAdminLevels().contains(adminLevel);
	}

	public ImmutableMultimap<String, User> getActiveAdmins() {
		return ImmutableMultimap.copyOf(activeAdmins);
	}

	public boolean addActiveAdmin(String level, User user) {
		checkArgument(isValidAdminLevel(level), "Invalid admin level: %s", level);

		//Replace existing user level with this one
		String existingUserLevel = getUserAdminLevel(user);
		if (!existingUserLevel.equals(AdminLevels.ANONYMOUS))
			removeActiveAdmin(existingUserLevel, user);
		return activeAdmins.put(level, user);
	}

	public boolean removeActiveAdmin(String level, User user) {
		return activeAdmins.remove(level, user);
	}

	public String getUserAdminLevel(User user) {
		for (Map.Entry<String, User> curEntry : activeAdmins.entries())
			if (curEntry.getValue() == user)
				return curEntry.getKey();
		//Not found, user isn't an admin
		return AdminLevels.ANONYMOUS;
	}

	public ImmutableList<String> getUserAdminLevels(User user) {
		String userLevel = getUserAdminLevel(user);
		ImmutableList.Builder<String> levelsBuilder = ImmutableList.builder();
		//Add each level till we reach the users
		for (String curLevel : controller.getQconfiguration().getAdminLevels()) {
			levelsBuilder.add(curLevel);
			if (curLevel.equals(userLevel))
				return levelsBuilder.build();
		}
		throw new RuntimeException("Could not finish building admin levels");
	}

	public static class CommandComparator implements Comparator<Command> {
		public int compare(Command o1, Command o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}
}
