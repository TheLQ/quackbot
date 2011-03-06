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

import java.io.File;

/**
 * The core of any PluginLoader abstract class. All plugin types need to implement this
 * <p>
 * Many of the get* and is* methods should respond to private fields, just like a
 * JavaBean syntax. There only here to force you to use them.
 *
 *
 * @author Lord.Quackstar
 */
public interface PluginLoader {
	/**
	 * Load method that is called when a file that matches the extension specified
	 * is found in plugins directory. If this plugin requires manual adding (IE JavaPlugin)
	 * then it must set the name to null so it can be ignored
	 * <p>
	 * Implementations of this must completly setup the Command, parsing all avalible infomration.
	 * The plugin must then be able to be activated by {@link #invoke(java.lang.String[], Quackbot.Bot, Quackbot.info.BotEvent)}
	 * @param file         The file that contains the script
	 * @throws Exception   Any exception encountered while parsing. The command will not be added
	 */
	public void load(File file) throws Exception;
}