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
package org.quackbot.events;

import java.io.File;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Event;
import org.quackbot.Controller;
import org.quackbot.HookLoader;
import org.quackbot.hook.Hook;

/**
 * Created when a plugin is loaded. Contains either the loaded plugin or the
 * exception that occured when loading it
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HookLoadEvent extends Event {
	private final Controller controller;
	private final Hook hook;
	private final HookLoader pluginLoader;
	private final File file;
	private final Exception exception;

	public HookLoadEvent(Controller controller, Hook hook, HookLoader pluginLoader, File file, Exception exception) {
		super(null);
		this.controller = controller;
		this.pluginLoader = pluginLoader;
		this.file = file;
		this.exception = exception;
		this.hook = hook;
	}
	
	/**
	 * Does NOT respond to the server! This will throw an {@link UnsupportedOperationException} 
	 * since there is no bot.
	 * @param response The response to send 
	 */
	@Override
	public void respond(String response) {
		throw new UnsupportedOperationException("Attempting to respond to a HookLoad");
	}
}
