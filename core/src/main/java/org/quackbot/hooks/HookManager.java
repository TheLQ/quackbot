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
package org.quackbot.hooks;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.quackbot.Bot;

/**
 * First, some definitions:
 * <ul>
 * <li>Event - An operation that is requested, originating from the user.
 * <li>Event - Class carrying out an operation in place of or with default code
 * <li>Event List - List of hooks that are executed in sequential order upon an
 *                 event
 * </ul>
 *
 * A hook is a special type of plugin that is called during the execution of
 * an event. The advantage of a hook based system is that the flow
 * and execution of various parts of the system are changeable by you.
 * They can be added anywhere in the List and can halt the execution of the Event
 * List, make hooks be ignored, and rearrange and delete hooks.
 * <p>
 * In Quackbot, everything that deals with the execution of user events is kept
 * on the list, even the core methods of Quackbot for executing plugins. Because
 * of this, you could, for example, use your own custom parsing method instead
 * of the provided one or handle PM and various IRC information commands in your
 * own way.
 * <p>
 * Internally, Event types are kept on an enum map while hooks are kept on their
 * corresponding {@link HookMap}. Please see {@link HookMap} for more
 * information. The the avalible events are defined in the enum.
 * <p>
 * HookManager is a singleton handling all Event operations. It holds the above
 * mentioned map and various methods for executing tasks.
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Slf4j
public class HookManager extends ThreadedListenerManager<Bot> {
	public static final CommandComparator COMMAND_COMPARATOR = new CommandComparator();
	protected BiMap<String, Command> commands = HashBiMap.create();

	/**
	 * All listeners that are QListeners
	 * @return An <b>immutable copy</b> of the current registered QListeners
	 */
	public ImmutableSortedSet<QListener> getQListeners() {
		return ImmutableSortedSet.copyOf(Iterables.filter(listeners, QListener.class));
	}

	/**
	 * All registered commands
	 * @return An <b>immutable copy</b> of the current registered commands
	 */
	public ImmutableSortedSet<Command> getCommands() {
		return ImmutableSortedSet.copyOf(COMMAND_COMPARATOR, commands.values());
	}

	/**
	 * Gets a command by name
	 * @param command A command name
	 * @return The command object, or null if it doesn't exist
	 */
	public Command getCommand(String command) {
		return commands.get(command);
	}

	public static class CommandComparator implements Comparator<Command> {
		public int compare(Command o1, Command o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}
}