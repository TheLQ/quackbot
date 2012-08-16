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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.hooks.Event;
import org.quackbot.Bot;
import org.quackbot.Controller;
import org.quackbot.err.InvalidHookException;
import org.quackbot.events.EndEvent;
import org.quackbot.events.StartEvent;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
@Slf4j
public class HookManager {
	/**
	 * String - Name of Hook Method
	 * ArrayList - All Hooks for that method
	 *		BaseHook - Hook
	 */
	@Autowired
	protected Map<String, Hook> hooks;
	protected final ExecutorService globalPool = Executors.newCachedThreadPool(new ThreadFactory() {
		public int count = 0;
		public ThreadGroup threadGroup = new ThreadGroup("mainPool");

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(threadGroup, "mainPool-" + (++count));
		}
	});
	@Autowired
	protected Controller controller;
	protected AtomicLong currentId = new AtomicLong();

	public void addHook(Hook hook) throws InvalidHookException {
		log.debug("Adding hook " + hook.getName());
		Hook potentialHook = null;
		if ((potentialHook = getHook(hook.getName())) != null)
			//Whoa, this name has already been used before
			throw new InvalidHookException("Hook " + hook.getName() + " (" + hook.getClass()
					+ ") uses the same name as " + potentialHook.getName() + " (" + potentialHook.getClass() + ")");
		hooks.put(hook.getName(), hook);
	}

	public void addHookOnce(Hook hook) {
		log.debug("Adding hook " + hook.getName() + " once");
		if (getHook(hook.getName()) == null)
			hooks.put(hook.getName(), hook);
	}

	public void removeHook(String hookName) {
		log.debug("Removing hook " + hookName);
		synchronized (hooks) {
			hooks.remove(hookName);
		}
	}

	public void removeHook(Hook hook) {
		log.debug("Removing hook " + hook.getName());
		hooks.remove(hook.getName());
	}

	public Set<Hook> getHooks() {
		return Collections.unmodifiableSet(new HashSet(hooks.values()));
	}

	public Hook getHook(String hookName) {
		synchronized (hooks) {
			return hooks.get(hookName);
		}
	}

	public boolean hookExists(Hook hook) {
		return hooks.containsValue(hook);
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
					for (Hook curHook : hooks.values())
						executeHook(curHook, startEvent);

					//Execute all events, building up the futures to wait for execution
					for (Hook curHook : hooks.values())
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
					for (Hook curHook : hooks.values())
						executeHook(curHook, endEvent);
				}
			}
		};
		executeRunnable(event, dispatcher);
	}

	protected Future executeHook(final Hook hook, final Event<Bot> event) {
		//Make a runnable that can be passed to any thread pool
		Runnable run = new Runnable() {
			public void run() {
				try {
					hook.onEvent(event);
				} catch (Throwable ex) {
					LoggerFactory.getLogger(this.getClass()).error("Exception encountered when executing Listener", ex);
				}
			}
		};
		return executeRunnable(event, run);
	}

	protected Future executeRunnable(Event<Bot> event, Runnable runnable) {
		if (event.getBot() != null)
			//Use bot's thread pool
			return event.getBot().getThreadPool().submit(runnable);
		else
			//No bot, use global thread pool
			return globalPool.submit(runnable);
	}

	/**
	 * Get all stored Commands, sifting out the plain Hooks
	 * @return An Unmodifiable set of Commands. An empty set means there are
	 *         no commands.
	 */
	public Set<Command> getCommands() {
		//Get all Commands from Hook list
		Set<Command> commands = new HashSet<Command>();
		for (Hook curHook : hooks.values())
			if (curHook instanceof Command)
				commands.add((Command) curHook);
		return Collections.unmodifiableSet(commands);
	}

	/**
	 * Gets a command by name
	 * @param command A command name
	 * @return The command object, or null if it doesn't exist
	 */
	public Command getCommand(String command) {
		Hook hook = hooks.get(command);
		if (hook instanceof Command)
			return (Command) hook;
		return null;
	}
	
	public void setCurrentId(long currentId) {
		this.currentId.set(currentId);
	}

	public long getCurrentId() {
		return currentId.get();
	}

	public long incrementCurrentId() {
		return currentId.getAndIncrement();
	}
}