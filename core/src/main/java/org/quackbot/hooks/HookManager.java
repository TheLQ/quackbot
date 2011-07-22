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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.hooks.Event;
import org.quackbot.Bot;
import org.quackbot.Controller;
import org.quackbot.err.InvalidHookException;
import org.quackbot.events.EndEvent;
import org.quackbot.events.StartEvent;
import org.slf4j.LoggerFactory;

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
@RequiredArgsConstructor
public class HookManager {
	/**
	 * String - Name of Hook Method
	 * ArrayList - All Hooks for that method
	 *		BaseHook - Hook
	 */
	private final Set<Hook> hooks = Collections.synchronizedSet(new HashSet());
	protected static final ExecutorService globalPool = Executors.newCachedThreadPool(new ThreadFactory() {
		public int count = 0;
		public ThreadGroup threadGroup = new ThreadGroup("mainPool");

		@Override
		public Thread newThread(Runnable r) {
			System.out.println("New thread for runnable " + r.toString());
			return new Thread(threadGroup, "mainPool-" + (++count));
		}
	});
	protected final Controller controller;

	public boolean addHook(Hook hook) throws InvalidHookException {
		log.debug("Adding hook " + hook.getName());
		Hook potentialHook = null;
		if ((potentialHook = getHook(hook.getName())) != null)
			//Whoa, this name has already been used before
			throw new InvalidHookException("Hook " + hook.getClass() + " uses the same name as " + potentialHook.getClass());
		return hooks.add(hook);
	}

	public boolean addHookOnce(Hook hook) {
		log.debug("Adding hook " + hook.getName() + " once");
		if (getHook(hook.getName()) == null)
			return hooks.add(hook);
		return false;
	}

	public boolean removeHook(String hookName) {
		log.debug("Removing hook " + hookName);
		boolean removed = false;
		synchronized (hooks) {
			Iterator<Hook> i = hooks.iterator();
			while (i.hasNext()) {
				Hook curHook = i.next();
				if (curHook.getName().equals(hookName)) {
					i.remove();
					removed = true;
				}
			}
		}
		return removed;
	}

	public boolean removeHook(Hook hook) {
		log.debug("Removing hook " + hook.getName());
		return hooks.remove(hook);
	}

	public Set<Hook> getHooks() {
		return Collections.unmodifiableSet(hooks);
	}

	public Hook getHook(String hookName) {
		synchronized (hooks) {
			for (Hook curHook : hooks)
				if (curHook.getName().equalsIgnoreCase(hookName))
					return curHook;
		}
		return null;
	}

	public boolean hookExists(Hook hook) {
		return hooks.contains(hook);
	}

	public boolean hookExists(String hookName) {
		//If we get a hook back, then it exists
		return getHook(hookName) != null;
	}

	public void dispatchEvent(final Event<Bot> event) {
		Runnable dispatcher = new Runnable() {
			public void run() {
				final List<Future> futures = new ArrayList<Future>(hooks.size());
				synchronized (hooks) {
					//First, execute onEvent for the StartEvent
					StartEvent startEvent = new StartEvent(controller);
					for (Hook curHook : hooks)
						executeHook(curHook, startEvent);

					//Execute all events, building up the futures to wait for execution
					for (Hook curHook : hooks)
						futures.add(executeHook(curHook, event));
				}

				//Wait for all futures to complete
				try {
					for (Future curFuture : futures)
						curFuture.get();
				} catch (Exception ex) {
					LoggerFactory.getLogger(this.getClass()).error("Exception encountered when waiting for Listener's to finish so an EndEvent could be dispatched", ex);
				}

				//Dispatch an EndEvent
				synchronized (hooks) {
					EndEvent endEvent = new EndEvent(controller);
					for (Hook curHook : hooks)
						executeHook(curHook, endEvent);
				}
			}
		};

		//Dump into the correct thread pool
		if (event.getBot() != null)
			event.getBot().getThreadPool().execute(dispatcher);
		else
			globalPool.execute(dispatcher);
	}

	protected Future executeHook(final Hook hook, final Event<Bot> event) {
		//Make a runnable that can be passed to any thread pool
		Runnable run = new Runnable() {
			public void run() {
				try {
					event.getBot().getController().getStorage().beginTransaction();
					hook.onEvent(event);
					event.getBot().getController().getStorage().endTransaction(true);
				} catch (Throwable ex) {
					LoggerFactory.getLogger(this.getClass()).error("Exception encountered when executing Listener", ex);
					event.getBot().getController().getStorage().endTransaction(false);
				}
			}
		};

		//Dispatch to appropiate thread pool
		if (event.getBot() != null)
			//Use bot's thread pool
			return event.getBot().getThreadPool().submit(run);
		else
			//No bot, use global thread pool
			return globalPool.submit(run);
	}

	/**
	 * Get all stored Commands, sifting out the plain Hooks
	 * @return An Unmodifiable set of Commands. An empty set means there are
	 *         no commands.
	 */
	@Synchronized("hooks")
	public Set<Command> getCommands() {
		//Get all Commands from Hook list
		Set<Command> commands = new HashSet<Command>();
		for (Hook curHook : hooks)
			if (curHook instanceof Command)
				commands.add((Command) curHook);
		return Collections.unmodifiableSet(commands);
	}

	/**
	 * Gets a command by name
	 * @param command A command name
	 * @return The command object, or null if it doesn't exist
	 */
	@Synchronized("hooks")
	public Command getCommand(String command) {
		for (Hook curHook : hooks)
			if (curHook instanceof Command && curHook.getName().equalsIgnoreCase(command))
				return (Command) curHook;
		return null;
	}
}